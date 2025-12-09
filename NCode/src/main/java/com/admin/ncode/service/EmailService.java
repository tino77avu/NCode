package com.admin.ncode.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final String FROM_EMAIL = "ncodeactive@indigo-negocios.com";
    private static final String FROM_NAME = "NCode Licenciamiento";

    public void enviarCodigoVerificacion(String toEmail, String codigo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM_EMAIL, FROM_NAME);
            helper.setTo(toEmail);
            helper.setSubject("Código de Verificación - Recuperación de Contraseña");

            String htmlContent = construirMensajeEmail(codigo);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Error al enviar el correo electrónico: " + e.getMessage(), e);
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
}

