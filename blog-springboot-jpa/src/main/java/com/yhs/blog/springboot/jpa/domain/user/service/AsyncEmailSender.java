package com.yhs.blog.springboot.jpa.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Log4j2
public class AsyncEmailSender {

    @Value("${spring.mail.username}")
    private String fromAddress;
    private final JavaMailSender mailSender;


    // 이메일 발송 한국말로 변경 그리고 유효시간 3분 내용 표시 그리고 @async는 하나의 쓰레드에서 사용하기떄문에 다중 사용자 사용할 수 있도록 처리하는방법알기
    @Async
    public CompletableFuture<Boolean> sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromAddress);
            message.setSubject(subject);
            message.setText(text);

            log.info("이메일 발송 시작 - Thread: {}", Thread.currentThread().getName());
            mailSender.send(message);
            log.info("이메일 발송 완료: {}", to);

            return CompletableFuture.completedFuture(true);


        } catch (MailException e) {
            // 메일 전송에 관한 예외 처리
            log.error("이메일 발송 실패: {}", e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
}
