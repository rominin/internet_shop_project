package ru.practicum.java.internet_shop_project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.java.internet_shop_project.entity.Cart;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findById(Long cartId);

    @Query("SELECT c FROM Cart c WHERE c.id = 1")
    Optional<Cart> findSingletonCart();

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.id = :cartId")
    void clearCart(@Param("cartId") Long cartId);

}
