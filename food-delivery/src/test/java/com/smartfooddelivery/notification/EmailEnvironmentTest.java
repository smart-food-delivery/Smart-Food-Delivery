package com.smartfooddelivery.notification;

import com.smartfooddelivery.model.User;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailEnvironmentTest {

    private Dotenv validEnvironment() {
        Dotenv env = mock(Dotenv.class);

        when(env.get("SMTP_HOST")).thenReturn("smtp.example.com");
        when(env.get("SMTP_PORT")).thenReturn("587");
        when(env.get("SMTP_USERNAME")).thenReturn("sender@example.com");
        when(env.get("SMTP_PASSWORD")).thenReturn("test-password");
        when(env.get("SMTP_FROM")).thenReturn("sender@example.com");
        when(env.get("SMTP_SECURITY", "starttls")).thenReturn("starttls");

        return env;
    }

    private DotenvBuilder builderFor(Dotenv env) {
        DotenvBuilder builder = mock(DotenvBuilder.class);

        when(builder.ignoreIfMissing()).thenReturn(builder);
        when(builder.load()).thenReturn(env);

        return builder;
    }

    @Test
    void createsServiceFromValidEnvironment() throws Exception {
        Dotenv env = validEnvironment();
        DotenvBuilder builder = builderFor(env);

        try (MockedStatic<Dotenv> dotenv = mockStatic(Dotenv.class)) {
            dotenv.when(Dotenv::configure).thenReturn(builder);

            EmailNotificationService service =
                    EmailNotificationService.fromEnvironment();

            User customer = new User(
                    1, "customer", "hash",
                    "Customer", "customer@example.com"
            );

            MimeMessage message = service.createMessage(
                    customer, "Order update"
            );

            assertEquals(
                    "sender@example.com",
                    message.getFrom()[0].toString()
            );
            assertEquals("Order update", message.getContent());
            verify(env).get("SMTP_SECURITY", "starttls");
        }
    }

    @Test
    void rejectsEachMissingRequiredSetting() {
        String[] requiredKeys = {
                "SMTP_HOST",
                "SMTP_PORT",
                "SMTP_USERNAME",
                "SMTP_PASSWORD",
                "SMTP_FROM"
        };

        for (String key : requiredKeys) {
            Dotenv env = validEnvironment();
            when(env.get(key)).thenReturn(null);

            DotenvBuilder builder = builderFor(env);

            try (MockedStatic<Dotenv> dotenv = mockStatic(Dotenv.class)) {
                dotenv.when(Dotenv::configure).thenReturn(builder);

                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        EmailNotificationService::fromEnvironment
                );

                assertEquals(
                        "Missing email setting: " + key,
                        exception.getMessage()
                );
            }
        }
    }

    @Test
    void rejectsEachBlankRequiredSetting() {
        String[] requiredKeys = {
                "SMTP_HOST",
                "SMTP_PORT",
                "SMTP_USERNAME",
                "SMTP_PASSWORD",
                "SMTP_FROM"
        };

        for (String key : requiredKeys) {
            Dotenv env = validEnvironment();
            when(env.get(key)).thenReturn("   ");

            DotenvBuilder builder = builderFor(env);

            try (MockedStatic<Dotenv> dotenv = mockStatic(Dotenv.class)) {
                dotenv.when(Dotenv::configure).thenReturn(builder);

                IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        EmailNotificationService::fromEnvironment
                );

                assertEquals(
                        "Missing email setting: " + key,
                        exception.getMessage()
                );
            }
        }
    }

    @Test
    void rejectsNonNumericPort() {
        Dotenv env = validEnvironment();
        when(env.get("SMTP_PORT")).thenReturn("invalid-port");

        DotenvBuilder builder = builderFor(env);

        try (MockedStatic<Dotenv> dotenv = mockStatic(Dotenv.class)) {
            dotenv.when(Dotenv::configure).thenReturn(builder);

            assertThrows(
                    NumberFormatException.class,
                    EmailNotificationService::fromEnvironment
            );
        }
    }
}