package com.sunny.sunnyfarm.repository;

import com.sunny.sunnyfarm.dto.FarmDto;
import com.sunny.sunnyfarm.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Integer> {
    @Query("SELECT new com.sunny.sunnyfarm.dto.FarmDto( " +
            "  CASE WHEN f.sign IS NOT NULL THEN s1.itemImageUrl ELSE NULL END, " +
            "  CASE WHEN f.corner IS NOT NULL THEN s2.itemImageUrl ELSE NULL END, " +
            "  f.farmDescription, " +
            "  f.gnomeEndsAt, " +
            "  f.gnomeImageUrl " +
            ") " +
            "FROM Farm f " +
            "LEFT JOIN Shop s1 ON f.sign.itemId = s1.itemId " +
            "LEFT JOIN Shop s2 ON f.corner.itemId = s2.itemId " +
            "WHERE f.farmId = :farmId")
    FarmDto findFarmDto(@Param("farmId") Integer farmId);

    @Modifying
    @Transactional
    @Query("UPDATE Farm f " +
            "SET f.leftPlant.userPlantId = CASE WHEN f.leftPlant.userPlantId = :userPlantId THEN NULL ELSE f.leftPlant.userPlantId END, " +
            "    f.centerPlant.userPlantId = CASE WHEN f.centerPlant.userPlantId = :userPlantId THEN NULL ELSE f.centerPlant.userPlantId END, " +
            "    f.rightPlant.userPlantId = CASE WHEN f.rightPlant.userPlantId = :userPlantId THEN NULL ELSE f.rightPlant.userPlantId END " +
            "WHERE f.farmId = :farmId")
    void clearPlantReference(@Param("farmId") int farmId, @Param("userPlantId") int userPlantId);
}

