package com.jaba.p2_t.pbxservices;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional; // დამატებულია Optional კლასის იმპორტი, რომელიც გამოიყენება findById მეთოდის მიერ დაბრუნებული მნიშვნელობების უსაფრთხოდ დასამუშავებლად.

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaba.p2_t.pbxmodels.ExtenViModel;
import com.jaba.p2_t.pbxmodels.PjsipAor;
import com.jaba.p2_t.pbxmodels.PjsipAuth;
import com.jaba.p2_t.pbxmodels.PjsipEndpoint;
import com.jaba.p2_t.pbxrepos.ExtenVirtualRepo;
import com.jaba.p2_t.pbxrepos.PjsipAorRepositor;
import com.jaba.p2_t.pbxrepos.PjsipAuthRepositor;
import com.jaba.p2_t.pbxrepos.PjsipEndpointRepositor;

import lombok.RequiredArgsConstructor;

/**
 * სერვისის კლასი ვირტუალური გაფართოებების მართვისთვის.
 * იყენებს Spring Data JPA რეპოზიტორებს მონაცემთა ბაზასთან ურთიერთობისთვის.
 */
@Service
@RequiredArgsConstructor // Lombok-ის ანოტაცია, რომელიც ავტომატურად ქმნის კონსტრუქტორს ყველა final
                         // ველისთვის.
public class VirtExtensionsService {

    private final SipSettings sipSettings; // SIP პარამეტრების ინექცია
    private final PjsipEndpointRepositor pjsipEndpointRepositor; // PJSIP Endpoint რეპოზიტორი
    private final PjsipAuthRepositor pjsipAuthRepositor; // PJSIP Auth რეპოზიტორი
    private final PjsipAorRepositor pjsipAorRepositor; // PJSIP AOR რეპოზიტორი
    private final ExtenVirtualRepo extenVirtualRepo; // ვირტუალური გაფართოებების რეპოზიტორი

    /**
     * ქმნის ახალ ვირტუალურ გაფართოებას.
     *
     * @param extensionId გაფართოების უნიკალური ID.
     * @return true თუ გაფართოება წარმატებით შეიქმნა, false თუ ასეთი ID უკვე
     *         არსებობს.
     */
    @Transactional // უზრუნველყოფს, რომ მეთოდის ყველა ოპერაცია შესრულდეს ერთ ტრანზაქციაში.
    public boolean createVirtExt(String extensionId) {
        // ვამოწმებთ, არსებობს თუ არა უკვე ასეთი extensionId რომელიმე რეპოზიტორში.
        if (!extenVirtualRepo.existsById(extensionId) &&
                !pjsipEndpointRepositor.existsById(extensionId) &&
                !pjsipAuthRepositor.existsById(extensionId) &&
                !pjsipAorRepositor.existsById(extensionId)) {

            // ExtenViModel ობიექტის ინიციალიზაცია და პარამეტრების დაყენება
            ExtenViModel viModel = new ExtenViModel();
            viModel.setId(extensionId);
            viModel.setDisplayName(extensionId);
            viModel.setActive(false);
            viModel.setOutPermit(3);
            viModel.setVirContext("default");
            viModel.setVirPass(sipSettings.getDefPassword());

            // PjsipEndpoint ობიექტის ინიციალიზაცია და პარამეტრების დაყენება
            PjsipEndpoint endpoint = new PjsipEndpoint();
            endpoint.setId(extensionId);
            endpoint.setType("endpoint");
            endpoint.setTransport("udp");
            endpoint.setContext("default");
            endpoint.setDisallow("all");
            endpoint.setAllow("ulaw,alaw");
            endpoint.setDtmfMode(sipSettings.getDtmfMode());
            endpoint.setDirectMedia(false);
            endpoint.setCallerId(extensionId + "<" + extensionId + ">");

            // PjsipAuth ობიექტის ინიციალიზაცია და პარამეტრების დაყენება
            PjsipAuth auth = new PjsipAuth();
            auth.setId(extensionId);
            auth.setAuthType("userpass");
            auth.setUsername(extensionId);
            auth.setPassword(sipSettings.getDefPassword());

            // PjsipAor ობიექტის ინიციალიზაცია და პარამეტრების დაყენება
            PjsipAor aor = new PjsipAor();
            aor.setId(extensionId);
            aor.setMaxContacts(1);

            // ყველა მოდელის შენახვა მონაცემთა ბაზაში
            saveModels(viModel, endpoint, auth, aor);
            return true; // გაფართოება წარმატებით შეიქმნა
        }
        return false; // გაფართოება ასეთი ID-ით უკვე არსებობს
    }

    /**
     * ანახლებს არსებულ ვირტუალურ გაფართოებას.
     *
     * @param extensionId გაფართოების ID.
     * @param displayName გაფართოების ახალი სახელი.
     * @param virContext  ვირტუალური კონტექსტი.
     * @param virPass     ვირტუალური პაროლი.
     * @param outPermit   გამავალი ზარების ნებართვა.
     * @return Map, რომელიც შეიცავს "success" სტატუსს (true/false).
     */
    @Transactional // უზრუნველყოფს, რომ მეთოდის ყველა ოპერაცია შესრულდეს ერთ ტრანზაქციაში.
    public Map<String, Object> updateVirtExt(String extensionId, String displayName, String virContext, String virPass,
            int outPermit) {
        Map<String, Object> response = new HashMap<>();

        // ყველა საჭირო ობიექტის მოძიება findById-ის გამოყენებით.
        // ეს მიდგომა უფრო მდგრადია და ამცირებს მონაცემთა ბაზის ზედმეტ მოთხოვნებს,
        // ვიდრე ჯერ existsById-ის და შემდეგ findById-ის გამოძახება.
        Optional<ExtenViModel> viModelOpt = extenVirtualRepo.findById(extensionId);
        Optional<PjsipEndpoint> endpointOpt = pjsipEndpointRepositor.findById(extensionId);
        Optional<PjsipAuth> authOpt = pjsipAuthRepositor.findById(extensionId);
        Optional<PjsipAor> aorOpt = pjsipAorRepositor.findById(extensionId);

        // ვამოწმებთ, არსებობს თუ არა ყველა ობიექტი.
        // თუ რომელიმე Optional ცარიელია (ანუ ობიექტი ვერ მოიძებნა),
        // მაშინ განახლება ვერ მოხდება და ვაბრუნებთ წარუმატებლობის სტატუსს.
        if (viModelOpt.isEmpty() || endpointOpt.isEmpty() || authOpt.isEmpty() || aorOpt.isEmpty()) {
            response.put("success", false);
            // სურვილისამებრ, შეგიძლიათ დაამატოთ უფრო დეტალური შეტყობინება:
            // response.put("message", "ერთი ან მეტი დაკავშირებული ობიექტი ვერ მოიძებნა
            // extensionId-ისთვის: " + extensionId);
            return response;
        }

        // ვიღებთ რეალურ ობიექტებს Optional-დან.
        ExtenViModel viModel = viModelOpt.get();
        PjsipEndpoint endpoint = endpointOpt.get();
        PjsipAuth auth = authOpt.get();
        PjsipAor aor = aorOpt.get(); // 'aor' გადაეცემა saveModels-ს, თუმცა ამ მეთოდში პირდაპირ არ მოდიფიცირდება.

        // ExtenViModel-ის პარამეტრების განახლება
        // viModel.setId(extensionId); // ეს ხაზი ამოღებულია, რადგან ID არ უნდა
        // შეიცვალოს განახლების დროს.
        viModel.setDisplayName(displayName);
        viModel.setVirContext(virContext);
        viModel.setVirPass(virPass);
        viModel.setOutPermit(outPermit);

        // PjsipEndpoint-ის პარამეტრების განახლება
        endpoint.setCallerId(displayName + "<" + displayName + ">");
        endpoint.setContext(virContext);

        // PjsipAuth-ის პარამეტრების განახლება
        auth.setPassword(virPass);

        // განახლებული მოდელების შენახვა მონაცემთა ბაზაში
        saveModels(viModel, endpoint, auth, aor);

        response.put("success", true); // განახლება წარმატებით დასრულდა
        return response;
    }

    public Map<String, Object> deleteVirtExt(String extensionId) {
        Map<String, Object> response = new HashMap<>();
        Optional<ExtenViModel> viModelOpt = extenVirtualRepo.findById(extensionId);
        Optional<PjsipEndpoint> endpointOpt = pjsipEndpointRepositor.findById(extensionId);
        Optional<PjsipAuth> authOpt = pjsipAuthRepositor.findById(extensionId);
        Optional<PjsipAor> aorOpt = pjsipAorRepositor.findById(extensionId);

        if (viModelOpt.isEmpty() || endpointOpt.isEmpty() || authOpt.isEmpty() || aorOpt.isEmpty()) {
            response.put("success", false);
            // სურვილისამებრ, შეგიძლიათ დაამატოთ უფრო დეტალური შეტყობინება:
            // response.put("message", "ერთი ან მეტი დაკავშირებული ობიექტი ვერ მოიძებნა
            // extensionId-ისთვის: " + extensionId);
            return response;
        }
        extenVirtualRepo.delete(viModelOpt.get());
        pjsipEndpointRepositor.delete(endpointOpt.get());
        pjsipAuthRepositor.delete(authOpt.get());
        pjsipAorRepositor.delete(aorOpt.get());
        response.put("success", true); // დამხმარება წარმატებით დასრულდა
        return response;
    }

    /**
     * დამხმარე მეთოდი ყველა დაკავშირებული მოდელის მონაცემთა ბაზაში შესანახად.
     *
     * @param viModel  ExtenViModel ობიექტი.
     * @param endpoint PjsipEndpoint ობიექტი.
     * @param auth     PjsipAuth ობიექტი.
     * @param aor      PjsipAor ობიექტი.
     */
    private void saveModels(ExtenViModel viModel, PjsipEndpoint endpoint, PjsipAuth auth, PjsipAor aor) {
        extenVirtualRepo.save(viModel);
        pjsipEndpointRepositor.save(endpoint);
        pjsipAuthRepositor.save(auth);
        pjsipAorRepositor.save(aor);
    }
}
