package com.college.extremis.repository;

import com.college.extremis.entity.AttendanceSession;
import com.college.extremis.entity.Student;
import com.college.extremis.entity.TimetableSlot;
import com.college.extremis.entity.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    Optional<AttendanceSession> findByTimetableSlotAndStudentAndSessionDate(
            TimetableSlot timetableSlot, Student student, LocalDate sessionDate);

    Optional<AttendanceSession> findByTimetableSlotIdAndStudentIdAndSessionDate(
            Long timetableSlotId, Long studentId, LocalDate sessionDate);

    List<AttendanceSession> findByStudentOrderBySessionDateDescCheckinTimeDesc(Student student);

    List<AttendanceSession> findByStudentIdOrderBySessionDateDesc(Long studentId);

    List<AttendanceSession> findBySessionDate(LocalDate sessionDate);

    List<AttendanceSession> findByTimetableSlotIdAndSessionDate(Long timetableSlotId, LocalDate sessionDate);

    List<AttendanceSession> findByStatus(AttendanceStatus status);

    List<AttendanceSession> findByStatusIn(List<AttendanceStatus> statuses);

    @Query("SELECT s FROM AttendanceSession s WHERE s.student.id = :studentId AND s.timetableSlot.subject.id = :subjectId")
    List<AttendanceSession> findByStudentIdAndSubjectId(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);

    @Query("SELECT s FROM AttendanceSession s WHERE s.timetableSlot.section.id = :sectionId AND s.sessionDate = :sessionDate")
    List<AttendanceSession> findBySectionIdAndDate(@Param("sectionId") Long sectionId, @Param("sessionDate") LocalDate sessionDate);

    @Query("SELECT s FROM AttendanceSession s WHERE s.timetableSlot.facultyUser.id = :facultyUserId AND s.sessionDate = :sessionDate")
    List<AttendanceSession> findByFacultyUserIdAndDate(@Param("facultyUserId") Long facultyUserId, @Param("sessionDate") LocalDate sessionDate);
}
