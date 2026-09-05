package com.hospital;

import java.util.List;

public interface DepartmentService {

    Department save(Department department);

    Department findById(Long id);

    List<Department> findAll();

    void deleteById(Long id);

    boolean existsByName(String name);
}