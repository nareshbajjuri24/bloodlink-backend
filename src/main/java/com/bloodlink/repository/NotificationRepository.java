package com.bloodlink.repository;

import com.bloodlink.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndSeenFalse(Long userId);
    void deleteByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("update Notification n set n.seen = true where n.userId = :uid")
    void markAllSeen(@Param("uid") Long userId);
}
