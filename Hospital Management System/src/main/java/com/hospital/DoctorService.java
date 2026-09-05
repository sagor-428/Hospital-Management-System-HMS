package com.hospital;

import java.util.List;

public interface DoctorService {

    Doctor save(Doctor doctor);

    Doctor findById(Long id);

    Doctor findByUserId(Long userId);

    Doctor findByUserEmail(String email);

    List<Doctor> findAll();

    List<Doctor> findByDepartment(Long departmentId);

    List<Doctor> searchBySpecialization(String specialization);

    void deleteById(Long id);
}