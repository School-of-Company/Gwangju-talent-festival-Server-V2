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
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatAlreadyReservedException;
import team.startup.gwangjutalentfestival.domain.seat.exception.SeatReservationLimitExceededException;
import team.startup.gwangjutalentfestival.domain.seat.presentation.data.request.ReservationSeatRequest;
import team.startup.gwangjutalentfestival.domain.seat.repository.SeatReservationRepository;
import team.startup.gwangjutalentfestival.domain.seat.service.ReservationSeatService;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.auth.CustomUserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

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
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final long userId = testUsers.get(i).getId();
            executor.submit(() -> {
                try {
                    startLatch.await();
                    setAuth(userId, Role.USER);
                    reservationSeatService.execute(new ReservationSeatRequest("A", 1));
                    successCount.incrementAndGet();
                } catch (SeatAlreadyReservedException e) {
                    failCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                    SecurityContextHolder.clearContext();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);
    }

    @Test
    void 동일_USER_동시_예약시_한도_초과는_실패한다() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger limitExceededCount = new AtomicInteger(0);

        long userId = testUsers.get(0).getId();
        String[] sections = {"A", "A"};
        Integer[] seatNumbers = {1, 2};

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
                } finally {
                    doneLatch.countDown();
                    SecurityContextHolder.clearContext();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(limitExceededCount.get()).isEqualTo(1);
    }
}
