package com.hospital;

import java.util.List;

public interface PatientService {

    Patient save(Patient patient);

    Patient findById(Long id);

    Patient findByUserId(Long userId);

    List<Patient> findAll();

    void deleteById(Long id);
}