package com.jhkgo.notification_hub.repository;

import com.jhkgo.notification_hub.domain.entity.Notification;
import com.jhkgo.notification_hub.dto.NotificationListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(value = """
        select
            n.id as id,
            n.type as type,
            n.title as title,
            n.message as message,
            n.recipientId as recipientId,
            count(d.id) as totalCount,
            sum(case when d.status = 'SUCCESS' then 1 else 0 end) as succeedCount,
            sum(case when d.status = 'FAILED' then 1 else 0 end) as failedCount
        from Notification n
        left join n.deliveries d
        group by n.id, n.type, n.title, n.message, n.recipientId
    """,
        countQuery = """
        select count(n.id) from Notification n
    """)
    Page<NotificationListProjection> findAllWithSummary(Pageable pageable);
}
