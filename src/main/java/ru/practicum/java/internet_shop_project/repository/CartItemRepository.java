package ru.practicum.java.internet_shop_project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.java.internet_shop_project.entity.CartItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.product.id = :productId AND ci.cart.id = 1")
    Optional<CartItem> findInSingletonCartByProductId(Long productId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = 1")
    List<CartItem> findInSingletonCart();

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = 1 AND ci.product.id = :productId")
    void removeItemFromSingletonCart(@Param("productId") Long productId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = 1")
    void clearCartItemsInSingletonCart();

}
