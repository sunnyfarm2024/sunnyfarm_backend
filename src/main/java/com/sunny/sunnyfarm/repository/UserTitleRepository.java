package com.sunny.sunnyfarm.repository;

import com.sunny.sunnyfarm.entity.UserTitle;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTitleRepository extends JpaRepository<UserTitle,Integer> {
    @Query("SELECT ut FROM UserTitle ut WHERE ut.user.userId = :userId AND ut.title.titleId = :titleId")
    Optional<UserTitle> findByUserIdAndTitleId(@Param("userId") Integer userId, @Param("titleId") Integer titleId);

    @Modifying
    @Transactional
    @Query("UPDATE UserTitle ut SET ut.isTitleCompleted = true, ut.isActive = true WHERE ut.user.userId = :userId AND ut.title.titleId = :titleId")
    void updateTitleStatus(@Param("userId") Integer userId, @Param("titleId") Integer titleId);

}
