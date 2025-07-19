package com.jaba.p2_t.pbxservices;

import java.util.ArrayList;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jaba.p2_t.pbxmodels.ExtenViModel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InboundService {
    private final VirtExtensionsService extensionsService;
    private final CallGroupService callGroupService;


    public List<String> inboundCandidates() {
        List<String> candidates = new ArrayList<>();
        candidates.clear();
        for (ExtenViModel ext : extensionsService.getVirtExts()) {
            candidates.add(ext.getId()+"-extension");
        }
        for (String callGroupId: callGroupService.getGroupIdSortedById()) {
            candidates.add(callGroupId+"-callgroup");
        }
        return candidates;
    }

}
