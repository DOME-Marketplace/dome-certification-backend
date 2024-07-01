package com.dekraspain.backend.template.shared.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

  @Autowired
  private JavaMailSender javaMailSender;

  @Autowired
  private TemplateEngine templateEngine;

  @Value("${email.address}")
  String emailAddress;

  public void sendEmailWithTemplate(
    String to,
    String subject,
    String templateName,
    Context context
  ) throws MessagingException {
    MimeMessage message = javaMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setFrom(emailAddress);
    helper.setTo(to);
    helper.setSubject(subject);

    String content = templateEngine.process(templateName, context);
    helper.setText(content, true);

    javaMailSender.send(message);
  }

  public void sendEmailWithTemplateNoContext(
    String to,
    String subject,
    String templateName
  ) throws MessagingException {
    MimeMessage message = javaMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setFrom(emailAddress);
    helper.setTo(to);
    helper.setSubject(subject);

    Context context = new Context();
    String htmlContent = templateEngine.process(templateName, context);

    // Set the HTML content of the email
    helper.setText(htmlContent, true);

    javaMailSender.send(message);
  }
}
