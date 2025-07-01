package com.jaba.p2_t.pbxmodels;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "extensions")
public class Extension {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 10)
    private String exten; // eg: "1001"

    @Column(nullable = false)
    private String secret;  // SIP password

    @Column(name = "caller_id")
    private String callerId;

    @Column(nullable = false)
    private String context = "default";

    private String technology = "PJSIP"; // or SIP

    private boolean enabled = false;
}
