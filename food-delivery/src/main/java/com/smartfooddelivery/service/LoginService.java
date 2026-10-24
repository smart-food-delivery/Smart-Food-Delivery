package com.smartfooddelivery.service;
import com.smartfooddelivery.model.User;
import com.smartfooddelivery.repository.UserRepository;
import security.PasswordUtils;
public class LoginService {
    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return null;
        }

        if (!PasswordUtils.verify(password, user.getPasswordHash())) {
            return null;
        }
        return user;
    }

}
