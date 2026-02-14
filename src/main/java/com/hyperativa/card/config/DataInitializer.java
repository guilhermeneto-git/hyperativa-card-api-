package com.hyperativa.card.config;

import com.hyperativa.card.model.User;
import com.hyperativa.card.repository.UserRepository;
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
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Verifica se já existem usuários
            if (userRepository.count() == 0) {
                log.info("=== Criando usuários padrão ===");

                // Usuário Admin
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@hyperativa.com");
                admin.setRole("ADMIN");
                admin.setEnabled(true);
                userRepository.save(admin);
                log.info("✓ Usuário 'admin' criado com sucesso");

                // Usuário comum
                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setEmail("user@hyperativa.com");
                user.setRole("USER");
                user.setEnabled(true);
                userRepository.save(user);
                log.info("✓ Usuário 'user' criado com sucesso");

                log.info("=== {} usuários criados ===", userRepository.count());
            } else {
                log.info("Usuários já existem no banco. Total: {}", userRepository.count());
            }
        };
    }
}

