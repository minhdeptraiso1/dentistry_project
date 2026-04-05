package com.project.base_v1.repository;

import com.project.base_v1.entity.DoctorShiftCapacity;
import com.project.base_v1.entity.User;
import com.project.base_v1.enums.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorShiftCapacityRepository extends JpaRepository<DoctorShiftCapacity, UUID> {
    Optional<DoctorShiftCapacity> findByDoctor_IdAndWorkDateAndShift(UUID doctorId, LocalDate workDate, WorkShift shift);

    @Query("SELECT dsc FROM DoctorShiftCapacity dsc WHERE dsc.workDate = :workDate AND dsc.shift = :shift")
    List<DoctorShiftCapacity> findByWorkDateAndShift(@Param("workDate") LocalDate workDate, @Param("shift") WorkShift shift);

    boolean existsByDoctorAndWorkDateAndShift(User doctor, LocalDate workDate, WorkShift shift);
}