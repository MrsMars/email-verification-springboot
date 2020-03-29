package com.aoher.service;

import com.aoher.model.MailProperties;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class SendingMailService {

    private static final Logger log = LoggerFactory.getLogger(SendingMailService.class);

    private final MailProperties mailProperties;
    private final Configuration templates;

    @Autowired
    public SendingMailService(MailProperties mailProperties, Configuration templates) {
        this.mailProperties = mailProperties;
        this.templates = templates;
    }

    public boolean sendVerificationMail(String toEmail, String verificationCode) {
        String subject = "Please verify your email";
        String body = "";

        try {
            Template template = templates.getTemplate("email-verification.ftl");
            Map<String, String> map = new HashMap<>();
            map.put("VERIFICATION_URL", mailProperties.getVerificationApi() + verificationCode);
            body = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        } catch (IOException | TemplateException e) {
            log.error(e.getMessage());
        }
        return sendMail(toEmail, subject, body);
    }

    private boolean sendMail(String toEmail, String subject, String body) {
        try {
            Properties properties = System.getProperties();
            properties.put("mail.transport.protocol", "smtp");
            properties.put("mail.smtp.port", mailProperties.getSmtp().getPort());
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.auth", "true");

            Session session = Session.getDefaultInstance(properties);
            session.setDebug(true);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailProperties.getFrom(), mailProperties.getFromName()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);
            message.setContent(body, "text/html");

            Transport transport = session.getTransport();
            transport.connect(
                    mailProperties.getSmtp().getHost(),
                    mailProperties.getSmtp().getUsername(),
                    mailProperties.getSmtp().getPassword());
            transport.sendMessage(message, message.getAllRecipients());
            return true;
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException => {}", e.getMessage());
        } catch (AddressException e) {
            log.error("AddressException => {}", e.getMessage());
        } catch (NoSuchProviderException e) {
            log.error("NoSuchProviderException => {}", e.getMessage());
        } catch (MessagingException e) {
            log.error("MessagingException => {}", e.getMessage());
        }
        return false;
    }
}
