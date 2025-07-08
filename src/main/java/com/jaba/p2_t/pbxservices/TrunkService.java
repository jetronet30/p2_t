package com.jaba.p2_t.pbxservices;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.jaba.p2_t.pbxmodels.PjsipTrunk;
import com.jaba.p2_t.pbxrepos.PjsipTrunkRepositor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrunkService {

    private final PjsipTrunkRepositor trunkRepositor;

    public void addTrunk(PjsipTrunk trunk) {
        if (!trunkRepositor.existsById(trunk.getId())) {
            trunkRepositor.save(trunk);
        }
    }

    public boolean deleteTrunk(String id) {
        if (trunkRepositor.existsById(id)) {
            trunkRepositor.deleteById(id);
            return true;
        }
        return false;
    }

    public List<PjsipTrunk> getAllTrunks() {
        return trunkRepositor.findAll();
    }

    public boolean generatePjsipTrunks(String filePath) {
        List<PjsipTrunk> trunks = trunkRepositor.findAll();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (PjsipTrunk trunk : trunks) {
                String id = trunk.getId();

                // Endpoint
                writer.write("[" + id + "]");
                writer.newLine();
                writer.write("type=endpoint");
                writer.newLine();
                writer.write("transport=" + trunk.getTransport());
                writer.newLine();
                writer.write("context=" + trunk.getContext());
                writer.newLine();
                writer.write("disallow=all");
                writer.newLine();
                writer.write("allow=ulaw,alaw");
                writer.newLine();
                writer.write("outbound_auth=" + trunk.getOutboundAuth());
                writer.newLine();
                writer.write("aors=" + id);
                writer.newLine();
                writer.write("callerid=" + trunk.getCallerId());
                writer.newLine();

                // Optional: outbound_proxy და from_domain
                if (trunk.getFromDomain() != null && !trunk.getFromDomain().isBlank()) {
                    writer.write("from_domain=" + trunk.getFromDomain());
                    writer.newLine();
                }
                if (trunk.getOutboundProxy() != null && !trunk.getOutboundProxy().isBlank()) {
                    writer.write("outbound_proxy=" + trunk.getOutboundProxy());
                    writer.newLine();
                }

                writer.newLine();

                // Auth
                writer.write("[" + trunk.getOutboundAuth() + "]");
                writer.newLine();
                writer.write("type=auth");
                writer.newLine();
                writer.write("auth_type=userpass");
                writer.newLine();
                writer.write("username=" + trunk.getUsername());
                writer.newLine();
                writer.write("password=" + trunk.getPassword());
                writer.newLine();
                writer.newLine();

                // AOR
                writer.write("[" + id + "]");
                writer.newLine();
                writer.write("type=aor");
                writer.newLine();
                writer.write("contact=" + trunk.getContactUri());
                writer.newLine();
                writer.newLine();
            }

            writer.flush();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
