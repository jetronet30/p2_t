package com.jaba.p2_t.pbxservices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jaba.p2_t.asteriskmanager.AsteriskManager;
import com.jaba.p2_t.pbxmodels.OutBoundRouteModel;
import com.jaba.p2_t.pbxrepos.OutBoundRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutBoundService {
    private static final File OUT_BOUND_ROUTES_CONF = new File("/etc/asterisk/outbounds.conf");
    private final OutBoundRepo oBoundRepo;
    private final AsteriskManager asteriskManager;

    public boolean addOutBondRoute(String name, String prefix, String autoAdd, String digits, String trunkId) {

        OutBoundRouteModel routeModel = new OutBoundRouteModel();
        routeModel.setId(name);
        routeModel.setPrefix(prefix);
        routeModel.setAutoAdd(autoAdd);
        routeModel.setDigits(digits);
        routeModel.setTrunkId(trunkId);
        oBoundRepo.save(routeModel);
        writeinfile();
        return true;
    }

    public Map<String, Object> editOutBoundRoute(String id, String prefix, String autoAdd, String digits,
            String trunkId) {
        Map<String, Object> response = new HashMap<>();

        OutBoundRouteModel existingRoute = oBoundRepo.findById(id).orElse(null);
        if (existingRoute == null) {
            response.put("success", false);
            return response;
        }

        existingRoute.setPrefix(prefix);
        existingRoute.setAutoAdd(autoAdd);
        existingRoute.setDigits(digits);
        existingRoute.setTrunkId(trunkId);
        oBoundRepo.save(existingRoute);

        response.put("success", true);
        writeinfile();
        return response;
    }

    public Map<String, Object> deletOutBoundRoute(String id) {
        Map<String, Object> response = new HashMap<>();
        if (oBoundRepo.existsById(id)) {
            oBoundRepo.deleteById(id);
            writeinfile();
            response.put("success", true);
        } else {
            response.put("success", false);
        }

        return response;
    }

    public List<OutBoundRouteModel> listAllOutBoundRoutes() {
        return oBoundRepo.findAll();
    }

    private void writeinfile() {
        if (OUT_BOUND_ROUTES_CONF.exists())
            OUT_BOUND_ROUTES_CONF.delete();
        try {
            OUT_BOUND_ROUTES_CONF.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_BOUND_ROUTES_CONF, true))) {
            writer.write("\n\n[default]\n\n");

            for (OutBoundRouteModel ob : oBoundRepo.findAll()) {
                String digits = ob.getDigits(); // ვალიდურია
                String trunk = ob.getTrunkId(); // ვალიდურია
                String prefix = ob.getPrefix(); // შეიძლება იყოს null/ცარიელი
                String autoAdd = ob.getAutoAdd(); // შეიძლება იყოს null/ცარიელი

                String pattern;
                if (prefix != null && !prefix.isEmpty()) {
                    pattern = prefix + digits;
                } else {
                    pattern = digits;
                }

                // ახლა მხოლოდ ერთხელ ვწერთ exten-ს და პრიორიტეტებს
                writer.write("exten => _" + pattern + ",1,NoOp(Dialing outbound call from ${CALLERID(num)})\n");
                writer.write(" same => n,GoSub(allow-outbound-users,${CALLERID(num)},1)\n");

                if (prefix != null && !prefix.isEmpty()) {
                    // Dial-ში ამოვიღოთ prefix (autoAdd-ს შემთხვევაში, ასევე ჩავსვათ autoAdd)
                    writer.write(" same => n,Set(OUTNUM=");
                    if (autoAdd != null && !autoAdd.isEmpty()) {
                        writer.write(autoAdd + "${EXTEN:" + prefix.length() + "}");
                    } else {
                        writer.write("${EXTEN:" + prefix.length() + "}");
                    }
                    writer.write(")\n");
                } else {
                    // როცა prefix არ არის, Set OUTNUM ჩვეულებრივია
                    if (autoAdd != null && !autoAdd.isEmpty()) {
                        writer.write(" same => n,Set(OUTNUM=" + autoAdd + "${EXTEN})\n");
                    } else {
                        writer.write(" same => n,Set(OUTNUM=${EXTEN})\n");
                    }
                }

                writer.write(" same => n,Dial(PJSIP/${OUTNUM}@trunk-" + trunk + "-sip)\n");
                writer.write(" same => n,Hangup()\n\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        asteriskManager.reloadDialplan();
    }

}
