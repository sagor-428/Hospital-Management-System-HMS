package com.hospital;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorScheduleServiceImpl
        implements DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;

    @Override
    public DoctorSchedule save(DoctorSchedule schedule) {

        return scheduleRepository.save(schedule);
    }

    @Override
    public DoctorSchedule findById(Long id) {

        return scheduleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Schedule not found with id: " + id
                        )
                );
    }

    @Override
    public List<DoctorSchedule> findByDoctor(Long doctorId) {

        return scheduleRepository.findByDoctorId(doctorId);
    }

    @Override
    public List<DoctorSchedule> findByDoctorAndDay(
            Long doctorId,
            DayOfWeek dayOfWeek) {

        return scheduleRepository
                .findByDoctorIdAndDayOfWeek(
                        doctorId,
                        dayOfWeek
                );
    }

    @Override
    public void deleteById(Long id) {

        if (!scheduleRepository.existsById(id)) {

            throw new RuntimeException(
                    "Schedule not found with id: " + id
            );
        }

        scheduleRepository.deleteById(id);
    }
}