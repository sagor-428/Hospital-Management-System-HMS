package com.hospital;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientDoctorController {

    private final DoctorService doctorService;
    private final DepartmentService departmentService;

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

        List<Department> departments =
                departmentService.findAll();

        model.addAttribute("doctors", doctors);
        model.addAttribute("departments", departments);
        model.addAttribute("specialization", specialization);
        model.addAttribute("selectedDepartment", departmentId);

        return "patient/doctors";
    }


    @GetMapping("/doctors/{id}")
    public String doctorDetails(
            @PathVariable Long id,
            Model model) {

        Doctor doctor = doctorService.findById(id);

        model.addAttribute("doctor", doctor);

        return "patient/doctor-details";
    }
}