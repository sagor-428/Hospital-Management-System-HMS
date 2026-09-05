package com.hospital;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PrescriptionController {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final PrescriptionService prescriptionService;
    private final UserRepository userRepository;


    // =========================================================
    // DOCTOR: SHOW CREATE / EDIT PRESCRIPTION FORM
    // =========================================================

    @GetMapping("/doctor/appointments/{id}/prescription")
    public String showPrescriptionForm(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        Doctor doctor = doctorService
                .findByUserEmail(authentication.getName());

        Appointment appointment = appointmentService.findById(id);

        if (!appointment.getDoctor()
                .getId()
                .equals(doctor.getId())) {

            throw new RuntimeException(
                    "You are not allowed to access this appointment"
            );
        }

        model.addAttribute("appointment", appointment);

        Prescription prescription =
                prescriptionService.existsByAppointmentId(id)
                        ? prescriptionService.findByAppointmentId(id)
                        : Prescription.builder()
                        .medicines(new ArrayList<>())
                        .build();

        model.addAttribute("prescription", prescription);

        return "doctor/prescription-form";
    }


    // =========================================================
    // DOCTOR: SAVE PRESCRIPTION
    // =========================================================

    @PostMapping("/doctor/appointments/{id}/prescription")
    public String savePrescription(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam String diagnosis,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) List<String> medicineName,
            @RequestParam(required = false) List<String> dosage,
            @RequestParam(required = false) List<String> duration,
            @RequestParam(required = false) List<String> instructions) {

        Doctor doctor = doctorService
                .findByUserEmail(authentication.getName());

        Appointment appointment = appointmentService.findById(id);

        if (!appointment.getDoctor()
                .getId()
                .equals(doctor.getId())) {

            throw new RuntimeException(
                    "You are not allowed to access this appointment"
            );
        }

        List<PrescriptionMedicine> medicines = new ArrayList<>();

        if (medicineName != null) {

            for (int i = 0; i < medicineName.size(); i++) {

                String name = medicineName.get(i);

                if (name == null || name.isBlank()) {
                    continue;
                }

                medicines.add(
                        PrescriptionMedicine.builder()
                                .medicineName(name.trim())
                                .dosage(valueAt(dosage, i))
                                .duration(valueAt(duration, i))
                                .instructions(valueAt(instructions, i))
                                .build()
                );
            }
        }

        prescriptionService.createOrUpdate(
                appointment,
                diagnosis,
                notes,
                medicines
        );

        return "redirect:/doctor/appointments";
    }


    // =========================================================
    // PATIENT: VIEW PRESCRIPTION
    // =========================================================

    @GetMapping("/patient/appointments/{id}/prescription")
    public String viewPrescription(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        Patient patient = getLoggedInPatient(authentication);

        Appointment appointment = appointmentService.findById(id);

        if (!appointment.getPatient()
                .getId()
                .equals(patient.getId())) {

            throw new RuntimeException(
                    "You are not allowed to view this prescription"
            );
        }

        model.addAttribute("appointment", appointment);

        if (prescriptionService.existsByAppointmentId(id)) {

            model.addAttribute(
                    "prescription",
                    prescriptionService.findByAppointmentId(id)
            );
        }

        return "patient/prescription-view";
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private String valueAt(List<String> values, int index) {

        if (values == null || values.size() <= index) {
            return null;
        }

        String value = values.get(index);

        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private Patient getLoggedInPatient(Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        return patientService.findByUserId(user.getId());
    }
}
