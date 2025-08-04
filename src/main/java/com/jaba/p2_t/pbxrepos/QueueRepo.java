package com.jaba.p2_t.pbxrepos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaba.p2_t.pbxmodels.QueueModel;

public interface QueueRepo extends JpaRepository<QueueModel,String>{

}
