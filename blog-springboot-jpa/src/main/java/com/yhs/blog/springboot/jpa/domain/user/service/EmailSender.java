package com.yhs.blog.springboot.jpa.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class EmailSender {

    @Value("${spring.mail.username}")
    private String fromAddress;
    private final JavaMailSender mailSender;

    public boolean sendEmail(String to, String subject, String text) {

        log.info("[EmailSender] sendEmail() 메서드 시작");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromAddress);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            return true;

        } catch (MailException e) {
            // 메일 전송에 관한 예외 처리
            log.error("이메일 발송 실패: {}", e.getMessage());
            return false;
        }
    }
}
