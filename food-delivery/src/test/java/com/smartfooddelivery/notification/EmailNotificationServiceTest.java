package com.smartfooddelivery.notification;

import com.smartfooddelivery.model.User;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailNotificationServiceTest {

    private EmailNotificationService service() {
        return new EmailNotificationService(
                "smtp.example.com",
                587,
                "sender@example.com",
                "test-password",
                "sender@example.com",
                "starttls"
        );
    }

    private User customer(String email) {
        return new User(
                1, "customer", "hash", "Customer", email
        );
    }

    @Test
    void buildsUtf8MessageWithCorrectRecipientAndSender() throws Exception {
        MimeMessage message = service().createMessage(
                customer("customer@example.com"),
                "طلبك جاهز للتوصيل"
        );

        message.saveChanges();

        assertEquals(
                "customer@example.com",
                message.getRecipients(Message.RecipientType.TO)[0].toString()
        );
        assertEquals(
                "sender@example.com",
                message.getFrom()[0].toString()
        );
        assertEquals(
                "Smart Food Delivery - Order update",
                message.getSubject()
        );
        assertEquals("طلبك جاهز للتوصيل", message.getContent());
        assertTrue(
                message.getContentType().toUpperCase().contains("UTF-8")
        );
        assertNotNull(message.getSentDate());
    }

    @Test
    void sendsCorrectMessageUsingConfiguredCredentials() throws Exception {
        EmailNotificationService service = service();

        try (MockedStatic<Transport> transport = mockStatic(Transport.class)) {
            service.sendNotification(
                    customer("customer@example.com"),
                    "Order #10 is out for delivery."
            );

            ArgumentCaptor<Message> messageCaptor =
                    ArgumentCaptor.forClass(Message.class);

            transport.verify(() -> Transport.send(
                    messageCaptor.capture(),
                    eq("sender@example.com"),
                    eq("test-password")
            ));

            Message sentMessage = messageCaptor.getValue();

            assertEquals(
                    "customer@example.com",
                    sentMessage.getRecipients(Message.RecipientType.TO)[0]
                            .toString()
            );
            assertEquals(
                    "Order #10 is out for delivery.",
                    sentMessage.getContent()
            );

            transport.verifyNoMoreInteractions();
        }
    }

    @Test
    void wrapsSendingFailureAndPreservesCause() {
        EmailNotificationService service = service();

        MessagingException failure =
                new MessagingException("Simulated SMTP failure");

        try (MockedStatic<Transport> transport = mockStatic(Transport.class)) {
            transport.when(() -> Transport.send(
                    any(Message.class),
                    eq("sender@example.com"),
                    eq("test-password")
            )).thenThrow(failure);

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> service.sendNotification(
                            customer("customer@example.com"),
                            "Order update"
                    )
            );

            assertSame(failure, exception.getCause());
            assertTrue(
                    exception.getMessage().contains("Email notification failed")
            );
            assertFalse(
                    exception.getMessage().contains("test-password")
            );
        }
    }

    @Test
    void rejectsInvalidRecipientsWithoutSending() {
        EmailNotificationService service = service();

        String[] invalidEmails = {
                null,
                "",
                "   ",
                "customer",
                "customer@@example.com",
                "bad\rBcc: other@example.com",
                "bad\nBcc: other@example.com"
        };

        try (MockedStatic<Transport> transport = mockStatic(Transport.class)) {
            for (String email : invalidEmails) {
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.sendNotification(
                                customer(email), "update"
                        )
                );
            }

            transport.verifyNoInteractions();
        }
    }

    @Test
    void rejectsMissingCustomerOrMessageWithoutSending() {
        EmailNotificationService service = service();

        try (MockedStatic<Transport> transport = mockStatic(Transport.class)) {
            assertThrows(
                    NullPointerException.class,
                    () -> service.sendNotification(null, "update")
            );

            assertThrows(
                    NullPointerException.class,
                    () -> service.sendNotification(
                            customer("customer@example.com"), null
                    )
            );

            transport.verifyNoInteractions();
        }
    }

    @Test
    void rejectsMissingHost() {
        for (String host : new String[]{null, "", "   "}) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new EmailNotificationService(
                            host,
                            587,
                            "user",
                            "password",
                            "sender@example.com",
                            "starttls"
                    )
            );
        }
    }

    @Test
    void rejectsPortsOutsideValidRange() {
        for (int port : new int[]{-1, 0, 65536}) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new EmailNotificationService(
                            "smtp.example.com",
                            port,
                            "user",
                            "password",
                            "sender@example.com",
                            "starttls"
                    )
            );
        }
    }

    @Test
    void rejectsMissingCredentials() {
        for (String missing : new String[]{null, "", "   "}) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new EmailNotificationService(
                            "smtp.example.com",
                            587,
                            missing,
                            "password",
                            "sender@example.com",
                            "starttls"
                    )
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> new EmailNotificationService(
                            "smtp.example.com",
                            587,
                            "user",
                            missing,
                            "sender@example.com",
                            "starttls"
                    )
            );
        }
    }

    @Test
    void rejectsUnsupportedSecurityModes() {
        for (String security : new String[]{null, "", "none", "tls"}) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new EmailNotificationService(
                            "smtp.example.com",
                            587,
                            "user",
                            "password",
                            "sender@example.com",
                            security
                    )
            );
        }
    }

    @Test
    void acceptsSslConfigurationAndBuildsMessage() throws Exception {
        EmailNotificationService sslService = new EmailNotificationService(
                "smtp.example.com",
                465,
                "user",
                "password",
                "sender@example.com",
                "ssl"
        );

        MimeMessage message = sslService.createMessage(
                customer("customer@example.com"),
                "SSL message"
        );

        assertEquals("SSL message", message.getContent());
        assertEquals(
                "sender@example.com",
                message.getFrom()[0].toString()
        );
    }

    @Test
    void rejectsInvalidSenderAddresses() {
        String[] invalidSenders = {
                null,
                "",
                "   ",
                "sender",
                "sender@@example.com",
                "sender@example.com\r\nBcc: other@example.com"
        };

        for (String sender : invalidSenders) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new EmailNotificationService(
                            "smtp.example.com",
                            587,
                            "user",
                            "password",
                            sender,
                            "starttls"
                    )
            );
        }
    }
}