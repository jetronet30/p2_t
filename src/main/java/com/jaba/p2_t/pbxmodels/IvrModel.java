package com.jaba.p2_t.pbxmodels;


import java.util.List;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IvrModel {

    @Id
    private String id;

    @Column(name = "digits")
    private List<Integer> digits;

    @Column(name = "members")
    private List<String> members;

    @Column(name = "context")
    private String context = "default";

    @Column(name = "voiceMessage")
    private String voiceMessage;

}
