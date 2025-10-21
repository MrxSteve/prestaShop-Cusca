package com.cusca.shopmoney_pg.services.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@shopmoney.com}")
    private String fromEmail;

    @Value("${app.name:ShopMoney}")
    private String appName;

    /**
     * Envía un correo electrónico usando un template HTML
     */
    public void enviarEmail(String destinatario, String asunto, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configurar datos básicos del correo
            helper.setFrom(fromEmail, appName);
            helper.setTo(destinatario);
            helper.setSubject(asunto);

            // Procesar template con variables
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);

            helper.setText(htmlContent, true);

            // Enviar correo
            mailSender.send(message);
            log.info("Correo enviado exitosamente a: {} con asunto: {}", destinatario, asunto);

        } catch (org.springframework.mail.MailAuthenticationException e) {
            log.error("Error de autenticación SMTP al enviar correo a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Error de autenticación en servidor de correo", e);
        } catch (org.springframework.mail.MailSendException e) {
            log.error("Error enviando correo a {} (posible problema con el destinatario): {}", destinatario, e.getMessage());
            throw new RuntimeException("Error enviando correo - problema con destinatario", e);
        } catch (org.springframework.mail.MailException e) {
            log.error("Error general de correo al enviar a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Error del sistema de correo", e);
        } catch (Exception e) {
            log.error("Error inesperado enviando correo a {}: {}", destinatario, e.getMessage(), e);
            throw new RuntimeException("Error enviando correo electrónico", e);
        }
    }

    /**
     * Envía un correo electrónico simple (texto plano)
     */
    public void enviarEmailSimple(String destinatario, String asunto, String mensaje) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje, false);

            mailSender.send(message);
            log.info("Correo simple enviado exitosamente a: {} con asunto: {}", destinatario, asunto);

        } catch (org.springframework.mail.MailAuthenticationException e) {
            log.error("Error de autenticación SMTP al enviar correo simple a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Error de autenticación en servidor de correo", e);
        } catch (org.springframework.mail.MailSendException e) {
            log.error("Error enviando correo simple a {} (posible problema con el destinatario): {}", destinatario, e.getMessage());
            throw new RuntimeException("Error enviando correo - problema con destinatario", e);
        } catch (org.springframework.mail.MailException e) {
            log.error("Error general de correo al enviar simple a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Error del sistema de correo", e);
        } catch (Exception e) {
            log.error("Error inesperado enviando correo simple a {}: {}", destinatario, e.getMessage(), e);
            throw new RuntimeException("Error enviando correo electrónico", e);
        }
    }

    /**
     * Envía correos masivos de forma asíncrona
     */
    public void enviarEmailsMasivos(String[] destinatarios, String asunto, String templateName, Map<String, Object> variables) {
        for (String destinatario : destinatarios) {
            try {
                enviarEmail(destinatario, asunto, templateName, variables);
            } catch (Exception e) {
                log.error("Error enviando correo masivo a {}: {}", destinatario, e.getMessage());
                // Continúa con los demás destinatarios aunque falle uno
            }
        }
    }
}
