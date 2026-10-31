package com.smartfooddelivery.repository;
import com.smartfooddelivery.model.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
public class UserRepository {
    public User findByUsername(String username) {
        InputStream input = getClass().getResourceAsStream("/users.txt");
        if (input == null) {
            throw new IllegalStateException("users.txt file not found");
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length != 4 && parts.length != 5) {
                    continue;
                }
                if (parts[1].trim().equals(username)) {
                    int id;

                    try {
                        id = Integer.parseInt(parts[0].trim());
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    return new User(
                            id,
                            parts[1].trim(),
                            parts[2].trim(),
                            parts[3].trim(),
                            parts.length == 5 ? parts[4].trim() : null
                    );
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException("Error reading users.txt", e);
        }
        return null;
    }
}
