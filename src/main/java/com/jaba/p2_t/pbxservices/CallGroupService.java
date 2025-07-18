package com.jaba.p2_t.pbxservices;

import com.jaba.p2_t.pbxmodels.CallGroup;
import com.jaba.p2_t.pbxrepos.CallGroupRepo;
import com.jaba.p2_t.pbxrepos.ExtenVirtualRepo;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CallGroupService {
    private final CallGroupRepo callGroupRepo;
    private final ExtenVirtualRepo extenVirtualRepo;

    public List<String> getGroupIdSortedById() {
        List<CallGroup> groups = callGroupRepo.findAll();
        List<String> ids = new ArrayList<>();

        for (CallGroup group : groups) {
            ids.add(group.getId());
        }

        Collections.sort(ids);

        return ids;
    }

    public void createCallGroup(
            String member1,
            String member2,
            String member3,
            String member4,
            String member5,
            String member6,
            String member7,
            String member8,
            String member9,
            String member10) {
        // გამოიყენეთ LinkedHashSet, რათა შეინარჩუნოთ წყობა და თავიდან აიცილოთ
        // დუბლიკატები
        Set<String> uniqueValidMembers = new LinkedHashSet<>();
        for (String member : List.of(member1, member2, member3, member4, member5, member6, member7, member8, member9,
                member10)) {
            if (member != null && !member.isBlank() && extenVirtualRepo.existsById(member)) {
                uniqueValidMembers.add(member);
            }
        }

        // თუ არ არსებობს არც ერთი ვალიდური წევრი, გავდივართ
        if (uniqueValidMembers.isEmpty())
            return;

        // ვპოულობთ თავისუფალ ID-ს 100000-100090
        String availableId = null;
        for (int i = 100000; i <= 100900; i++) {
            String idStr = String.valueOf(i);
            if (!callGroupRepo.existsById(idStr)) {
                availableId = idStr;
                break;
            }
        }

        if (availableId == null) {
            throw new RuntimeException("ყველა CallGroup ID დაკავებულია (100000 - 100090)");
        }

        // ვქმნით ახალ CallGroup-ს
        CallGroup group = new CallGroup();
        group.setId(availableId);
        group.setContext("default");

        List<String> members = new ArrayList<>(uniqueValidMembers);
        if (members.size() > 0)
            group.setMember1(members.get(0));
        if (members.size() > 1)
            group.setMember2(members.get(1));
        if (members.size() > 2)
            group.setMember3(members.get(2));
        if (members.size() > 3)
            group.setMember4(members.get(3));
        if (members.size() > 4)
            group.setMember5(members.get(4));
        if (members.size() > 5)
            group.setMember6(members.get(5));
        if (members.size() > 6)
            group.setMember7(members.get(6));
        if (members.size() > 7)
            group.setMember8(members.get(7));
        if (members.size() > 8)
            group.setMember9(members.get(8));
        if (members.size() > 9)
            group.setMember10(members.get(9));

        callGroupRepo.save(group);
    }

    public Optional<CallGroup> findById(String id) {
        return callGroupRepo.findById(id);
    }

    public void deleteById(String id) {
        callGroupRepo.deleteById(id);
    }

}
