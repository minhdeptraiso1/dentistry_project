package com.project.base_v1.service.impl;

import com.project.base_v1.entity.Appointment;
import com.project.base_v1.entity.User;
import com.project.base_v1.enums.AppointmentStatus;
import com.project.base_v1.repository.AppointmentRepository;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.service.AutomationAppointmentReminderJobService;
import com.project.base_v1.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentReminderJobImpl implements AutomationAppointmentReminderJobService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void sendDailyAppointmentReminder() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        List<AppointmentStatus> validStatuses = List.of(
                AppointmentStatus.WAITING,
                AppointmentStatus.ASSIGNED
        );

        List<Appointment> todayAppointments =
                appointmentRepository.findByWorkDateAndReminderTodaySentFalseAndStatusIn(
                        today,
                        validStatuses
                );

        List<Appointment> tomorrowAppointments =
                appointmentRepository.findByWorkDateAndReminderTomorrowSentFalseAndStatusIn(
                        tomorrow,
                        validStatuses
                );

        for (Appointment appointment : todayAppointments) {
            sendReminderEmail(appointment, "hôm nay", true);
        }

        for (Appointment appointment : tomorrowAppointments) {
            sendReminderEmail(appointment, "ngày mai", false);
        }
    }

    private void sendReminderEmail(Appointment appointment, String reminderLabel, boolean isTodayReminder) {
        try {
            Optional<User> userOpt =
                    userRepository.findByPatient_Id(appointment.getPatient().getId());

            if (userOpt.isEmpty()) {
                return;
            }

            User user = userOpt.get();

            if (user.getEmail() == null || user.getEmail().isBlank()) {
                return;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("patientName", appointment.getPatient().getFullName());
            model.put("appointmentCode", appointment.getAppointmentCode());
            model.put("workDate", appointment.getWorkDate());
            model.put("shift", appointment.getShift());
            model.put("reminderLabel", reminderLabel);
            model.put(
                    "doctorName",
                    appointment.getDoctor() != null
                            ? appointment.getDoctor().getUsername()
                            : "Chưa phân công"
            );

            emailService.sendTemplate(
                    user.getEmail(),
                    isTodayReminder
                            ? "Nhắc lịch hẹn khám hôm nay"
                            : "Nhắc lịch hẹn khám ngày mai",
                    "appointment-reminder",
                    model
            );

            if (isTodayReminder) {
                appointment.setReminderTodaySent(true);
                appointment.setReminderTodaySentAt(Instant.now());
            } else {
                appointment.setReminderTomorrowSent(true);
                appointment.setReminderTomorrowSentAt(Instant.now());
            }

        } catch (Exception e) {
            log.error(
                    "Lỗi gửi mail nhắc lịch cho appointmentId={}, reminderType={}",
                    appointment.getId(),
                    isTodayReminder ? "TODAY" : "TOMORROW",
                    e
            );
        }
    }
}