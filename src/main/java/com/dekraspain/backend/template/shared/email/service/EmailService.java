package com.dekraspain.backend.template.shared.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j // Lombok generará el logger automáticamente
public class EmailService {

  @Autowired
  private JavaMailSender javaMailSender;

  @Autowired
  private TemplateEngine templateEngine;

  @Value("${email.address}")
  String emailAddress;

  @Value("${jwt.oauth.redirect-uri}")
  private String redirectUri;

  private String extractBaseUrl(String url) {
    try {
      URI uri = new URI(url);
      return uri.getScheme() + "://" + uri.getHost();
    } catch (URISyntaxException e) {
      log.error("Invalid redirect URI: " + url, e);
      return ""; // o lanza una excepción si prefieres
    }
  }

  @Async
  public void sendEmailWithTemplate(
    String to,
    String subject,
    String templateName,
    Context context
  ) {
    try {
      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(emailAddress);
      helper.setTo(to);
      helper.setSubject(subject);

      String content = templateEngine.process(templateName, context);
      helper.setText(content, true);

      javaMailSender.send(message);
      log.info("Correo enviado a: " + to);
    } catch (MessagingException | MailException e) {
      // Captura cualquier excepción que ocurra durante el envío del correo
      log.error("Error al enviar el correo a " + to, e);
    }
  }

  @Async
  public void sendEmailWithTemplateNoContext(
    String to,
    String subject,
    String templateName
  ) {
    try {
      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(emailAddress);
      helper.setTo(to);
      helper.setSubject(subject);
      String baseUrl = extractBaseUrl(redirectUri);
      Context context = new Context();
      context.setVariable("baseUrl", baseUrl);
      String htmlContent = templateEngine.process(templateName, context);

      // Set the HTML content of the email
      helper.setText(htmlContent, true);

      javaMailSender.send(message);
      log.info("Correo enviado a: " + to);
    } catch (MessagingException | MailException e) {
      // Captura cualquier excepción que ocurra durante el envío del correo
      log.error("Error al enviar el correo a " + to, e);
    }
  }
}
