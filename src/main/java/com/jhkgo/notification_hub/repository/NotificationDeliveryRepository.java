package com.jhkgo.notification_hub.repository;

import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import com.jhkgo.notification_hub.domain.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {

    List<NotificationDelivery> findByStatus(DeliveryStatus status);

    @Modifying
    @Query("""
        UPDATE NotificationDelivery d
        SET d.status = :status
        WHERE d.id IN :ids
        """)
    void updateStatusIn(@Param("ids") List<Long> ids, @Param("status") DeliveryStatus status);
}
