package vn.iotstar;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;

@SpringBootApplication
public class Jwtspringboot3Application {

    public static void main(String[] args) {
        SpringApplication.run(Jwtspringboot3Application.class, args);
    }

    /**
     * Seeds initial user according to lecture slides if not already present in the database.
     */
    @Bean
    CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String defaultEmail = "trungnh@hcmute.edu.vn";
            if (userRepository.findByEmail(defaultEmail).isEmpty()) {
                User defaultUser = new User();
                defaultUser.setFullName("Nguyễn Hữu Trung");
                defaultUser.setEmail(defaultEmail);
                defaultUser.setPassword(passwordEncoder.encode("123456"));
                defaultUser.setImages("/images/avatar.png");
                userRepository.save(defaultUser);
                System.out.println(">>> Initialized default demo user: " + defaultEmail + " / 123456");
            }
        };
    }
}
