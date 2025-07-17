package com.jaba.p2_t.pbxservices;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jaba.p2_t.pbxmodels.ExtenViModel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InboundService {
    private final VirtExtensionsService extensionsService;


    public Map<String, String> inboundCandidates() {
        Map<String, String> candidates = new LinkedHashMap<>();
        for (ExtenViModel ext : extensionsService.getVirtExts()) {
            candidates.put( ext.getId()+"-extension",ext.getId());
        }
        return candidates;
    }

}
