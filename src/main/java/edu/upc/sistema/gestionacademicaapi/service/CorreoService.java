package edu.upc.sistema.gestionacademicaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envio de correo (HU-23). En desarrollo registra el correo en el log
 * (app.mail.enabled=false); en produccion usa el JavaMailSender configurado
 * (spring.mail.* + app.mail.enabled=true).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CorreoService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.from:no-reply@upc.edu.pe}")
    private String remitente;

    @Value("${app.mail.enabled:false}")
    private boolean habilitado;

    /** Retorna true si el correo se envio (o se registro correctamente en dev). */
    public boolean enviar(String destino, String asunto, String cuerpo) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();

        if (!habilitado || sender == null || destino == null || destino.isBlank()) {
            log.info("[CORREO-DEV] Para: {} | Asunto: {}\n{}", destino, asunto, cuerpo);
            return true;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(remitente);
            msg.setTo(destino);
            msg.setSubject(asunto);
            msg.setText(cuerpo);
            sender.send(msg);
            log.info("Correo enviado a {} — {}", destino, asunto);
            return true;
        } catch (Exception ex) {
            log.warn("Fallo el envio de correo a {}: {}", destino, ex.getMessage());
            return false;
        }
    }
}
