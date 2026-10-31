package com.smartfooddelivery.notification;

import com.smartfooddelivery.model.User;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

/** SMTP notification sender. Environment variables override the working-directory .env. */
public final class EmailNotificationService implements NotificationService {
    private final Session session;
    private final String username;
    private final String password;
    private final InternetAddress from;

    public static EmailNotificationService fromEnvironment() {
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        return new EmailNotificationService(
                required(env, "SMTP_HOST"),
                Integer.parseInt(required(env, "SMTP_PORT")),
                required(env, "SMTP_USERNAME"),
                required(env, "SMTP_PASSWORD"),
                required(env, "SMTP_FROM"),
                env.get("SMTP_SECURITY", "starttls"));
    }

    private static String required(Dotenv env, String key) {
        String value = env.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing email setting: " + key);
        }
        return value;
    }

    public EmailNotificationService(String host, int port, String username,
                                    String password, String from, String security) {
        if (host == null || host.isBlank() || port < 1 || port > 65535) {
            throw new IllegalArgumentException("Valid SMTP host and port are required");
        }
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("SMTP credentials are required");
        }
        if (!"starttls".equals(security) && !"ssl".equals(security)) {
            throw new IllegalArgumentException("SMTP_SECURITY must be starttls or ssl");
        }
        this.username = username;
        this.password = password;
        this.from = address(from);
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", Integer.toString(port));
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        properties.setProperty("mail.smtp.ssl.enable", Boolean.toString("ssl".equals(security)));
        properties.setProperty("mail.smtp.starttls.enable", Boolean.toString("starttls".equals(security)));
        properties.setProperty("mail.smtp.starttls.required", Boolean.toString("starttls".equals(security)));
        properties.setProperty("mail.smtp.connectiontimeout", "10000");
        properties.setProperty("mail.smtp.timeout", "10000");
        properties.setProperty("mail.smtp.writetimeout", "10000");
        session = Session.getInstance(properties);
    }

    private static InternetAddress address(String value) {
        try {
            if (value == null || value.isBlank() || value.contains("\r") || value.contains("\n")) {
                throw new IllegalArgumentException("A valid email address is required");
            }
            InternetAddress address = new InternetAddress(value, true);
            address.validate();
            if (!address.getAddress().contains("@")) {
                throw new IllegalArgumentException("A valid email address is required");
            }
            return address;
        } catch (MessagingException e) {
            throw new IllegalArgumentException("A valid email address is required", e);
        }
    }

    MimeMessage createMessage(User customer, String text) throws MessagingException {
        Objects.requireNonNull(customer, "Customer is required");
        Objects.requireNonNull(text, "Message is required");
        MimeMessage message = new MimeMessage(session);
        message.setFrom(from);
        message.setRecipient(Message.RecipientType.TO, address(customer.getEmail()));
        message.setSubject("Smart Food Delivery - Order update", StandardCharsets.UTF_8.name());
        message.setText(text, StandardCharsets.UTF_8.name());
        message.setSentDate(new Date());
        return message;
    }

    @Override
    public void sendNotification(User customer, String message) {
        try {
            Transport.send(createMessage(customer, message), username, password);
        } catch (MessagingException e) {
            // Preserve the cause for callers; never include credentials in the message.
            throw new IllegalStateException("Email notification failed; check SMTP settings and connectivity", e);
        }
    }
}
