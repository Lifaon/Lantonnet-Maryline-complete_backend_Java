package com.pcs.app;

import com.pcs.app.config.SecurityConfiguration;
import com.pcs.app.domain.User;
import com.pcs.app.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

    @Bean
    public CommandLineRunner demo(UserService service) {

        return (args) -> {
            final PasswordEncoder encoder = new BCryptPasswordEncoder(SecurityConfiguration.getEncoderStrength());

            if (service.getAllUsers().stream().noneMatch(user ->
                    user.getUsername().equals("admin"))) {
                User admin = new User();
                admin.setFullname("Administrator");
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("AdminPass*8"));
                admin.setRole("ROLE_ADMIN");
                service.createUser(admin);
            }

            if (service.getAllUsers().stream().noneMatch(user ->
                    user.getUsername().equals("test_user"))) {
                User test_user = new User();
                test_user.setFullname("Test user");
                test_user.setUsername("test_user");
                test_user.setPassword(encoder.encode("TestPass*8"));
                test_user.setRole("ROLE_USER");
                service.createUser(test_user);
            }

        };
    }
}
