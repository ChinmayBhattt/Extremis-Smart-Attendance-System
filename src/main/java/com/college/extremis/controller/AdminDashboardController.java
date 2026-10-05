package com.college.extremis.controller;

import com.college.extremis.entity.Device;
import com.college.extremis.entity.User;
import com.college.extremis.repository.*;
import com.college.extremis.service.DeviceService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final ClassroomRepository classroomRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final TimetableSlotRepository timetableSlotRepository;
    private final DeviceService deviceService;
    private final AuditLogRepository auditLogRepository;

    public AdminDashboardController(UserRepository userRepository,
                                    StudentRepository studentRepository,
                                    FacultyRepository facultyRepository,
                                    ClassroomRepository classroomRepository,
                                    DepartmentRepository departmentRepository,
                                    SubjectRepository subjectRepository,
                                    TimetableSlotRepository timetableSlotRepository,
                                    DeviceService deviceService,
                                    AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.classroomRepository = classroomRepository;
        this.departmentRepository = departmentRepository;
        this.subjectRepository = subjectRepository;
        this.timetableSlotRepository = timetableSlotRepository;
        this.deviceService = deviceService;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        model.addAttribute("currentUser", user);

        model.addAttribute("totalStudents", studentRepository.count());
        model.addAttribute("totalFaculty", facultyRepository.count());
        model.addAttribute("totalClassrooms", classroomRepository.count());
        model.addAttribute("totalDepartments", departmentRepository.count());
        model.addAttribute("totalSubjects", subjectRepository.count());
        model.addAttribute("totalSlots", timetableSlotRepository.count());

        List<Device> pendingDevices = deviceService.getPendingDeviceApprovals();
        model.addAttribute("pendingDevices", pendingDevices);
        model.addAttribute("pendingDevicesCount", pendingDevices.size());

        model.addAttribute("recentAuditLogs", auditLogRepository.findAllByOrderByTimestampDesc().stream().limit(10).toList());

        return "admin/dashboard";
    }
}
