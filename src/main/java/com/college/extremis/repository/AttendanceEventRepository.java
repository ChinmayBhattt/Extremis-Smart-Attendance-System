package com.college.extremis.repository;

import com.college.extremis.entity.AttendanceEvent;
import com.college.extremis.entity.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findBySessionIdOrderByScheduledTimeAsc(Long sessionId);
    List<AttendanceEvent> findBySessionIdAndStatus(Long sessionId, EventStatus status);
    List<AttendanceEvent> findByStatusAndScheduledTimeLessThanEqual(EventStatus status, LocalDateTime time);
}
