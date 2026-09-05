package com.hospital;

import java.util.List;

public interface UserService {

    User save(User user);

    User findById(Long id);

    User findByEmail(String email);

    List<User> findAll();

    User registerPatient(RegistrationRequest request);

    boolean existsByEmail(String email);
}