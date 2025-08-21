package com.jaba.p2_t.pms;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jaba.p2_t.models.ServiceCodeModel;
import com.jaba.p2_t.repos.ServiceCodeRepo;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceCodeService {
    private final ServiceCodeRepo serviceCodeRepo;

    @PostConstruct
    public void initServiceCodes() {
        if (serviceCodeRepo.count() == 0) {
            ServiceCodeModel serviceCodeModel = new ServiceCodeModel();
            serviceCodeModel.setId(1);
            serviceCodeModel.setClean("*61");
            serviceCodeModel.setDirty("*62");
            serviceCodeModel.setOutOfOrder("*63");
            serviceCodeModel.setOutOfService("*64");
            serviceCodeModel.setInspected("*65");
            serviceCodeModel.setAlarm("*001");
            serviceCodeRepo.save(serviceCodeModel);
        }
    }

    public ServiceCodeModel getServiceCode() {
        return serviceCodeRepo.getReferenceById(1);
    }

    public Map<String, Object> editServiceCodes(String clean,
            String dirty,
            String outOfOrder,
            String outOfService,
            String inspected,
            String alarm) {
        Map<String, Object> response = new HashMap<>();
        try {
            ServiceCodeModel model = serviceCodeRepo.getReferenceById(1);

            model.setClean(clean);
            model.setDirty(dirty);
            model.setOutOfOrder(outOfOrder);
            model.setOutOfService(outOfService);
            model.setAlarm(alarm);
            model.setInspected(inspected);

            serviceCodeRepo.save(model);

            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
        }
        return response;
    }

}
