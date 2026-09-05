package com.hospital;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientDashboardController {

    private final UserRepository userRepository;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    @GetMapping("/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model) {

        Patient patient =
                getLoggedInPatient(authentication);


        model.addAttribute(
                "patient",
                patient
        );


        model.addAttribute(
                "appointments",
                appointmentService.findByPatient(
                        patient.getId()
                )
        );


        return "patient/dashboard";
    }


    // =========================================================
    // MY PROFILE
    // =========================================================

    @GetMapping("/profile")
    public String profile(
            Authentication authentication,
            Model model) {

        Patient patient =
                getLoggedInPatient(authentication);

        model.addAttribute("patient", patient);

        return "patient/profile";
    }


    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    @PostMapping("/profile")
    public String updateProfile(
            Authentication authentication,
            @RequestParam String name,
            @RequestParam String phone,
            @RequestParam(required = false) LocalDate dateOfBirth,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String newPassword,
            Model model) {

        Patient patient =
                getLoggedInPatient(authentication);

        User user = patient.getUser();
        user.setName(name);
        user.setPhone(phone);

        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        userService.save(user);

        patient.setDateOfBirth(dateOfBirth);
        patient.setGender(gender);
        patient.setBloodGroup(bloodGroup);
        patient.setAddress(address);

        patientService.save(patient);

        model.addAttribute("patient", patient);
        model.addAttribute("success", "Profile updated successfully.");

        return "patient/profile";
    }


    // =========================================================
    // GET LOGGED-IN PATIENT
    // =========================================================

    private Patient getLoggedInPatient(
            Authentication authentication) {

        String email =
                authentication.getName();


        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );


        return patientService
                .findByUserId(
                        user.getId()
                );
    }
}