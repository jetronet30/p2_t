package com.jaba.p2_t.pms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.jaba.p2_t.asteriskmanager.AsteriskManager;
import com.jaba.p2_t.models.ServiceCodeModel;

import com.jaba.p2_t.repos.ServiceCodeRepo;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceCodeService {
    private final AsteriskManager asteriskManager;
    private final ServiceCodeRepo serviceCodeRepo;
    private static final File AGI_DIAL = new File("/etc/asterisk/agi_dial.conf");

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
            writeAGICodesDial();
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
        writeAGICodesDial();
        return response;
    }

    public void writeAGICodesDial() {
        if (AGI_DIAL.exists())
            AGI_DIAL.delete();
        try {
            AGI_DIAL.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(AGI_DIAL, true))) {

            writer.write("\n\n[default]\n\n");
            writer.write("; Clean (status = 1)\n");
            writer.write("exten => " + getServiceCode().getClean() + ",1,NoOp(Set room status CLEAN)\n");
            writer.write(" same => n,Set(ROOM=${CALLERID(num)})\n");
            writer.write(" same => n,Set(STATUS=1)\n");
            writer.write(" same => n,AGI(pms_update_http.agi,${ROOM},${STATUS})\n");
            writer.write(" same => n,Playback(en/demo-thanks)\n");
            writer.write(" same => n,Hangup()\n\n");
            writer.write("; Dirty (status = 2)\n");
            writer.write("exten => " + getServiceCode().getDirty() + ",1,NoOp(Set room status DIRTY)\n");
            writer.write(" same => n,Set(ROOM=${CALLERID(num)})\n");
            writer.write(" same => n,Set(STATUS=2)\n");
            writer.write(" same => n,AGI(pms_update_http.agi,${ROOM},${STATUS})\n");
            writer.write(" same => n,Playback(en/demo-thanks)\n");
            writer.write(" same => n,Hangup()\n\n");
            writer.write("; Out of Order (status = 3)\n");
            writer.write("exten => " + getServiceCode().getOutOfOrder() + ",1,NoOp(Set room status OUT OF ORDER)\n");
            writer.write(" same => n,Set(ROOM=${CALLERID(num)})\n");
            writer.write(" same => n,Set(STATUS=3)\n");
            writer.write(" same => n,AGI(pms_update_http.agi,${ROOM},${STATUS})\n");
            writer.write(" same => n,Playback(en/demo-thanks)\n");
            writer.write(" same => n,Hangup()\n\n");
            writer.write("; Out of Service (status = 4)\n");
            writer.write(
                    "exten => " + getServiceCode().getOutOfService() + ",1,NoOp(Set room status OUT OF SERVICE)\n");
            writer.write(" same => n,Set(ROOM=${CALLERID(num)})\n");
            writer.write(" same => n,Set(STATUS=4)\n");
            writer.write(" same => n,AGI(pms_update_http.agi,${ROOM},${STATUS})\n");
            writer.write(" same => n,Playback(en/demo-thanks)\n");
            writer.write(" same => n,Hangup()\n\n");
            writer.write("; Inspected (status = 5)\n");
            writer.write("exten => " + getServiceCode().getInspected() + ",1,NoOp(Set room status INSPECTED)\n");
            writer.write(" same => n,Set(ROOM=${CALLERID(num)})\n");
            writer.write(" same => n,Set(STATUS=5)\n");
            writer.write(" same => n,AGI(pms_update_http.agi,${ROOM},${STATUS})\n");
            writer.write(" same => n,Playback(en/demo-thanks)\n");
            writer.write(" same => n,Hangup()\n\n");
            writer.write("; Alarm\n");
            writer.write("exten => " + getServiceCode().getAlarm() + ",1,NoOp(Trigger ALARM)\n");
            writer.write(" same => n,Set(ROOM=${CALLERID(num)})\n");
            writer.write(" same => n,AGI(pms_update_http.agi,${ROOM})\n");
            writer.write(" same => n,Playback(en/demo-thanks)\n");
            writer.write(" same => n,Hangup()\n\n");

        } catch (IOException e) {
            e.printStackTrace();
        }

        asteriskManager.reloadDialplan();
    }

}
