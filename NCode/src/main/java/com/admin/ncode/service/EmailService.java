package com.admin.ncode.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // SendGrid - Solo se usa si SENDGRID_API_KEY está configurado (típicamente en Render)
    @Value("${SENDGRID_API_KEY:}")
    private String sendGridApiKey;

    @Value("${SENDGRID_FROM_EMAIL:albertinovillar@ncod3.com}")
    private String fromEmail;

    @Value("${SENDGRID_FROM_NAME:NCOD3 Soporte}")
    private String fromName;
    
    // Para desarrollo local: usar SMTP (Zoho)
    // Para Render: usar SendGrid (configurar SENDGRID_API_KEY)

    // Configuración anterior (comentada)
    //private static final String FROM_EMAIL = "ncodeactive@indigo-negocios.com";
    //private static final String FROM_NAME = "NCode Licenciamiento";
    
    // Configuración nueva - Zoho (fallback si SendGrid no está disponible)
    private static final String FALLBACK_FROM_EMAIL = "soporte@ncod3.com";
    private static final String FALLBACK_FROM_NAME = "NCOD3 Soporte";

    public void enviarCodigoVerificacion(String toEmail, String codigo) {
        // Intentar usar SendGrid si está configurado
        if (sendGridApiKey != null && !sendGridApiKey.isEmpty()) {
            try {
                enviarConSendGrid(toEmail, "Código de Verificación - Recuperación de Contraseña", 
                                 construirMensajeEmail(codigo));
                logger.info("Correo enviado exitosamente con SendGrid a: {}", toEmail);
                return;
            } catch (Exception e) {
                logger.error("Error al enviar con SendGrid, intentando con SMTP: {}", e.getMessage());
                // Continuar con SMTP como fallback
            }
        }
        
        // Fallback a SMTP (para desarrollo local)
        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                String emailFrom = fromEmail != null && !fromEmail.isEmpty() ? fromEmail : FALLBACK_FROM_EMAIL;
                String nameFrom = fromName != null && !fromName.isEmpty() ? fromName : FALLBACK_FROM_NAME;
                
                helper.setFrom(emailFrom, nameFrom);
                helper.setTo(toEmail);
                helper.setSubject("Código de Verificación - Recuperación de Contraseña");

                String htmlContent = construirMensajeEmail(codigo);
                helper.setText(htmlContent, true);

                mailSender.send(message);
                logger.info("Correo enviado exitosamente con SMTP a: {}", toEmail);
            } catch (MessagingException | UnsupportedEncodingException e) {
                logger.error("Error al enviar el correo electrónico a {}: {}", toEmail, e.getMessage(), e);
                throw new RuntimeException("Error al enviar el correo electrónico: " + e.getMessage(), e);
            } catch (Exception e) {
                logger.error("Error inesperado al enviar el correo electrónico a {}: {}", toEmail, e.getMessage(), e);
                throw new RuntimeException("Error al enviar el correo electrónico: " + e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("No hay servicio de email configurado (SendGrid o SMTP)");
        }
    }

    public void enviarCodigoDemo(String toEmail, String codigo, String nombre) {
        // Intentar usar SendGrid si está configurado
        if (sendGridApiKey != null && !sendGridApiKey.isEmpty()) {
            try {
                enviarConSendGrid(toEmail, "Código de Verificación - Demo NCOD3", 
                                 construirMensajeEmailDemo(codigo, nombre));
                logger.info("Correo de demo enviado exitosamente con SendGrid a: {}", toEmail);
                return;
            } catch (Exception e) {
                logger.error("Error al enviar con SendGrid, intentando con SMTP: {}", e.getMessage());
                // Continuar con SMTP como fallback
            }
        }
        
        // Fallback a SMTP (para desarrollo local)
        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                String emailFrom = fromEmail != null && !fromEmail.isEmpty() ? fromEmail : FALLBACK_FROM_EMAIL;
                String nameFrom = fromName != null && !fromName.isEmpty() ? fromName : FALLBACK_FROM_NAME;
                
                helper.setFrom(emailFrom, nameFrom);
                helper.setTo(toEmail);
                helper.setSubject("Código de Verificación - Demo NCOD3");

                String htmlContent = construirMensajeEmailDemo(codigo, nombre);
                helper.setText(htmlContent, true);

                mailSender.send(message);
                logger.info("Correo de demo enviado exitosamente con SMTP a: {}", toEmail);
            } catch (MessagingException | UnsupportedEncodingException e) {
                logger.error("Error al enviar el correo de demo a {}: {}", toEmail, e.getMessage(), e);
                throw new RuntimeException("Error al enviar el correo electrónico: " + e.getMessage(), e);
            } catch (Exception e) {
                logger.error("Error inesperado al enviar el correo de demo a {}: {}", toEmail, e.getMessage(), e);
                throw new RuntimeException("Error al enviar el correo electrónico: " + e.getMessage(), e);
            }
        } else {
            logger.warn("No hay servicio de email configurado (SendGrid o SMTP). El código de demo es: {}", codigo);
            // No lanzar excepción, solo loguear el warning
            // El código ya está guardado en la BD, así que el usuario puede usarlo
        }
    }
    
    /**
     * Envía un email usando SendGrid API
     */
    private void enviarConSendGrid(String toEmail, String subject, String htmlContent) throws IOException {
        Email from = new Email(fromEmail != null && !fromEmail.isEmpty() ? fromEmail : FALLBACK_FROM_EMAIL, 
                              fromName != null && !fromName.isEmpty() ? fromName : FALLBACK_FROM_NAME);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("Email enviado exitosamente con SendGrid. Status: {}", response.getStatusCode());
            } else {
                logger.error("Error al enviar email con SendGrid. Status: {}, Body: {}", 
                           response.getStatusCode(), response.getBody());
                throw new RuntimeException("Error al enviar email con SendGrid: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (java.net.SocketTimeoutException ex) {
            logger.error("Timeout al enviar email con SendGrid: {}", ex.getMessage(), ex);
            throw new RuntimeException("Timeout al enviar email con SendGrid: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            logger.error("Error de IO al enviar email con SendGrid: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private String construirMensajeEmail(String codigo) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #00BFFF 0%, #0088CC 100%); color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
                ".content { background: #f9f9f9; padding: 30px; border: 1px solid #ddd; border-top: none; border-radius: 0 0 8px 8px; }" +
                ".code-box { background: #fff; border: 2px solid #00BFFF; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }" +
                ".code { font-size: 24px; font-weight: bold; color: #00BFFF; letter-spacing: 3px; font-family: 'Courier New', monospace; }" +
                ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>NCOD3 - Recuperación de Contraseña</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hola,</p>" +
                "<p>Has solicitado recuperar tu contraseña. Utiliza el siguiente código de verificación para continuar:</p>" +
                "<div class='code-box'>" +
                "<div class='code'>" + codigo + "</div>" +
                "</div>" +
                "<p>Este código es válido por 15 minutos. Si no solicitaste este cambio, puedes ignorar este correo.</p>" +
                "<p>Saludos,<br><strong>Equipo NCode Licenciamiento</strong></p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Este es un correo automático, por favor no respondas a este mensaje.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String construirMensajeEmailDemo(String codigo, String nombre) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #00BFFF 0%, #0088CC 100%); color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
                ".content { background: #f9f9f9; padding: 30px; border: 1px solid #ddd; border-top: none; border-radius: 0 0 8px 8px; }" +
                ".code-box { background: #fff; border: 2px solid #00BFFF; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }" +
                ".code { font-size: 24px; font-weight: bold; color: #00BFFF; letter-spacing: 3px; font-family: 'Courier New', monospace; }" +
                ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>NCOD3 - Código de Verificación para Demo</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hola " + (nombre != null ? nombre : "") + ",</p>" +
                "<p>Gracias por tu interés en probar NCOD3. Hemos recibido tu solicitud de demo.</p>" +
                "<p>Utiliza el siguiente código de verificación para acceder al demo:</p>" +
                "<div class='code-box'>" +
                "<div class='code'>" + codigo + "</div>" +
                "</div>" +
                "<p>Este código es válido por 1 día (24 horas). Nuestro equipo se pondrá en contacto contigo pronto para coordinar la demostración.</p>" +
                "<p>Saludos,<br><strong>Equipo NCOD3</strong></p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Este es un correo automático, por favor no respondas a este mensaje.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}

