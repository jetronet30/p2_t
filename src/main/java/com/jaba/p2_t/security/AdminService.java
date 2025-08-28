package com.jaba.p2_t.security;

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

}
