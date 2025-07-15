package com.jaba.p2_t.pbxservices;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jaba.p2_t.pbxmodels.ExtenViModel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InboundService {
    private final TrunkService trunkService;
    private final VirtExtensionsService extensionsService;

    private Map<String,String> inboundCandidates(){
        Map<String,String> candidates = new HashMap<>();
        for (ExtenViModel ext : extensionsService.getVirtExts()) {
            candidates.put("extensoin",ext.getId());
        }
        return candidates;
    }

}
