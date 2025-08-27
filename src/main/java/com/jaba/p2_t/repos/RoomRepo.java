package com.jaba.p2_t.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaba.p2_t.models.RoomModel;

public interface RoomRepo extends JpaRepository<RoomModel, String>{

}
