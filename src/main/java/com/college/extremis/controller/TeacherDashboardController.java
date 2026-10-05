package com.college.extremis.controller;

import com.college.extremis.entity.Section;
import com.college.extremis.entity.Student;
import com.college.extremis.entity.User;
import com.college.extremis.repository.AttendanceSessionRepository;
import com.college.extremis.repository.SectionRepository;
import com.college.extremis.repository.StudentRepository;
import com.college.extremis.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/teacher")
public class TeacherDashboardController {

    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final StudentRepository studentRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;

    public TeacherDashboardController(UserRepository userRepository,
                                      SectionRepository sectionRepository,
                                      StudentRepository studentRepository,
                                      AttendanceSessionRepository attendanceSessionRepository) {
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.studentRepository = studentRepository;
        this.attendanceSessionRepository = attendanceSessionRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        model.addAttribute("currentUser", user);

        if (user != null) {
            Section assignedSection = sectionRepository.findByClassTeacherUser(user).orElse(null);
            model.addAttribute("assignedSection", assignedSection);

            if (assignedSection != null) {
                List<Student> students = studentRepository.findBySection(assignedSection);
                model.addAttribute("students", students);
                model.addAttribute("totalClassStudents", students.size());
            }
        }

        return "teacher/dashboard";
    }
}
