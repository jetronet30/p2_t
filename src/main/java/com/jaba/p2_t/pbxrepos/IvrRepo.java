package com.jaba.p2_t.pbxrepos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaba.p2_t.pbxmodels.IvrModel;

public interface IvrRepo extends JpaRepository<IvrModel,String> {

}
