package com.hospital;

import java.util.List;

public interface PrescriptionService {

    Prescription save(Prescription prescription);

    Prescription findById(Long id);

    Prescription findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);

    Prescription createOrUpdate(
            Appointment appointment,
            String diagnosis,
            String notes,
            List<PrescriptionMedicine> medicines
    );
}