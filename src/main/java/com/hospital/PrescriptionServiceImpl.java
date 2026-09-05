package com.hospital;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl
        implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    @Override
    public Prescription save(Prescription prescription) {
        return prescriptionRepository.save(prescription);
    }

    @Override
    public Prescription findById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Prescription not found"
                        ));
    }

    @Override
    public Prescription findByAppointmentId(Long appointmentId) {
        return prescriptionRepository
                .findByAppointmentId(appointmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Prescription not found"
                        ));
    }

    @Override
    public boolean existsByAppointmentId(Long appointmentId) {
        return prescriptionRepository
                .existsByAppointmentId(appointmentId);
    }


    // =========================================================
    // CREATE OR UPDATE PRESCRIPTION (WITH MEDICINES)
    // =========================================================

    @Override
    @Transactional
    public Prescription createOrUpdate(
            Appointment appointment,
            String diagnosis,
            String notes,
            List<PrescriptionMedicine> medicines) {

        Prescription prescription = prescriptionRepository
                .findByAppointmentId(appointment.getId())
                .orElseGet(() ->
                        Prescription.builder()
                                .appointment(appointment)
                                .createdAt(LocalDateTime.now())
                                .build()
                );

        prescription.setDiagnosis(diagnosis);
        prescription.setNotes(notes);

        // Clear existing medicines so orphanRemoval deletes the old rows,
        // then attach the fresh list sent from the form.
        prescription.getMedicines().clear();

        for (PrescriptionMedicine medicine : medicines) {
            medicine.setPrescription(prescription);
            prescription.getMedicines().add(medicine);
        }

        return prescriptionRepository.save(prescription);
    }
}