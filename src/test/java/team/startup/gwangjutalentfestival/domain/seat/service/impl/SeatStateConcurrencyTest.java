package team.startup.gwangjutalentfestival.domain.seat.service.impl;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatBanEntity;
import team.startup.gwangjutalentfestival.domain.seat.entity.SeatEntity;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyBannedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatBanNotFoundException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatBannedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatNotFoundException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.BanSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.CancelSeatBanRequest;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatBanRepository;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.CancelSeatReservationService;
import team.startup.gwangjutalentfestival.domain.seat.service.ReservationSeatService;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.BanSeatService;
import team.startup.gwangjutalentfestival.domain.seat.service.admin.CancelSeatBanService;
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
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SeatStateConcurrencyTest {

    private static final String SECTION = "A";
    private static final int NUMBER = 33;

    @MockBean
    Sheets sheets;

    @MockBean
    Drive drive;

    @Autowired
    private ReservationSeatService reservationSeatService;

    @Autowired
    private CancelSeatReservationService cancelSeatReservationService;

    @Autowired
    private BanSeatService banSeatService;

    @Autowired
    private CancelSeatBanService cancelSeatBanService;

    @Autowired
    private SeatReservationRepository seatReservationRepository;

    @Autowired
    private SeatBanRepository seatBanRepository;

    @Autowired
    private UserRepository userRepository;

    private final List<UserEntity> users = new ArrayList<>();

    @AfterEach
    void tearDown() {
        seatReservationRepository.deleteAll();
        seatBanRepository.deleteAll();
        userRepository.deleteAll(users);
    }

    @Test
    void 빈_좌석의_예약과_차단이_동시에_성공하지_않는다() throws InterruptedException {
        UserEntity user = saveUser(0);
        AtomicInteger reservationSuccess = new AtomicInteger();
        AtomicInteger banSuccess = new AtomicInteger();

        List<Throwable> unexpected = runConcurrently(
                () -> {
                    setAuth(user);
                    try {
                        reservationSeatService.execute(new ReservationSeatRequest(SECTION, NUMBER));
                        reservationSuccess.incrementAndGet();
                    } catch (SeatBannedException ignored) {
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                },
                () -> {
                    try {
                        banSeatService.execute(new BanSeatRequest(SECTION, NUMBER));
                        banSuccess.incrementAndGet();
                    } catch (SeatAlreadyReservedException ignored) {
                    }
                }
        );

        assertThat(unexpected).isEmpty();
        assertThat(reservationSuccess.get() + banSuccess.get()).isEqualTo(1);
        assertThat(seatReservationRepository.existsBySeatSectionAndSeatNumber(SECTION, NUMBER))
                .isNotEqualTo(seatBanRepository.existsBySeatSectionAndSeatNumber(SECTION, NUMBER));
    }

    @Test
    void 같은_좌석을_동시에_차단하면_한_건만_성공한다() throws InterruptedException {
        AtomicInteger success = new AtomicInteger();
        AtomicInteger duplicate = new AtomicInteger();
        Runnable ban = () -> {
            try {
                banSeatService.execute(new BanSeatRequest(SECTION, NUMBER));
                success.incrementAndGet();
            } catch (SeatAlreadyBannedException ignored) {
                duplicate.incrementAndGet();
            }
        };

        List<Throwable> unexpected = runConcurrently(ban, ban);

        assertThat(unexpected).isEmpty();
        assertThat(success.get()).isEqualTo(1);
        assertThat(duplicate.get()).isEqualTo(1);
        assertThat(seatBanRepository.count()).isEqualTo(1);
    }

    @Test
    void 같은_예약을_동시에_취소하면_한_건만_성공한다() throws InterruptedException {
        UserEntity user = saveUser(1);
        seatReservationRepository.saveAndFlush(SeatEntity.builder()
                .seatSection(SECTION)
                .seatNumber(NUMBER)
                .user(user)
                .build());
        AtomicInteger success = new AtomicInteger();
        AtomicInteger notFound = new AtomicInteger();
        Runnable cancel = () -> {
            setAuth(user);
            try {
                cancelSeatReservationService.execute();
                success.incrementAndGet();
            } catch (SeatNotFoundException ignored) {
                notFound.incrementAndGet();
            } finally {
                SecurityContextHolder.clearContext();
            }
        };

        List<Throwable> unexpected = runConcurrently(cancel, cancel);

        assertThat(unexpected).isEmpty();
        assertThat(success.get()).isEqualTo(1);
        assertThat(notFound.get()).isEqualTo(1);
        assertThat(seatReservationRepository.count()).isZero();
    }

    @Test
    void 같은_차단을_동시에_해제하면_한_건만_성공한다() throws InterruptedException {
        seatBanRepository.saveAndFlush(SeatBanEntity.builder()
                .seatSection(SECTION)
                .seatNumber(NUMBER)
                .build());
        AtomicInteger success = new AtomicInteger();
        AtomicInteger notFound = new AtomicInteger();
        Runnable cancelBan = () -> {
            try {
                cancelSeatBanService.execute(new CancelSeatBanRequest(SECTION, NUMBER));
                success.incrementAndGet();
            } catch (SeatBanNotFoundException ignored) {
                notFound.incrementAndGet();
            }
        };

        List<Throwable> unexpected = runConcurrently(cancelBan, cancelBan);

        assertThat(unexpected).isEmpty();
        assertThat(success.get()).isEqualTo(1);
        assertThat(notFound.get()).isEqualTo(1);
        assertThat(seatBanRepository.count()).isZero();
    }

    @Test
    void 서로_다른_좌석의_예약은_서로를_막지_않는다() throws InterruptedException {
        List<UserEntity> concurrentUsers = IntStream.range(0, 5)
                .mapToObj(this::saveUser)
                .toList();
        AtomicInteger success = new AtomicInteger();
        Runnable[] reservations = IntStream.range(0, concurrentUsers.size())
                .mapToObj(index -> (Runnable) () -> {
                    setAuth(concurrentUsers.get(index));
                    try {
                        reservationSeatService.execute(new ReservationSeatRequest(SECTION, NUMBER + index));
                        success.incrementAndGet();
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                })
                .toArray(Runnable[]::new);

        List<Throwable> unexpected = runConcurrently(reservations);

        assertThat(unexpected).isEmpty();
        assertThat(success.get()).isEqualTo(5);
        assertThat(seatReservationRepository.count()).isEqualTo(5);
    }

    @Test
    void 예약_취소와_차단이_경쟁해도_두_상태가_함께_남지_않는다() throws InterruptedException {
        UserEntity user = saveUser(10);
        seatReservationRepository.saveAndFlush(SeatEntity.builder()
                .seatSection(SECTION)
                .seatNumber(NUMBER)
                .user(user)
                .build());
        AtomicInteger cancelSuccess = new AtomicInteger();

        List<Throwable> unexpected = runConcurrently(
                () -> {
                    setAuth(user);
                    try {
                        cancelSeatReservationService.execute();
                        cancelSuccess.incrementAndGet();
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                },
                () -> {
                    try {
                        banSeatService.execute(new BanSeatRequest(SECTION, NUMBER));
                    } catch (SeatAlreadyReservedException ignored) {
                    }
                }
        );

        assertThat(unexpected).isEmpty();
        assertThat(cancelSuccess.get()).isEqualTo(1);
        assertThat(seatReservationRepository.existsBySeatSectionAndSeatNumber(SECTION, NUMBER)).isFalse();
    }

    @Test
    void 차단_해제와_예약이_경쟁해도_차단과_예약이_함께_남지_않는다() throws InterruptedException {
        UserEntity user = saveUser(11);
        seatBanRepository.saveAndFlush(SeatBanEntity.builder()
                .seatSection(SECTION)
                .seatNumber(NUMBER)
                .build());
        AtomicInteger cancelBanSuccess = new AtomicInteger();

        List<Throwable> unexpected = runConcurrently(
                () -> {
                    cancelSeatBanService.execute(new CancelSeatBanRequest(SECTION, NUMBER));
                    cancelBanSuccess.incrementAndGet();
                },
                () -> {
                    setAuth(user);
                    try {
                        reservationSeatService.execute(new ReservationSeatRequest(SECTION, NUMBER));
                    } catch (SeatBannedException ignored) {
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                }
        );

        assertThat(unexpected).isEmpty();
        assertThat(cancelBanSuccess.get()).isEqualTo(1);
        assertThat(seatBanRepository.existsBySeatSectionAndSeatNumber(SECTION, NUMBER)).isFalse();
    }

    private UserEntity saveUser(int suffix) {
        UserEntity user = userRepository.save(UserEntity.builder()
                .phoneNumber("0107777" + String.format("%04d", suffix))
                .role(Role.USER)
                .build());
        users.add(user);
        return user;
    }

    private void setAuth(UserEntity user) {
        CustomUserDetails details = CustomUserDetails.fromToken(user.getId(), user.getRole());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                details, null, details.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    private List<Throwable> runConcurrently(Runnable... tasks) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.length);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(tasks.length);
        List<Throwable> unexpected = Collections.synchronizedList(new ArrayList<>());
        try {
            for (Runnable task : tasks) {
                executor.submit(() -> {
                    try {
                        start.await();
                        task.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Throwable throwable) {
                        unexpected.add(throwable);
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdown();
        }
        return unexpected;
    }
}
