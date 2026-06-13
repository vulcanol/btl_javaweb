package com.cuutruyen.service;

import com.cuutruyen.dto.AuthResponse;
import com.cuutruyen.dto.LoginRequest;
import com.cuutruyen.dto.RegisterRequest;
import com.cuutruyen.entity.User;
import com.cuutruyen.repository.UserRepository;
import com.cuutruyen.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setRole(User.Role.user); // Mặc định là user

        User savedUser = userRepository.save(user);

        return savedUser;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu không chính xác!");
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("Tài khoản đã bị vô hiệu hóa!");
        }

        if (!user.isAccountNonLocked()) {
            throw new RuntimeException("Tài khoản đã bị khóa!");
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUserId(), user.getUsername(), user.getRole().name());
    }
}
