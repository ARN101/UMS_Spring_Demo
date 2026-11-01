package com.university.management.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return getDashboardRedirect(authentication);
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Authentication authentication,
                        @RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "denied", required = false) String denied,
                        @RequestParam(value = "deleted", required = false) String deleted,
                        Model model) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            return getDashboardRedirect(authentication);
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email, password, or account is pending administrator approval.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        if (denied != null) {
            model.addAttribute("errorMessage", "Access denied: You do not have permission to view that resource.");
        }
        if (deleted != null) {
            model.addAttribute("logoutMessage", "Your account was successfully deleted.");
        }

        return "login";
    }

    private String getDashboardRedirect(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return "redirect:/admin/dashboard";
            } else if ("ROLE_TEACHER".equals(authority.getAuthority())) {
                return "redirect:/teacher/dashboard";
            } else if ("ROLE_STUDENT".equals(authority.getAuthority())) {
                return "redirect:/student/dashboard";
            }
        }
        return "redirect:/login";
    }
}
