package com.jaba.p2_t.pbxservices;

import org.springframework.stereotype.Service;

import com.jaba.p2_t.pbxrepos.PjsipAorRepositor;
import com.jaba.p2_t.pbxrepos.PjsipAuthRepositor;
import com.jaba.p2_t.pbxrepos.PjsipEndpointRepositor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExtensionService {
    private final PjsipEndpointRepositor pjsipEndpointRepositor;
    private final PjsipAuthRepositor pAuthRepositor;
    private final PjsipAorRepositor pRepositor;

    public void addExtension(String id,String ){
        if (pjsipEndpointRepositor.existsById(id)&&pAuthRepositor.existsById(id)&&pRepositor.existsById(id)) {
            
        }
    }

}
