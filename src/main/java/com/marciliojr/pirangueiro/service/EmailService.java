package com.marciliojr.pirangueiro.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service responsável pelo envio de emails
 */
@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${relatorio.email.remetente}")
    private String emailRemetente;

    /**
     * Envia email HTML usando template Thymeleaf
     */
    public void enviarEmailHtml(String destinatario, String assunto, String template, Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(emailRemetente);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            
            String htmlContent = templateEngine.process(template, context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email enviado com sucesso para: {}", destinatario);
            
        } catch (MessagingException e) {
            log.error("Erro ao enviar email para {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Falha ao enviar email", e);
        }
    }

    /**
     * Envia email de texto simples
     */
    public void enviarEmailTexto(String destinatario, String assunto, String conteudo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(emailRemetente);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(conteudo, false);
            
            mailSender.send(message);
            log.info("Email de texto enviado com sucesso para: {}", destinatario);
            
        } catch (MessagingException e) {
            log.error("Erro ao enviar email de texto para {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Falha ao enviar email", e);
        }
    }

    /**
     * Valida se o serviço de email está configurado corretamente
     */
    public boolean isEmailConfigurado() {
        try {
            mailSender.createMimeMessage();
            return true;
        } catch (Exception e) {
            log.warn("Configuração de email não está válida: {}", e.getMessage());
            return false;
        }
    }
} 