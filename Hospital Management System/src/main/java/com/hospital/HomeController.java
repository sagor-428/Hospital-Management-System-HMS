package com.hospital;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DoctorService doctorService;
    private final DepartmentService departmentService;


    // =========================================================
    // HOME PAGE
    // =========================================================

    @GetMapping("/")
    public String home(Model model) {

        List<Doctor> allDoctors = doctorService.findAll();

        List<Doctor> featuredDoctors = allDoctors.size() > 6
                ? allDoctors.subList(0, 6)
                : allDoctors;

        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("doctors", featuredDoctors);
        model.addAttribute("doctorCount", allDoctors.size());
        model.addAttribute("departmentCount", departmentService.findAll().size());

        return "home/index";
    }


    // =========================================================
    // ABOUT PAGE
    // =========================================================

    @GetMapping("/about")
    public String about(Model model) {

        model.addAttribute("doctorCount", doctorService.findAll().size());
        model.addAttribute("departmentCount", departmentService.findAll().size());

        return "home/about";
    }


    // =========================================================
    // CONTACT PAGE
    // =========================================================

    @GetMapping("/contact")
    public String contact() {
        return "home/contact";
    }


    // =========================================================
    // PUBLIC DOCTOR LISTING
    // =========================================================

    @GetMapping("/doctors")
    public String doctors(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Long departmentId,
            Model model) {

        List<Doctor> doctors;

        if (specialization != null && !specialization.isBlank()) {

            doctors = doctorService
                    .searchBySpecialization(specialization);

        } else if (departmentId != null) {

            doctors = doctorService
                    .findByDepartment(departmentId);

        } else {

            doctors = doctorService.findAll();
        }

        model.addAttribute("doctors", doctors);
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("specialization", specialization);
        model.addAttribute("selectedDepartment", departmentId);

        return "home/doctors";
    }


    // =========================================================
    // PUBLIC DEPARTMENT LISTING
    // =========================================================

    @GetMapping("/departments")
    public String departments(Model model) {

        model.addAttribute(
                "departments",
                departmentService.findAll()
        );

        return "home/departments";
    }
}
