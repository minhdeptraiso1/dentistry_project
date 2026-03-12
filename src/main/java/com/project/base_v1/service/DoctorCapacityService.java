package com.project.base_v1.service;

import java.time.LocalDate;
import java.util.List;

import com.project.base_v1.dto.request.appointment.SetDoctorShiftCapacityRequest;
import com.project.base_v1.dto.response.appointment.AvailableDoctorResponse;
import com.project.base_v1.enums.WorkShift;

public interface DoctorCapacityService {
    void setCapacity(SetDoctorShiftCapacityRequest request);
    
    List<AvailableDoctorResponse> getAvailableDoctors(LocalDate workDate, WorkShift shift);
}