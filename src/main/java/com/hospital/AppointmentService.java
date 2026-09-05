package com.hospital;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentService {

    Appointment save(Appointment appointment);

    Appointment findById(Long id);

    List<Appointment> findAll();

    List<Appointment> findByPatient(Long patientId);

    List<Appointment> findByDoctor(Long doctorId);

    List<Appointment> findByDoctorAndDate(
            Long doctorId,
            LocalDate date
    );

    boolean isSlotBooked(
            Long doctorId,
            LocalDate date,
            LocalTime time
    );

    List<LocalTime> getAvailableSlots(
            Doctor doctor,
            LocalDate date
    );

    List<Appointment> findByDoctorAndStatus(
            Long doctorId,
            AppointmentStatus status
    );

    void updateStatus(
            Long appointmentId,
            AppointmentStatus status
    );

    void cancelAppointment(Long appointmentId);
}