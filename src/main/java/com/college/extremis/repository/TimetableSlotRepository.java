package com.college.extremis.repository;

import com.college.extremis.entity.Section;
import com.college.extremis.entity.TimetableSlot;
import com.college.extremis.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, Long> {
    List<TimetableSlot> findBySectionAndDayOfWeekOrderByStartTimeAsc(Section section, DayOfWeek dayOfWeek);
    List<TimetableSlot> findBySectionIdAndDayOfWeekOrderByStartTimeAsc(Long sectionId, DayOfWeek dayOfWeek);
    List<TimetableSlot> findByFacultyUserAndDayOfWeekOrderByStartTimeAsc(User facultyUser, DayOfWeek dayOfWeek);
    List<TimetableSlot> findByFacultyUserIdOrderByDayOfWeekAscStartTimeAsc(Long facultyUserId);
    List<TimetableSlot> findBySectionIdOrderByDayOfWeekAscStartTimeAsc(Long sectionId);
    List<TimetableSlot> findByDayOfWeekOrderByStartTimeAsc(DayOfWeek dayOfWeek);
}
