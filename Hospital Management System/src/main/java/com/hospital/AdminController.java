package com.hospital;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DepartmentService departmentService;
    private final DoctorService doctorService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    // =========================
    // ADMIN DASHBOARD
    // =========================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute(
                "departmentCount",
                departmentService.findAll().size()
        );

        model.addAttribute(
                "doctorCount",
                doctorService.findAll().size()
        );

        model.addAttribute(
                "patientCount",
                userRepository.findAll()
                        .stream()
                        .filter(user -> user.getRole() == Role.PATIENT)
                        .count()
        );

        return "admin/dashboard";
    }


    // =========================
    // DEPARTMENT LIST
    // =========================

    @GetMapping("/departments")
    public String departments(
            @RequestParam(required = false) String error,
            Model model) {

        model.addAttribute(
                "departments",
                departmentService.findAll()
        );

        if (error != null) {
            model.addAttribute(
                    "error",
                    "Cannot delete this department while it still has " +
                            "doctors assigned to it. Reassign or remove " +
                            "those doctors first."
            );
        }

        return "admin/departments";
    }


    // =========================
    // ADD DEPARTMENT FORM
    // =========================

    @GetMapping("/departments/add")
    public String addDepartment(Model model) {

        model.addAttribute(
                "department",
                new Department()
        );

        return "admin/department-form";
    }


    // =========================
    // SAVE DEPARTMENT
    // =========================

    @PostMapping("/departments/save")
    public String saveDepartment(
            @ModelAttribute Department department) {

        departmentService.save(department);

        return "redirect:/admin/departments";
    }


    // =========================
    // DELETE DEPARTMENT
    // =========================

    @PostMapping("/departments/delete/{id}")
    public String deleteDepartment(
            @PathVariable Long id) {

        try {

            departmentService.deleteById(id);

        } catch (DataIntegrityViolationException e) {

            return "redirect:/admin/departments?error=true";
        }

        return "redirect:/admin/departments";
    }


    // =========================
    // DOCTOR LIST
    // =========================

    @GetMapping("/doctors")
    public String doctors(
            @RequestParam(required = false) String error,
            Model model) {

        model.addAttribute(
                "doctors",
                doctorService.findAll()
        );

        if (error != null) {
            model.addAttribute(
                    "error",
                    "Cannot delete this doctor while they still have " +
                            "appointments or a schedule on record."
            );
        }

        return "admin/doctors";
    }


    // =========================
    // ADD DOCTOR FORM
    // =========================

    @GetMapping("/doctors/add")
    public String addDoctor(Model model) {

        model.addAttribute(
                "doctor",
                new Doctor()
        );

        model.addAttribute(
                "departments",
                departmentService.findAll()
        );

        return "admin/doctor-form";
    }


    // =========================
    // SAVE DOCTOR
    // =========================

    @PostMapping("/doctors/save")
    public String saveDoctor(
            @ModelAttribute Doctor doctor,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String phone,
            Model model) {

        // Check whether email already exists
        if (userRepository.existsByEmail(email)) {

            model.addAttribute(
                    "error",
                    "A user with this email already exists."
            );

            model.addAttribute(
                    "departments",
                    departmentService.findAll()
            );

            return "admin/doctor-form";
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phone(phone)
                .role(Role.DOCTOR)
                .enabled(true)
                .build();

        userRepository.save(user);

        doctor.setUser(user);

        doctorService.save(doctor);

        return "redirect:/admin/doctors";
    }


    // =========================
    // DELETE DOCTOR
    // =========================

    @PostMapping("/doctors/delete/{id}")
    public String deleteDoctor(
            @PathVariable Long id) {

        try {

            Doctor doctor = doctorService.findById(id);

            doctorService.deleteById(id);

            // Also disable the login account tied to this doctor
            // so it can't be used to sign in anymore.
            User user = doctor.getUser();
            user.setEnabled(false);
            userRepository.save(user);

        } catch (DataIntegrityViolationException e) {

            return "redirect:/admin/doctors?error=true";
        }

        return "redirect:/admin/doctors";
    }
}