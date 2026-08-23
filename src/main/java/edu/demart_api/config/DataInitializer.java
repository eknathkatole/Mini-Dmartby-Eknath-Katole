package edu.demart_api.config;

import edu.demart_api.entity.Role;
import edu.demart_api.entity.User;
import edu.demart_api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner seedDefaultUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                // Seed Admin User
                if (!userRepository.existsByEmail("admin@dmart.com")) {
                    User admin = new User();
                    admin.setName("Super Admin");
                    admin.setEmail("admin@dmart.com");
                    admin.setPassword(passwordEncoder.encode("Admin@123"));
                    admin.setPhone("9999999999");
                    admin.setRole(Role.ADMIN);
                    admin.setActive(true);
                    userRepository.save(admin);
                    log.info("✅ Seeded default ADMIN user: admin@dmart.com / Admin@123");
                }

                // Seed Restaurant / Staff User
                if (!userRepository.existsByEmail("restaurant@dmart.com")) {
                    User restaurant = new User();
                    restaurant.setName("Restaurant & Store Manager");
                    restaurant.setEmail("restaurant@dmart.com");
                    restaurant.setPassword(passwordEncoder.encode("Restaurant@123"));
                    restaurant.setPhone("8888888888");
                    restaurant.setRole(Role.STAFF);
                    restaurant.setActive(true);
                    userRepository.save(restaurant);
                    log.info("✅ Seeded default RESTAURANT/STAFF user: restaurant@dmart.com / Restaurant@123");
                }
            } catch (Exception e) {
                log.warn("⚠️ DataInitializer user seeding skipped or partial: {}", e.getMessage());
            }
        };
    }
}
