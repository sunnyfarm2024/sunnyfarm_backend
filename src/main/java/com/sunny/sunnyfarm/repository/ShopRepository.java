package com.sunny.sunnyfarm.repository;

import com.sunny.sunnyfarm.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Integer> {

    @Query("SELECT s FROM Shop s")
    List<Shop> getItemList();

    @Query("SELECT i.item.category FROM Inventory i WHERE i.item.itemId = :itemId and i.user.userId = :userId")
    Shop.ItemCategory findByCategory(@Param("userId") int userId, @Param("itemId") int itemId);
}