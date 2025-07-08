package com.jaba.p2_t.pbxrepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import com.jaba.p2_t.pbxmodels.PjsipEndpoint;

import java.util.List;

public interface PjsipEndpointRepositor extends JpaRepository<PjsipEndpoint, String> {

    @Query("SELECT p.id FROM PjsipEndpoint p")
    List<String> findAllIds();
}