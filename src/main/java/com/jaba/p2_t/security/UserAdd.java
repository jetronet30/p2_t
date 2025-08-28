package com.jaba.p2_t.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;



import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAdd implements UserDetailsService {
    private final AdminService adminService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserDetails adminDetails = User.builder()
        .username(adminService.getAdmin().getUserName())
        .password(adminService.getAdmin().getUserPassword())
        .roles(adminService.getAdmin().getRole())
        .build();
        return adminDetails; 
    }
}
