package com.hospital;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl
        implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;


    // =========================================================
    // SAVE APPOINTMENT
    // =========================================================

    @Override
    @Transactional
    public Appointment save(Appointment appointment) {

        boolean booked = isSlotBooked(
                appointment.getDoctor().getId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        );

        if (booked) {

            throw new RuntimeException(
                    "This appointment slot is already booked"
            );
        }

        if (appointment.getStatus() == null) {

            appointment.setStatus(
                    AppointmentStatus.PENDING
            );
        }

        if (appointment.getCreatedAt() == null) {

            appointment.setCreatedAt(
                    LocalDateTime.now()
            );
        }

        return appointmentRepository.save(appointment);
    }


    // =========================================================
    // FIND BY ID
    // =========================================================

    @Override
    public Appointment findById(Long id) {

        return appointmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Appointment not found"
                        )
                );
    }


    // =========================================================
    // FIND ALL
    // =========================================================

    @Override
    public List<Appointment> findAll() {

        return appointmentRepository.findAll();
    }


    // =========================================================
    // FIND PATIENT APPOINTMENTS
    // =========================================================

    @Override
    public List<Appointment> findByPatient(Long patientId) {

        return appointmentRepository
                .findByPatientIdOrderByAppointmentDateDesc(
                        patientId
                );
    }


    // =========================================================
    // FIND DOCTOR APPOINTMENTS
    // =========================================================

    @Override
    public List<Appointment> findByDoctor(Long doctorId) {

        return appointmentRepository
                .findByDoctorIdOrderByAppointmentDateDesc(
                        doctorId
                );
    }


    // =========================================================
    // FIND DOCTOR APPOINTMENTS BY DATE
    // =========================================================

    @Override
    public List<Appointment> findByDoctorAndDate(
            Long doctorId,
            LocalDate date) {

        return appointmentRepository
                .findByDoctorIdAndAppointmentDate(
                        doctorId,
                        date
                );
    }


    // =========================================================
    // CHECK SLOT BOOKED
    // =========================================================

    @Override
    public boolean isSlotBooked(
            Long doctorId,
            LocalDate date,
            LocalTime time) {

        return appointmentRepository
                .existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                        doctorId,
                        date,
                        time
                );
    }


    // =========================================================
    // GET AVAILABLE SLOTS
    // =========================================================

    @Override
    public List<LocalTime> getAvailableSlots(
            Doctor doctor,
            LocalDate date) {

        List<LocalTime> availableSlots =
                new ArrayList<>();


        // -----------------------------------------------------
        // Find schedule for selected day
        // -----------------------------------------------------

        DayOfWeek dayOfWeek =
                date.getDayOfWeek();

        List<DoctorSchedule> schedules =
                doctorScheduleRepository
                        .findByDoctorIdAndDayOfWeek(
                                doctor.getId(),
                                dayOfWeek
                        );


        // -----------------------------------------------------
        // No schedule
        // -----------------------------------------------------

        if (schedules.isEmpty()) {

            return availableSlots;
        }


        // -----------------------------------------------------
        // Generate slots
        // -----------------------------------------------------

        for (DoctorSchedule schedule : schedules) {

            LocalTime current =
                    schedule.getStartTime();

            LocalTime end =
                    schedule.getEndTime();

            int duration =
                    schedule.getSlotDuration();


            while (
                    !current.plusMinutes(duration)
                            .isAfter(end)
            ) {

                boolean booked =
                        isSlotBooked(
                                doctor.getId(),
                                date,
                                current
                        );

                if (!booked) {

                    availableSlots.add(current);
                }

                current =
                        current.plusMinutes(duration);
            }
        }


        return availableSlots;
    }


    // =========================================================
    // FIND DOCTOR APPOINTMENTS BY STATUS
    // =========================================================

    @Override
    public List<Appointment> findByDoctorAndStatus(
            Long doctorId,
            AppointmentStatus status) {

        return appointmentRepository
                .findByDoctorIdAndStatus(
                        doctorId,
                        status
                );
    }


    // =========================================================
    // UPDATE STATUS
    // =========================================================

    @Override
    @Transactional
    public void updateStatus(
            Long appointmentId,
            AppointmentStatus status) {

        Appointment appointment =
                findById(appointmentId);

        appointment.setStatus(status);

        appointmentRepository.save(appointment);
    }


    // =========================================================
    // CANCEL APPOINTMENT
    // =========================================================

    @Override
    @Transactional
    public void cancelAppointment(
            Long appointmentId) {

        Appointment appointment =
                findById(appointmentId);

        appointment.setStatus(
                AppointmentStatus.CANCELLED
        );

        appointmentRepository.save(appointment);
    }
}