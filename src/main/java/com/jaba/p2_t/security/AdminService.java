package com.jaba.p2_t.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.jaba.p2_t.servermanager.SerManger;

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

    public Map<String, Object> editAdmin(String userName, String userPassword) {
        Map<String, Object> respons = new HashMap<>();
        if (adminRepo.existsById((long) 1)) {
            AdminModel admin = adminRepo.getReferenceById((long) 1);
            admin.setUserName(userName);
            admin.setUserPassword(passwordEncoder.encode(userPassword));
            adminRepo.save(admin);
            respons.put("success", true);
            SerManger.reboot();
        } else {
            respons.put("success", false);
        }
        return respons;
    }

}
