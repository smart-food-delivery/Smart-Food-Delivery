package com.smartfooddelivery.service;
import org.junit.jupiter.api.Test;
import com.smartfooddelivery.repository.UserRepository;
import com.smartfooddelivery.model.User;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import security.PasswordUtils;
public class LoginServiceTest {
    @Test
    void loginWithUnknownUsernameShouldFail() {
        UserRepository repository = new UserRepository();
        LoginService loginService = new LoginService(repository);
        User result = loginService.login("unknownUser", "123456");
        assertNull(result);

    }
    @Test
    void loginWithWrongPasswordShouldFail() {
        UserRepository repository = new UserRepository();
        LoginService loginService = new LoginService(repository);
        User result = loginService.login("customer1", "wrong123");
        assertNull(result);

    }
    @Test
    void loginWithCorrectCredentialsShouldSucceed() {
        UserRepository repository = new UserRepository();
        LoginService loginService = new LoginService(repository);
        User result = loginService.login("customer1", "123456");
        assertNotNull(result);
        assertEquals("Customer", result.getRole());

    }
    @Test
    void loginWithManagerCredentialsShouldSucceed() {
        UserRepository repository = new UserRepository();
        LoginService loginService = new LoginService(repository);

        User result = loginService.login("manager1", "123456");

        assertNotNull(result);
        assertEquals("Manager", result.getRole());
    }
    @Test
    void loginWithUnknownUserUsingMockitoShouldFail() {
        UserRepository repository = mock(UserRepository.class);
        LoginService loginService = new LoginService(repository);
        when(repository.findByUsername("unknownUser")).thenReturn(null);
        User result = loginService.login("unknownUser", "123456");
        assertNull(result);
    }
    @Test
    void loginWithWrongPasswordUsingMockitoShouldFail() {
        UserRepository repository = mock(UserRepository.class);
        LoginService loginService = new LoginService(repository);
        User user = new User(1, "customer1", "someHash", "Customer");
        when(repository.findByUsername("customer1")).thenReturn(user);
        User result = loginService.login("customer1", "wrong123");
        assertNull(result);
    }
    @Test
    void loginWithCorrectCredentialsUsingMockitoShouldSucceed() {
        UserRepository repository = mock(UserRepository.class);
        LoginService loginService = new LoginService(repository);
        User user = new User(
                1,
                "customer1",
                PasswordUtils.hash("123456"),
                "Customer"
        );
        when(repository.findByUsername("customer1")).thenReturn(user);
        User result = loginService.login("customer1", "123456");
        assertNotNull(result);
        assertEquals("Customer", result.getRole());

    }
}
