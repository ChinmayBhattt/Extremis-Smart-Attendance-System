package com.college.extremis.controller;

import com.college.extremis.entity.Faculty;
import com.college.extremis.entity.TimetableSlot;
import com.college.extremis.entity.User;
import com.college.extremis.repository.AttendanceSessionRepository;
import com.college.extremis.repository.FacultyRepository;
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
@RequestMapping("/faculty")
public class FacultyDashboardController {

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final TimetableSlotRepository timetableSlotRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;

    public FacultyDashboardController(UserRepository userRepository,
                                      FacultyRepository facultyRepository,
                                      TimetableSlotRepository timetableSlotRepository,
                                      AttendanceSessionRepository attendanceSessionRepository) {
        this.userRepository = userRepository;
        this.facultyRepository = facultyRepository;
        this.timetableSlotRepository = timetableSlotRepository;
        this.attendanceSessionRepository = attendanceSessionRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        Faculty faculty = user != null ? facultyRepository.findByUser(user).orElse(null) : null;

        model.addAttribute("currentUser", user);
        model.addAttribute("faculty", faculty);

        if (user != null) {
            LocalDate today = LocalDate.now();
            List<TimetableSlot> todaySlots = timetableSlotRepository.findByFacultyUserAndDayOfWeekOrderByStartTimeAsc(
                    user, today.getDayOfWeek());
            model.addAttribute("todaySlots", todaySlots);

            List<TimetableSlot> allMySlots = timetableSlotRepository.findByFacultyUserIdOrderByDayOfWeekAscStartTimeAsc(user.getId());
            model.addAttribute("allMySlots", allMySlots);

            var todaySessions = attendanceSessionRepository.findByFacultyUserIdAndDate(user.getId(), today);
            model.addAttribute("todaySessions", todaySessions);
        }

        return "faculty/dashboard";
    }
}
