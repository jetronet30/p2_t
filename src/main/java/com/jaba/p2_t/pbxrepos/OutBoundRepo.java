package com.jaba.p2_t.pbxrepos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaba.p2_t.pbxmodels.OutBoundRouteModel;

public interface OutBoundRepo extends JpaRepository<OutBoundRouteModel,String>{
    Optional<OutBoundRouteModel> findByPrefixAndTrunkId(String prefix, String trunkId);
}
