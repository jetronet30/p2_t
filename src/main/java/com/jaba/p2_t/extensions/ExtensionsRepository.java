package com.jaba.p2_t.extensions;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExtensionsRepository extends JpaRepository<Extensions, Long> {
    Extensions findByExten(String exten);
}
