package com.cuutruyen;

import com.cuutruyen.entity.User;
import com.cuutruyen.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@SpringBootApplication
public class CuuTruyenApplication {

	public static void main(String[] args) {
		SpringApplication.run(CuuTruyenApplication.class, args);
	}

	@Bean
	public CommandLineRunner fixAdminRole(UserRepository userRepository) {
		return args -> {
			Optional<User> adminOpt = userRepository.findByUsername("admin");
			if (adminOpt.isPresent()) {
				User admin = adminOpt.get();
				if (admin.getRole() != User.Role.admin) {
					admin.setRole(User.Role.admin);
					userRepository.save(admin);
					System.out.println(">>> Đã tự động khôi phục quyền Admin cho tài khoản 'admin' <<<");
				}
			}
		};
	}
}
