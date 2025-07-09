package com.jaba.p2_t.pbxrepos;

import com.jaba.p2_t.pbxmodels.PjsipRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PjsipRegistrationRepository extends JpaRepository<PjsipRegistration, String> {
    // აქ შეგიძლია დაამატო კასტომ რეპოზიტორული მეთოდები, თუ დაგჭირდება
}
