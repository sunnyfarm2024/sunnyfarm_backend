package com.sunny.sunnyfarm.repository;

import com.sunny.sunnyfarm.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    @Query("SELECT i.item.itemId, " +
            "i.slotNumber, " +
            "i.item.itemImageUrl, " +
            "i.item.itemName, " +
            "i.item.itemDescription, " +
            "i.item.category " +
            "FROM Inventory i " +
            "WHERE i.user.userId = :userId " +
            "AND i.item IS NOT NULL")
    List<Object[]> findBasicInventoryData(@Param("userId") Integer userId);

    @Query("SELECT f.leftPlant.fertilizerEndsAt, f.centerPlant.fertilizerEndsAt, f.rightPlant.fertilizerEndsAt " +
            "FROM Farm f " +
            "WHERE f.user.userId = :userId")
    List<Object[]> findFertilizerTimes(@Param("userId") Integer userId);

    @Query("SELECT f.gnomeEndsAt " +
            "FROM Farm f " +
            "WHERE f.user.userId = :userId")
    LocalDateTime findGnomeEndsAt(@Param("userId") Integer userId);

    @Query("SELECT f.leftPlant.plant.plantId, f.centerPlant.plant.plantId, f.rightPlant.plant.plantId " +
            "FROM Farm f " +
            "LEFT JOIN f.leftPlant lp " +
            "LEFT JOIN f.centerPlant cp " +
            "LEFT JOIN f.rightPlant rp " +
            "WHERE f.user.userId = :userId")
    List<Object[]> findPlantIdsInFarm(@Param("userId") Integer userId);

    @Query("SELECT i FROM Inventory i WHERE i.user.userId = :userId AND i.slotNumber = :slotNumber")
    Optional<Inventory> findByUserIdAndSlotNumber(@Param("userId") Integer userId, @Param("slotNumber") int slotNumber);

    @Query("SELECT i FROM Inventory i WHERE i.user.userId = :userId AND i.item IS NULL")
    Optional<Inventory> findFirstByUserIdAndItemIsNull(Integer userId);
}
