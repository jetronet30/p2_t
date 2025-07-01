package com.jaba.p2_t.pbxservices;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jaba.p2_t.pbxmodels.Extension;
import com.jaba.p2_t.pbxrepos.ExtensionRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExtensionService {
    private final ExtensionRepo extensionRepo;
    private static final String SECRET = "minitelsy2";
    private static final String CONTEXT = "default";

    public void addExtension(String exten, String callerId, String context, String secret) {
        if (!extensionRepo.existsByExten(exten)) {
            Extension extension = new Extension();
            extension.setExten(exten);
            extension.setCallerId((callerId != null && !callerId.isEmpty()) ? callerId : exten);
            extension.setContext((context != null && !context.isEmpty()) ? context : CONTEXT);
            extension.setSecret((secret != null && !secret.isEmpty()) ? secret : SECRET);
            extensionRepo.save(extension);
        }
    }

    public void editExtension(Long id, String exten, String callerId, String context, String secret) {
        Extension current = extensionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Extension not found"));

        Extension existing = extensionRepo.findByExten(exten);
        if (existing != null && !existing.getId().equals(id)) {
            throw new IllegalArgumentException("Exten already exists for another record");
        }
        current.setExten(exten);
        current.setCallerId((callerId != null && !callerId.isEmpty()) ? callerId : exten);
        current.setContext((context != null && !context.isEmpty()) ? context : current.getContext());
        current.setSecret((secret != null && !secret.isEmpty()) ? secret : current.getSecret());

        extensionRepo.save(current);
    }

    public boolean deleteExtension(Long id) {
        if (extensionRepo.existsById(id)) {
            extensionRepo.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public List<Extension> getAllExtensions() {
        return extensionRepo.findAll();
    }

    public List<Extension> getAllExtensionsSorted() {
        return extensionRepo.findAll(Sort.by(Sort.Direction.ASC, "exten"));
    }

    public void addExtensionsRange(String startExten, String endExten) {
        int start;
        int end;
        try {
            start = Integer.parseInt(startExten);
            end = Integer.parseInt(endExten);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid start or end exten format, must be numeric");
        }
        if (start > end) {
            throw new IllegalArgumentException("Start exten must be less or equal to end exten");
        }

        for (int i = start; i <= end; i++) {
            String exten = String.valueOf(i);
            if (!extensionRepo.existsByExten(exten)) {
                Extension extension = new Extension();
                extension.setExten(exten);
                extension.setCallerId(exten);
                extension.setContext(CONTEXT);
                extension.setSecret(SECRET);
                extensionRepo.save(extension);
            }
        }
    }

}
