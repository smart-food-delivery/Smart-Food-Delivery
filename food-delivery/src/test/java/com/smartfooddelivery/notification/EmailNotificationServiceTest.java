package com.smartfooddelivery.notification;

import com.smartfooddelivery.model.User;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailNotificationServiceTest {
    private EmailNotificationService service() {
        return new EmailNotificationService("smtp.example.com", 587,
                "sender@example.com", "test-password", "sender@example.com", "starttls");
    }

    @Test
    void buildsUtf8MessageWithCorrectRecipientAndSender() throws Exception {
        MimeMessage message = service().createMessage(
                new User(1, "customer", "hash", "Customer", "customer@example.com"),
                "طلبك جاهز للتوصيل");
        message.saveChanges();
        assertEquals("customer@example.com", message.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("sender@example.com", message.getFrom()[0].toString());
        assertEquals("طلبك جاهز للتوصيل", message.getContent());
        assertTrue(message.getContentType().toUpperCase().contains("UTF-8"));
    }

    @Test
    void rejectsMissingOrMalformedRecipientBeforeConnecting() {
        assertThrows(IllegalArgumentException.class, () -> service().sendNotification(
                new User(1, "customer", "hash", "Customer"), "update"));
        assertThrows(IllegalArgumentException.class, () -> service().sendNotification(
                new User(1, "customer", "hash", "Customer", "bad\r\nBcc: other@example.com"), "update"));
    }

    @Test
    void rejectsUnencryptedModeAndInvalidPort() {
        assertThrows(IllegalArgumentException.class, () -> new EmailNotificationService(
                "smtp.example.com", 587, "user", "password", "sender@example.com", "none"));
        assertThrows(IllegalArgumentException.class, () -> new EmailNotificationService(
                "smtp.example.com", 0, "user", "password", "sender@example.com", "ssl"));
    }
}
