package com.jaba.p2_t.pms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jaba.p2_t.models.RoomModel;
import com.jaba.p2_t.repos.RoomRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepo roomRepo;

    public void addRoom(String id, boolean isRoom) {
        if (!roomRepo.existsById(id) && isRoom) {
            RoomModel rModel = new RoomModel();
            rModel.setId(id);
            rModel.setName(id);
            rModel.setStatus("dirty");
            roomRepo.save(rModel);
        }
    }

    public void setStatus(String id, String status) {
        RoomModel rModel = roomRepo.getReferenceById(id);
        switch (status) {
            case "clean":
            rModel.setStatus("Clean");
            break;
            case "dirty":
            rModel.setStatus("Dirty");
            break;
            case "OOO":
            rModel.setStatus("Out Of Order");
            break;
            case "OOS":
            rModel.setStatus("Out Of Service");
            break;
            case "inspected":
            rModel.setStatus("Inspected");
            break;
        }
        
        roomRepo.save(rModel);
    }

    public Map<String, Object> setName(String id, String name) {
        Map<String, Object> response = new HashMap<>();
        if (roomRepo.existsById(id)) {
            RoomModel rModel = roomRepo.getReferenceById(id);
            rModel.setName(name);
            roomRepo.save(rModel);
            response.put("success", true);
        } else {
            response.put("success", false);
        }
        return response;
    }

    public void deleteRoom(String id) {
        if (roomRepo.existsById(id))
            roomRepo.deleteById(id);
    }

    public List<RoomModel> listRooms() {
        return roomRepo.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

}
