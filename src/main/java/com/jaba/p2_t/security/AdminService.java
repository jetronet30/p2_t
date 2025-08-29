package com.jaba.p2_t.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepo adminRepo;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initAdminstrator() {
        if (adminRepo.count() == 0) {
            AdminModel aModel = new AdminModel();
            aModel.setId((long) 1);
            aModel.setUserName("admin");
            aModel.setUserPassword(passwordEncoder.encode("admin"));
            aModel.setRole("ADMIN");
            adminRepo.save(aModel);
        }
    }

    public AdminModel getAdmin() {
        return adminRepo.findById(1L)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
    }

    public Map<String, Object> editAdmin(String userName, String userPassword, String rePassword) {
    Map<String, Object> response = new HashMap<>();

    // --- Input validation ---
    if (userName == null || userName.trim().isEmpty()) {
        response.put("success", false);
        response.put("message", "Username cannot be empty");
        return response;
    }

    if (userPassword == null || rePassword == null || !userPassword.equals(rePassword)) {
        response.put("success", false);
        response.put("message", "Passwords do not match");
        return response;
    }

    if (userPassword.length() < 4) {
        response.put("success", false);
        response.put("message", "Password must be at least 8 characters");
        return response;
    }

    // --- Find Admin safely ---
    Optional<AdminModel> optionalAdmin = adminRepo.findById(1L);
    if (optionalAdmin.isEmpty()) {
        response.put("success", false);
        response.put("message", "Admin user not found");
        return response;
    }

    // --- Update securely ---
    AdminModel admin = optionalAdmin.get();
    admin.setUserName(userName.trim());
    admin.setUserPassword(passwordEncoder.encode(userPassword));
    adminRepo.save(admin);

    response.put("success", true);
    response.put("message", "Admin updated successfully");
    return response;
}


}
