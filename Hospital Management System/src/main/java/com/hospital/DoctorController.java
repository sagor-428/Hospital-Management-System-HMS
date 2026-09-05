package com.hospital;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;

@Controller
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorScheduleService doctorScheduleService;
    private final AppointmentService appointmentService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // DOCTOR DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model) {

        Doctor doctor = doctorService
                .findByUserEmail(authentication.getName());

        model.addAttribute("doctor", doctor);

        java.util.List<Appointment> appointments =
                appointmentService.findByDoctor(doctor.getId());

        model.addAttribute(
                "appointments",
                appointments
        );

        model.addAttribute(
                "totalAppointments",
                appointments.size()
        );

        model.addAttribute(
                "pendingAppointments",
                appointments.stream()
                        .filter(a -> a.getStatus() == AppointmentStatus.PENDING)
                        .count()
        );

        model.addAttribute(
                "confirmedAppointments",
                appointments.stream()
                        .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED)
                        .count()
        );

        model.addAttribute(
                "completedAppointments",
                appointments.stream()
                        .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                        .count()
        );

        return "doctor/dashboard";
    }


    // =========================================================
    // MY APPOINTMENTS
    // =========================================================

    @GetMapping("/appointments")
    public String appointments(
            Authentication authentication,
            Model model) {

        Doctor doctor = doctorService
                .findByUserEmail(authentication.getName());

        model.addAttribute("doctor", doctor);

        model.addAttribute(
                "appointments",
                appointmentService.findByDoctor(doctor.getId())
        );

        return "doctor/appointments";
    }


    // =========================================================
    // UPDATE APPOINTMENT STATUS
    // =========================================================

    @PostMapping("/appointments/{id}/status")
    public String updateAppointmentStatus(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {

        Doctor doctor = doctorService
                .findByUserEmail(authentication.getName());

        Appointment appointment =
                appointmentService.findById(id);

        // Security check
        if (!appointment.getDoctor()
                .getId()
                .equals(doctor.getId())) {

            throw new RuntimeException(
                    "You are not allowed to update this appointment"
            );
        }

        appointmentService.updateStatus(id, status);

        return "redirect:/doctor/appointments";
    }


    // =========================================================
    // MY SCHEDULE
    // =========================================================

    @GetMapping("/schedule")
    public String schedule(
            Authentication authentication,
            Model model) {

        Doctor doctor = doctorService
                .findByUserEmail(authentication.getName());

        model.addAttribute("doctor", doctor);

        model.addAttribute(
                "schedules",
                doctorScheduleService.findByDoctor(
                        doctor.getId()
                )
        );

        model.addAttribute(
                "schedule",
                new DoctorSchedule()
        );

        model.addAttribute(
                "days",
                DayOfWeek.values()
        );

        return "doctor/schedule";
    }


    // =========================================================
    // EDIT SCHEDULE
    // =========================================================

    @GetMapping("/schedule/edit/{id}")
    public String editSchedule(
            Authentication authentication,
            @PathVariable Long id,
            Model model) {

        Doctor doctor = doctorService
                .findByUserEmail(authentication.getName());

        DoctorSchedule schedule =
                doctorScheduleService.findById(id);

        // Security check
        if (!schedule.getDoctor()
                .getId()
                .equals(doctor.getId())) {

            throw new RuntimeException(
                    "You are not allowed to edit this schedule"
            );
        }

        model.addAttribute("doctor", doctor);

        model.addAttribute(
                "schedules",
                doctorScheduleService.findByDoctor(
                        doctor.getId()
                )
        );

        model.addAttribute(
                "schedule",
                schedule
        );

        model.addAttribute(
                "days",
                DayOfWeek.values()
        );

        return "doctor/schedule";
    }


    // =========================================================
    // SAVE / UPDATE SCHEDULE
    // =========================================================

    @PostMapping("/schedule/save")
    public String saveSchedule(
            Authentication authentication,
            @ModelAttribute("schedule")
            DoctorSchedule schedule,
            Model model) {

        Doctor doctor = doctorService
                .findByUserEmail(authentication.getName());


        // =====================================================
        // VALIDATION
        // =====================================================

        if (schedule.getDayOfWeek() == null) {

            model.addAttribute(
                    "error",
                    "Please select a day."
            );

            return loadSchedulePage(
                    doctor,
                    schedule,
                    model
            );
        }


        if (schedule.getStartTime() == null ||
                schedule.getEndTime() == null) {

            model.addAttribute(
                    "error",
                    "Start time and end time are required."
            );

            return loadSchedulePage(
                    doctor,
                    schedule,
                    model
            );
        }


        if (!schedule.getStartTime()
                .isBefore(schedule.getEndTime())) {

            model.addAttribute(
                    "error",
                    "Start time must be before end time."
            );

            return loadSchedulePage(
                    doctor,
                    schedule,
                    model
            );
        }


        if (schedule.getSlotDuration() == null ||
                schedule.getSlotDuration() <= 0) {

            model.addAttribute(
                    "error",
                    "Slot duration must be greater than 0."
            );

            return loadSchedulePage(
                    doctor,
                    schedule,
                    model
            );
        }


        // =====================================================
        // SECURITY
        // =====================================================

        /*
         * If this is an existing schedule,
         * make sure the schedule belongs to the
         * currently logged-in doctor.
         */

        if (schedule.getId() != null) {

            DoctorSchedule existingSchedule =
                    doctorScheduleService.findById(
                            schedule.getId()
                    );

            if (!existingSchedule.getDoctor()
                    .getId()
                    .equals(doctor.getId())) {

                throw new RuntimeException(
                        "You are not allowed to update this schedule"
                );
            }
        }


        // Always assign logged-in doctor
        schedule.setDoctor(doctor);


        // Save / Update
        doctorScheduleService.save(schedule);


        return "redirect:/doctor/schedule";
    }


    // =========================================================
    // DELETE SCHEDULE
    // =========================================================

    @PostMapping("/schedule/delete/{id}")
    public String deleteSchedule(
            Authentication authentication,
            @PathVariable Long id) {

        Doctor doctor = doctorService
                .findByUserEmail(authentication.getName());

        DoctorSchedule schedule =
                doctorScheduleService.findById(id);


        // Security check
        if (!schedule.getDoctor()
                .getId()
                .equals(doctor.getId())) {

            throw new RuntimeException(
                    "You are not allowed to delete this schedule"
            );
        }


        doctorScheduleService.deleteById(id);


        return "redirect:/doctor/schedule";
    }


    // =========================================================
    // MY PROFILE
    // =========================================================

    @GetMapping("/profile")
    public String profile(
            Authentication authentication,
            Model model) {

        Doctor doctor = doctorService
                .findByUserEmail(authentication.getName());

        model.addAttribute("doctor", doctor);

        return "doctor/profile";
    }


    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    @PostMapping("/profile")
    public String updateProfile(
            Authentication authentication,
            @RequestParam String name,
            @RequestParam String phone,
            @RequestParam String specialization,
            @RequestParam(required = false) String qualification,
            @RequestParam(required = false) Integer experience,
            @RequestParam(required = false) Double consultationFee,
            @RequestParam(required = false) String about,
            @RequestParam(required = false) String newPassword,
            Model model) {

        Doctor doctor = doctorService
                .findByUserEmail(authentication.getName());

        User user = doctor.getUser();
        user.setName(name);
        user.setPhone(phone);

        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        userService.save(user);

        doctor.setSpecialization(specialization);
        doctor.setQualification(qualification);
        doctor.setExperience(experience);
        doctor.setConsultationFee(consultationFee);
        doctor.setAbout(about);

        doctorService.save(doctor);

        model.addAttribute("doctor", doctor);
        model.addAttribute("success", "Profile updated successfully.");

        return "doctor/profile";
    }


    // =========================================================
    // LOAD SCHEDULE PAGE
    // =========================================================

    private String loadSchedulePage(
            Doctor doctor,
            DoctorSchedule schedule,
            Model model) {

        model.addAttribute(
                "doctor",
                doctor
        );

        model.addAttribute(
                "schedules",
                doctorScheduleService.findByDoctor(
                        doctor.getId()
                )
        );

        model.addAttribute(
                "schedule",
                schedule
        );

        model.addAttribute(
                "days",
                DayOfWeek.values()
        );

        return "doctor/schedule";
    }
}