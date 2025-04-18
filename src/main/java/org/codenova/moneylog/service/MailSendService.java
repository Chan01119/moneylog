package org.codenova.moneylog.service;

import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codenova.moneylog.entity.User;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class MailSendService {

    private JavaMailSender mailSender;

    public boolean sendWelcomeMessage(User user) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(user.getEmail());
        message.setSubject("[머니로그] 환영합니다.");
        message.setText("머니로그에 가입해주셔서 감사합니다.");
        message.setText("안녕하세요, " + user.getNickname() + "님!\n머니로그에 가입하신 것을 진심으로 환영합니다.\n\n앞으로 다양한 컨텐츠와 서비스를 제공해드리겠습니다.\n\n팀 코드노바 드림");
        boolean result = true;
        try {
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("error = {}", e);
            result = false;
        }
        return result;
    }

    public boolean senWelcomeHtmlMessage(User user) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, "utf-8");
            messageHelper.setTo(user.getEmail());
            messageHelper.setSubject("[코드노바] 머니로그 회원가입을 환영합니다.");

            String html = "<h2>안녕하세요, 머니로그입니다.</h2>";

            html += "<p>안녕하세요<h2><a href='http://192.168.10.158:8080'>머니로그</a>에 가입하신 것을 진심으로 환영합니다.</p>";
            html += "<p>앞으로 다양한 컨텐츠와 서비스를 제공해드리겠습니다.</p>";
            html += "<p><span style='color : gray'>팀 코드노바 올림</span</</p>";
            messageHelper.setText(html, true);

            mailSender.send(message);
        } catch (Exception e) {
            log.error("error = {}", e);
        }
        return true;
    }

    public boolean sendTemporalPasswordMessage(String email, String temporalPassword) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("[코드노바] 임시 비밀번호를 설정합니다.");
        message.setText("임시비밀번호 :" + temporalPassword);

        boolean result = true;
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("error = {}", e);
            result = false;
        }
        return result;
    }

    public boolean sendEmailVerify(String email, String token) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, "utf-8");
            messageHelper.setTo(email);
            messageHelper.setSubject("[머니로그] 이메일 인증합니다.");

            String html = "<h2>안녕하세요, <a href='http://192.168.10.158:8080'>머니로그</a>입니다</h2>";
            html += "<p><a href='http://192.168.10.158:8080/auth/email-verify/" + email + "/" + token + "'>인증하기</a></p>";
            messageHelper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            log.error("error = {}", e);
            return false;
        }
        return true;
    }
}
