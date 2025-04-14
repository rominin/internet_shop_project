package ru.practicum.java.payment_service.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.practicum.java.payment_service.entity.UserBalance;

@Repository
public interface UserBalanceRepository extends ReactiveCrudRepository<UserBalance, Long> {

    Mono<UserBalance> findByUserId(Long userId);

}
