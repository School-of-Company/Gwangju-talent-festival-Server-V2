package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatReservationLimitExceededException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.BulkReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.ReservationSeatService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.auth.CustomUserDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ReservationSeatConcurrencyTest {

    @MockBean
    Sheets sheets;

    @MockBean
    Drive drive;

    @Autowired
    private ReservationSeatService reservationSeatService;

    @Autowired
    private SeatReservationRepository seatReservationRepository;

    @Autowired
    private UserRepository userRepository;

    private List<UserEntity> testUsers;

    @BeforeEach
    void setUp() {
        testUsers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            testUsers.add(userRepository.save(UserEntity.builder()
                    .phoneNumber("0109999" + String.format("%04d", i))
                    .role(Role.USER)
                    .build()));
        }
    }

    @AfterEach
    void tearDown() {
        seatReservationRepository.deleteAll();
        userRepository.deleteAll(testUsers);
    }

    private void setAuth(long userId, Role role) {
        CustomUserDetails details = CustomUserDetails.fromToken(userId, role);
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    void 동일_좌석_동시_예약시_1건만_성공한다() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        try {
            for (int i = 0; i < threadCount; i++) {
                final long userId = testUsers.get(i).getId();
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        setAuth(userId, Role.USER);
                        reservationSeatService.execute(new ReservationSeatRequest("A", 33));
                        successCount.incrementAndGet();
                    } catch (SeatAlreadyReservedException e) {
                        failCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Throwable t) {
                        exceptions.add(t);
                    } finally {
                        doneLatch.countDown();
                        SecurityContextHolder.clearContext();
                    }
                });
            }

            startLatch.countDown();
            boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
            assertThat(completed).withFailMessage("스레드 완료 대기 중 시간 초과가 발생했습니다.").isTrue();
        } finally {
            executor.shutdown();
        }

        assertThat(exceptions).isEmpty();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);
    }

    @Test
    void 동일_USER_동시_예약시_한도_초과는_실패한다() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger limitExceededCount = new AtomicInteger(0);

        long userId = testUsers.get(0).getId();
        String[] sections = {"A", "A"};
        Integer[] seatNumbers = {33, 34};

        try {
            for (int i = 0; i < threadCount; i++) {
                final int idx = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        setAuth(userId, Role.USER);
                        reservationSeatService.execute(new ReservationSeatRequest(sections[idx], seatNumbers[idx]));
                        successCount.incrementAndGet();
                    } catch (SeatReservationLimitExceededException e) {
                        limitExceededCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Throwable t) {
                        exceptions.add(t);
                    } finally {
                        doneLatch.countDown();
                        SecurityContextHolder.clearContext();
                    }
                });
            }

            startLatch.countDown();
            boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
            assertThat(completed).withFailMessage("스레드 완료 대기 중 시간 초과가 발생했습니다.").isTrue();
        } finally {
            executor.shutdown();
        }

        assertThat(exceptions).isEmpty();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(limitExceededCount.get()).isEqualTo(1);
    }

    @Test
    void 동일_PERFORMER가_세_좌석을_동시_예약하면_두_건만_성공한다() throws InterruptedException {
        UserEntity performer = userRepository.save(UserEntity.builder()
                .phoneNumber("01088880000")
                .role(Role.PERFORMER)
                .build());
        testUsers.add(performer);

        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger limitExceededCount = new AtomicInteger();
        int[] seatNumbers = {16, 17, 18};

        try {
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        setAuth(performer.getId(), Role.PERFORMER);
                        reservationSeatService.execute(new ReservationSeatRequest("A", seatNumbers[index]));
                        successCount.incrementAndGet();
                    } catch (SeatReservationLimitExceededException e) {
                        limitExceededCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Throwable t) {
                        exceptions.add(t);
                    } finally {
                        doneLatch.countDown();
                        SecurityContextHolder.clearContext();
                    }
                });
            }

            startLatch.countDown();
            assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdown();
        }

        assertThat(exceptions).isEmpty();
        assertThat(successCount.get()).isEqualTo(2);
        assertThat(limitExceededCount.get()).isEqualTo(1);
        assertThat(seatReservationRepository.countByUserId(performer.getId())).isEqualTo(2);
    }

    @Test
    void 동일_PERFORMER의_동일한_다중_요청은_동시에_재전송돼도_멱등하게_성공한다() throws InterruptedException {
        UserEntity performer = savePerformer("01088880001");
        BulkReservationSeatRequest request = bulk(16, 17);
        AtomicInteger successCount = new AtomicInteger();

        Runnable reservation = () -> {
            setAuth(performer.getId(), Role.PERFORMER);
            try {
                reservationSeatService.executeBulk(request);
                successCount.incrementAndGet();
            } finally {
                SecurityContextHolder.clearContext();
            }
        };

        List<Throwable> exceptions = runConcurrently(reservation, reservation);

        assertThat(exceptions).isEmpty();
        assertThat(successCount.get()).isEqualTo(2);
        assertThat(seatReservationRepository.countByUserId(performer.getId())).isEqualTo(2);
    }

    @Test
    void 두_PERFORMER가_같은_좌석을_역순으로_요청해도_한_요청만_전체_성공한다() throws InterruptedException {
        UserEntity first = savePerformer("01088880002");
        UserEntity second = savePerformer("01088880003");
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger alreadyReservedCount = new AtomicInteger();

        Runnable firstReservation = bulkReservation(first, bulk(16, 17), successCount, alreadyReservedCount);
        Runnable secondReservation = bulkReservation(second, bulk(17, 16), successCount, alreadyReservedCount);

        List<Throwable> exceptions = runConcurrently(firstReservation, secondReservation);

        assertThat(exceptions).isEmpty();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(alreadyReservedCount.get()).isEqualTo(1);
        assertThat(seatReservationRepository.count()).isEqualTo(2);
        assertThat(List.of(
                seatReservationRepository.countByUserId(first.getId()),
                seatReservationRepository.countByUserId(second.getId())))
                .containsExactlyInAnyOrder(0L, 2L);
    }

    @Test
    void 겹치는_다중_요청이_경쟁해도_패배한_요청의_비경합_좌석은_저장되지_않는다() throws InterruptedException {
        UserEntity first = savePerformer("01088880004");
        UserEntity second = savePerformer("01088880005");
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger alreadyReservedCount = new AtomicInteger();

        List<Throwable> exceptions = runConcurrently(
                bulkReservation(first, bulk(16, 17), successCount, alreadyReservedCount),
                bulkReservation(second, bulk(17, 18), successCount, alreadyReservedCount));

        assertThat(exceptions).isEmpty();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(alreadyReservedCount.get()).isEqualTo(1);
        assertThat(seatReservationRepository.count()).isEqualTo(2);
        assertThat(List.of(
                seatReservationRepository.countByUserId(first.getId()),
                seatReservationRepository.countByUserId(second.getId())))
                .containsExactlyInAnyOrder(0L, 2L);
    }

    @Test
    void 동일_PERFORMER의_서로_다른_다중_요청이_경쟁해도_최종_두_좌석만_남는다() throws InterruptedException {
        UserEntity performer = savePerformer("01088880006");
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger limitExceededCount = new AtomicInteger();

        Runnable first = bulkReservationWithLimit(performer, bulk(16, 17), successCount, limitExceededCount);
        Runnable second = bulkReservationWithLimit(performer, bulk(18, 19), successCount, limitExceededCount);

        List<Throwable> exceptions = runConcurrently(first, second);

        assertThat(exceptions).isEmpty();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(limitExceededCount.get()).isEqualTo(1);
        assertThat(seatReservationRepository.countByUserId(performer.getId())).isEqualTo(2);
    }

    @Test
    void 다중_요청의_한_좌석이_선점됐으면_다른_좌석도_저장되지_않는다() {
        UserEntity performer = savePerformer("01088880007");
        UserEntity blocker = savePerformer("01088880008");
        seatReservationRepository.saveAndFlush(SeatEntity.builder()
                .seatSection("A")
                .seatNumber(17)
                .user(blocker)
                .build());
        setAuth(performer.getId(), Role.PERFORMER);

        try {
            assertThatThrownBy(() -> reservationSeatService.executeBulk(bulk(16, 17)))
                    .isInstanceOf(SeatAlreadyReservedException.class);
        } finally {
            SecurityContextHolder.clearContext();
        }

        assertThat(seatReservationRepository.existsBySeatSectionAndSeatNumber("A", 16)).isFalse();
        assertThat(seatReservationRepository.countByUserId(performer.getId())).isZero();
        assertThat(seatReservationRepository.countByUserId(blocker.getId())).isEqualTo(1);
    }

    private UserEntity savePerformer(String phoneNumber) {
        UserEntity performer = userRepository.save(UserEntity.builder()
                .phoneNumber(phoneNumber)
                .role(Role.PERFORMER)
                .build());
        testUsers.add(performer);
        return performer;
    }

    private BulkReservationSeatRequest bulk(int firstSeatNumber, int secondSeatNumber) {
        return new BulkReservationSeatRequest(List.of(
                new ReservationSeatRequest("A", firstSeatNumber),
                new ReservationSeatRequest("A", secondSeatNumber)));
    }

    private Runnable bulkReservation(
            UserEntity performer,
            BulkReservationSeatRequest request,
            AtomicInteger successCount,
            AtomicInteger alreadyReservedCount
    ) {
        return () -> {
            setAuth(performer.getId(), Role.PERFORMER);
            try {
                reservationSeatService.executeBulk(request);
                successCount.incrementAndGet();
            } catch (SeatAlreadyReservedException e) {
                alreadyReservedCount.incrementAndGet();
            } finally {
                SecurityContextHolder.clearContext();
            }
        };
    }

    private Runnable bulkReservationWithLimit(
            UserEntity performer,
            BulkReservationSeatRequest request,
            AtomicInteger successCount,
            AtomicInteger limitExceededCount
    ) {
        return () -> {
            setAuth(performer.getId(), Role.PERFORMER);
            try {
                reservationSeatService.executeBulk(request);
                successCount.incrementAndGet();
            } catch (SeatReservationLimitExceededException e) {
                limitExceededCount.incrementAndGet();
            } finally {
                SecurityContextHolder.clearContext();
            }
        };
    }

    private List<Throwable> runConcurrently(Runnable... tasks) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.length);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(tasks.length);
        try {
            for (Runnable task : tasks) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        task.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Throwable throwable) {
                        exceptions.add(throwable);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }
            startLatch.countDown();
            assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdown();
        }
        return exceptions;
    }
}
