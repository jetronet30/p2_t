package com.jaba.p2_t.pbxrepos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaba.p2_t.pbxmodels.Extension;

public interface ExtensionRepo extends JpaRepository<Extension, Long> {
    boolean existsByExtensionNumber(String extensionNumber);
    Extension findByExtensionNumber(String extensionNumber);
    
}
