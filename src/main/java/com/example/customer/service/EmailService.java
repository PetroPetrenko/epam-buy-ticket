package com.example.customer.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    /**
     * Отправляет подтверждение бронирования с использованием HTML-шаблона.
     * 
     * @param to Получатель
     * @param subject Тема письма
     * @param templateName Имя шаблона в src/main/resources/templates/mail/
     * @param variables Переменные для подстановки в шаблон
     */
    public void sendBookingConfirmation(String to, String subject, String templateName, Map<String, Object> variables) {
        log.info("Preparing to send booking confirmation email to: {}", to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, 
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, 
                StandardCharsets.UTF_8.name());

            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process("mail/" + templateName, context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("booking-noreply@example.com");

            mailSender.send(message);
            log.info("Booking confirmation email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send booking confirmation email to {}: {}", to, e.getMessage());
            // В реальном приложении здесь может быть логика повторной отправки или уведомление админа
        }
    }
}
