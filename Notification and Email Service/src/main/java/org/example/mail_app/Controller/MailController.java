package org.example.mail_app.Controller;

import org.example.mail_app.DTO.MailRequestDTO;
import org.example.mail_app.Services.MailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    private final MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping("/send")
    public String sendMail(@RequestBody MailRequestDTO mailRequest) {
        mailService.sendEmail(mailRequest.getTo(), mailRequest.getSubject(), mailRequest.getBody());
        return "Email sent successfully!";
    }
}
