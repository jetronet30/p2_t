package com.jaba.p2_t.pbxrepos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaba.p2_t.pbxmodels.PjsipContact;

public interface PjsipContactRepository extends JpaRepository<PjsipContact, String> {

    List<PjsipContact> findByEndpoint(String endpoint);
    void deleteByEndpoint(String endpoint);
}
