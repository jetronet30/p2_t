package com.jaba.p2_t.pbxrepos;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jaba.p2_t.pbxmodels.TrunkViModel;
import java.util.List;

public interface TrunkViModelRepository extends JpaRepository<TrunkViModel, String> {

    // საჭიროების შემთხვევაში შეგიძლია ისეთი მეთოდები დაამატო, როგორიცაა
    List<TrunkViModel> findByActiveTrue();

    // ან ტრანკის სახლით ძებნა
    TrunkViModel findByTrunkName(String trunkName);

}
