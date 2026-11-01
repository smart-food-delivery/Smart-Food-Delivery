package com.smartfooddelivery.notification;

import com.smartfooddelivery.model.User;
import java.util.Scanner;

/** Explicitly invoked manual test: sends one real email to the supplied address. */
public final class EmailSmokeTest {
    private EmailSmokeTest() { }

    public static void main(String[] args) {
        if (args.length > 1) {
            throw new IllegalArgumentException("Supply at most one recipient email address");
        }
        String recipient;
        if (args.length == 0) {
            System.out.print("Recipient email (Enter sends one real test email): ");
            recipient = new Scanner(System.in).nextLine().trim();
        } else {
            recipient = args[0].trim();
        }
        if (recipient.isEmpty()) {
            System.out.println("No recipient entered. No email sent.");
            return;
        }
        EmailNotificationService.fromEnvironment().sendNotification(
                new User(0, "email-test", "", "Customer", recipient),
                "Test email from Smart Food Delivery. SMTP sending is working.");
        System.out.println("SMTP server accepted the test email.");
    }
}
