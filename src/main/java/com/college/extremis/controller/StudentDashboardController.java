package com.college.extremis.controller;

import com.college.extremis.entity.AttendanceSession;
import com.college.extremis.entity.Student;
import com.college.extremis.entity.TimetableSlot;
import com.college.extremis.entity.User;
import com.college.extremis.repository.AttendanceSessionRepository;
import com.college.extremis.repository.NotificationRepository;
import com.college.extremis.repository.StudentRepository;
import com.college.extremis.repository.TimetableSlotRepository;
import com.college.extremis.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentDashboardController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TimetableSlotRepository timetableSlotRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final NotificationRepository notificationRepository;

    public StudentDashboardController(UserRepository userRepository,
                                      StudentRepository studentRepository,
                                      TimetableSlotRepository timetableSlotRepository,
                                      AttendanceSessionRepository attendanceSessionRepository,
                                      NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.timetableSlotRepository = timetableSlotRepository;
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        Student student = user != null ? studentRepository.findByUser(user).orElse(null) : null;

        model.addAttribute("currentUser", user);
        model.addAttribute("student", student);

        if (student != null) {
            LocalDate today = LocalDate.now();
            List<TimetableSlot> todaySlots = timetableSlotRepository.findBySectionAndDayOfWeekOrderByStartTimeAsc(
                    student.getSection(), today.getDayOfWeek());
            model.addAttribute("todaySlots", todaySlots);

            List<AttendanceSession> attendanceHistory = attendanceSessionRepository
                    .findByStudentOrderBySessionDateDescCheckinTimeDesc(student);
            model.addAttribute("attendanceHistory", attendanceHistory);

            model.addAttribute("notifications", notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
        }

        return "student/dashboard";
    }
}
