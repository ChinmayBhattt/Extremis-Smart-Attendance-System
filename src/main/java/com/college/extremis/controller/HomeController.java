package com.college.extremis.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            String role = auth.getAuthority();
            switch (role) {
                case "ROLE_ADMIN":
                    return "redirect:/admin/dashboard";
                case "ROLE_CLASS_TEACHER":
                    return "redirect:/teacher/dashboard";
                case "ROLE_FACULTY":
                    return "redirect:/faculty/dashboard";
                case "ROLE_STUDENT":
                    return "redirect:/student/dashboard";
            }
        }
        return "redirect:/login";
    }
}
