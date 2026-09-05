package com.hospital;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientAppointmentController {

    private final UserRepository userRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;


    // =========================================================
    // BOOK APPOINTMENT PAGE
    // =========================================================

    @GetMapping("/book-appointment")
    public String bookAppointment(
            @RequestParam Long doctorId,
            @RequestParam(required = false) LocalDate date,
            Authentication authentication,
            Model model) {

        Patient patient =
                getLoggedInPatient(authentication);

        Doctor doctor =
                doctorService.findById(doctorId);


        // If date is not selected
        if (date == null) {

            date = LocalDate.now();
        }


        List<LocalTime> availableSlots =
                appointmentService.getAvailableSlots(
                        doctor,
                        date
                );


        model.addAttribute(
                "patient",
                patient
        );

        model.addAttribute(
                "doctor",
                doctor
        );

        model.addAttribute(
                "selectedDate",
                date
        );

        model.addAttribute(
                "availableSlots",
                availableSlots
        );


        return "patient/book-appointment";
    }


    // =========================================================
    // LOAD AVAILABLE SLOTS FOR DATE
    // =========================================================

    @GetMapping("/available-slots")
    public String availableSlots(
            @RequestParam Long doctorId,
            @RequestParam LocalDate date,
            Authentication authentication,
            Model model) {

        Patient patient =
                getLoggedInPatient(authentication);

        Doctor doctor =
                doctorService.findById(doctorId);


        List<LocalTime> availableSlots =
                appointmentService.getAvailableSlots(
                        doctor,
                        date
                );


        model.addAttribute(
                "patient",
                patient
        );

        model.addAttribute(
                "doctor",
                doctor
        );

        model.addAttribute(
                "selectedDate",
                date
        );

        model.addAttribute(
                "availableSlots",
                availableSlots
        );


        return "patient/book-appointment";
    }


    // =========================================================
    // SAVE APPOINTMENT
    // =========================================================

    @PostMapping("/book-appointment")
    public String saveAppointment(
            @RequestParam Long doctorId,
            @RequestParam LocalDate appointmentDate,
            @RequestParam LocalTime appointmentTime,
            @RequestParam(required = false) String reason,
            Authentication authentication,
            Model model) {

        Patient patient =
                getLoggedInPatient(authentication);

        Doctor doctor =
                doctorService.findById(doctorId);


        // -----------------------------------------------------
        // Past date validation
        // -----------------------------------------------------

        if (appointmentDate.isBefore(
                LocalDate.now())) {

            model.addAttribute(
                    "error",
                    "You cannot book an appointment for a past date."
            );

            return loadBookingPage(
                    doctor,
                    appointmentDate,
                    patient,
                    model
            );
        }


        // -----------------------------------------------------
        // Check whether selected time is a valid schedule slot
        // -----------------------------------------------------

        List<LocalTime> availableSlots =
                appointmentService.getAvailableSlots(
                        doctor,
                        appointmentDate
                );


        if (!availableSlots.contains(
                appointmentTime)) {

            model.addAttribute(
                    "error",
                    "The selected time is not available."
            );

            return loadBookingPage(
                    doctor,
                    appointmentDate,
                    patient,
                    model
            );
        }


        // -----------------------------------------------------
        // Create appointment
        // -----------------------------------------------------

        Appointment appointment =
                Appointment.builder()
                        .patient(patient)
                        .doctor(doctor)
                        .appointmentDate(
                                appointmentDate
                        )
                        .appointmentTime(
                                appointmentTime
                        )
                        .reason(reason)
                        .status(
                                AppointmentStatus.PENDING
                        )
                        .build();


        try {

            appointmentService.save(
                    appointment
            );

        } catch (RuntimeException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return loadBookingPage(
                    doctor,
                    appointmentDate,
                    patient,
                    model
            );
        }


        return "redirect:/patient/appointments";
    }


    // =========================================================
    // VIEW MY APPOINTMENTS
    // =========================================================

    @GetMapping("/appointments")
    public String myAppointments(
            Authentication authentication,
            Model model) {

        Patient patient =
                getLoggedInPatient(authentication);


        List<Appointment> appointments =
                appointmentService.findByPatient(
                        patient.getId()
                );


        model.addAttribute(
                "patient",
                patient
        );

        model.addAttribute(
                "appointments",
                appointments
        );


        return "patient/appointments";
    }


    // =========================================================
    // CANCEL APPOINTMENT
    // =========================================================

    @PostMapping("/appointments/cancel/{id}")
    public String cancelAppointment(
            @PathVariable Long id,
            Authentication authentication) {

        Patient patient =
                getLoggedInPatient(authentication);


        Appointment appointment =
                appointmentService.findById(id);


        // -----------------------------------------------------
        // Security check
        // -----------------------------------------------------

        if (!appointment
                .getPatient()
                .getId()
                .equals(patient.getId())) {

            throw new RuntimeException(
                    "You are not allowed to cancel this appointment"
            );
        }


        appointmentService.cancelAppointment(
                id
        );


        return "redirect:/patient/appointments";
    }


    // =========================================================
    // GET LOGGED-IN PATIENT
    // =========================================================

    private Patient getLoggedInPatient(
            Authentication authentication) {

        String email =
                authentication.getName();


        User user =
                userRepository
                        .findByEmail(email)
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


    // =========================================================
    // LOAD BOOKING PAGE
    // =========================================================

    private String loadBookingPage(
            Doctor doctor,
            LocalDate date,
            Patient patient,
            Model model) {

        List<LocalTime> availableSlots =
                appointmentService.getAvailableSlots(
                        doctor,
                        date
                );


        model.addAttribute(
                "patient",
                patient
        );

        model.addAttribute(
                "doctor",
                doctor
        );

        model.addAttribute(
                "selectedDate",
                date
        );

        model.addAttribute(
                "availableSlots",
                availableSlots
        );


        return "patient/book-appointment";
    }
}