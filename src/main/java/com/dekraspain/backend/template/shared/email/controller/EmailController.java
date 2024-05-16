package com.dekraspain.backend.template.shared.email.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;

import com.dekraspain.backend.template.shared.email.DTO.SendEmailDTO;
import com.dekraspain.backend.template.shared.email.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class EmailController {

  @Autowired
  private EmailService emailService;

  @PostMapping("/send-mail")
  public ResponseEntity<String> sendMail(
    @Valid @RequestBody SendEmailDTO emailBody
  ) {
    Context context = new Context();
    context.setVariable("title", emailBody.title);
    context.setVariable("content", emailBody.content);
    System.out.println(emailBody);
    try {
      emailService.sendEmailWithTemplate(
        emailBody.to,
        emailBody.subject,
        "email-template",
        context
      );
      return ResponseEntity.ok("Email sent successfull.");
    } catch (MessagingException e) {
      return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Error sending email.");
    }
  }
}
