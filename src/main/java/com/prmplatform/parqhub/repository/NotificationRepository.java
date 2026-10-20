package com.prmplatform.parqhub.repository;

import com.prmplatform.parqhub.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByTimestampDesc(Long userId);
    List<Notification> findByAdminIdOrderByTimestampDesc(Long adminId);
    long countByType(Notification.NotificationType type);

    @Query("SELECT n FROM Notification n WHERE LOWER(n.description) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(n.type) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Notification> findByDescriptionContainingIgnoreCaseOrTypeContainingIgnoreCase(@Param("search") String search, @Param("search") String search2, Pageable pageable);



}


