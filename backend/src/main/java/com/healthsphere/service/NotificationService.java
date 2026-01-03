package com.healthsphere.service;

import com.healthsphere.dto.NotificationDTO;
import com.healthsphere.model.*;
import com.healthsphere.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public NotificationDTO createNotification(Long userId, String title, String message, 
                                             NotificationType type, Long relatedEntityId, 
                                             String relatedEntityType) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification(user, title, message, type);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRelatedEntityType(relatedEntityType);

        notification = notificationRepository.save(notification);
        
        // Console notification (basic implementation)
        System.out.println("NOTIFICATION [" + type + "] for User " + userId + ": " + title + " - " + message);
        
        return convertToDTO(notification);
    }

    public List<NotificationDTO> getNotificationsByUser(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotificationsByUser(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        notification.setRead(true);
        notification = notificationRepository.save(notification);
        return convertToDTO(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    // Scheduled task to check for upcoming appointments and send reminders
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void checkAppointmentReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayFromNow = now.plusDays(1);
        LocalDateTime twoHoursFromNow = now.plusHours(2);

        // Get appointments scheduled for tomorrow (24-hour reminder)
        List<Appointment> tomorrowAppointments = appointmentRepository.findAll().stream()
            .filter(a -> a.getAppointmentDate().isAfter(now) && 
                        a.getAppointmentDate().isBefore(oneDayFromNow) &&
                        a.getStatus() == AppointmentStatus.SCHEDULED ||
                        a.getStatus() == AppointmentStatus.CONFIRMED)
            .collect(Collectors.toList());

        for (Appointment appointment : tomorrowAppointments) {
            // Check if reminder already sent (simple check - in production, use a flag)
            boolean reminderExists = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                appointment.getPatient().getUser().getId())
                .stream()
                .anyMatch(n -> n.getRelatedEntityId() != null && 
                              n.getRelatedEntityId().equals(appointment.getId()) &&
                              n.getType() == NotificationType.APPOINTMENT_REMINDER &&
                              n.getCreatedAt().isAfter(now.minusHours(2)));

            if (!reminderExists) {
                String message = "You have an appointment with Dr. " + 
                    appointment.getDoctor().getFirstName() + " " + 
                    appointment.getDoctor().getLastName() + 
                    " tomorrow at " + appointment.getAppointmentDate().toLocalTime();
                
                createNotification(
                    appointment.getPatient().getUser().getId(),
                    "Appointment Reminder",
                    message,
                    NotificationType.APPOINTMENT_REMINDER,
                    appointment.getId(),
                    "APPOINTMENT"
                );
            }
        }

        // Get appointments scheduled in 2 hours (2-hour reminder)
        List<Appointment> soonAppointments = appointmentRepository.findAll().stream()
            .filter(a -> a.getAppointmentDate().isAfter(now) && 
                        a.getAppointmentDate().isBefore(twoHoursFromNow) &&
                        (a.getStatus() == AppointmentStatus.SCHEDULED ||
                         a.getStatus() == AppointmentStatus.CONFIRMED))
            .collect(Collectors.toList());

        for (Appointment appointment : soonAppointments) {
            boolean reminderExists = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                appointment.getPatient().getUser().getId())
                .stream()
                .anyMatch(n -> n.getRelatedEntityId() != null && 
                              n.getRelatedEntityId().equals(appointment.getId()) &&
                              n.getType() == NotificationType.APPOINTMENT_REMINDER &&
                              n.getCreatedAt().isAfter(now.minusHours(1)));

            if (!reminderExists) {
                String message = "You have an appointment with Dr. " + 
                    appointment.getDoctor().getFirstName() + " " + 
                    appointment.getDoctor().getLastName() + 
                    " in 2 hours at " + appointment.getAppointmentDate().toLocalTime();
                
                createNotification(
                    appointment.getPatient().getUser().getId(),
                    "Appointment Reminder (2 hours)",
                    message,
                    NotificationType.APPOINTMENT_REMINDER,
                    appointment.getId(),
                    "APPOINTMENT"
                );
            }
        }
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setRead(notification.isRead());
        dto.setReadAt(notification.getReadAt());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        dto.setRelatedEntityType(notification.getRelatedEntityType());
        return dto;
    }
}

