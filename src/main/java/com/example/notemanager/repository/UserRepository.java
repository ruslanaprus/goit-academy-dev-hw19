package com.example.notemanager.repository;

import com.example.notemanager.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);
    boolean existsByUserName(String userName);
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = u.failedAttempts + 1, " +
            "u.accountLockedUntil = CASE WHEN u.failedAttempts + 1 >= :maxFailedAttempts THEN :lockTime ELSE u.accountLockedUntil END " +
            "WHERE u.id = :userId")
    void incrementFailedAttempts(@Param("userId") Long userId,
                                 @Param("maxFailedAttempts") int maxFailedAttempts,
                                 @Param("lockTime") LocalDateTime lockTime);
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = 0, u.accountLockedUntil = NULL WHERE u.id = :userId")
    void resetFailedAttempts(@Param("userId") Long userId);

}
