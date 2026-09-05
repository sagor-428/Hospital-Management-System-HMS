package com.hospital;

import java.time.DayOfWeek;
import java.util.List;

public interface DoctorScheduleService {

    DoctorSchedule save(DoctorSchedule schedule);

    DoctorSchedule findById(Long id);

    List<DoctorSchedule> findByDoctor(Long doctorId);

    List<DoctorSchedule> findByDoctorAndDay(
            Long doctorId,
            DayOfWeek dayOfWeek
    );

    void deleteById(Long id);
}