package com.letsgettesty.backend.model;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.letsgettesty.backend.notification.NotificationRepository;

@Service
public class Notifier {

    private static final Logger logger = LoggerFactory.getLogger(Notifier.class);

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;
    private final String senderAddress;
    private final String senderPassword;

    public Notifier(
            JavaMailSender mailSender,
            NotificationRepository notificationRepository,
            @Value("${spring.mail.username:}") String senderAddress,
            @Value("${spring.mail.password:}") String senderPassword) {
        this.mailSender = mailSender;
        this.notificationRepository = notificationRepository;
        this.senderAddress = senderAddress == null ? "" : senderAddress.trim();
        this.senderPassword = senderPassword == null ? "" : senderPassword.trim();
    }

    public Optional<Notification> sendEmail(int reservationId, String destination, String subject, String message) {
        Notification notification = new Notification();
        notification.setReservationId(reservationId);
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setDestination(destination);
        notification.setSubject(subject);
        notification.setMessage(message);
        return send(notification);
    }

    public Optional<Notification> send(Notification notification) {
        if (notification == null || notification.getChannel() != NotificationChannel.EMAIL) {
            return Optional.empty();
        }
        if (isBlank(notification.getDestination())) {
            logger.debug("Skipping email notification for reservation {} because no destination email is available.",
                    notification.getReservationId());
            return Optional.empty();
        }
        if (isBlank(senderAddress) || isBlank(senderPassword)) {
            logger.debug("Skipping email notification because sender email or app password is not configured.");
            return Optional.empty();
        }
        if (isBlank(notification.getMessage())) {
            logger.warn("Skipping email notification for reservation {} because the message body is blank.",
                    notification.getReservationId());
            return Optional.empty();
        }

        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(senderAddress);
        email.setTo(notification.getDestination().trim());
        email.setSubject(isBlank(notification.getSubject()) ? "LetsGetTesty notification" : notification.getSubject().trim());
        email.setText(notification.getMessage().trim());

        try {
            mailSender.send(email);
            notification.setSentAt(LocalDateTime.now());
            return Optional.of(notificationRepository.insert(notification));
        } catch (MailException exception) {
            logger.warn(
                    "Failed to send email notification for reservation {} to {}: {}",
                    notification.getReservationId(),
                    notification.getDestination(),
                    exception.getMessage());
            return Optional.empty();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
