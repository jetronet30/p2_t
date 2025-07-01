package com.jaba.p2_t.extensions;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "extensions")
public class Extensions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Extension number or name (e.g. "1001", "2002")
    @Column(name = "exten", nullable = false, unique = true, length = 20)
    private String exten;

    // Context name in Asterisk dialplan (e.g. "default", "internal")
    @Column(name = "context", length = 50)
    private String context;

    // Priority in dialplan (e.g. 1, 2, 3)
    @Column(name = "priority")
    private Integer priority;

    // Application executed (e.g. Dial, Voicemail)
    @Column(name = "application", length = 50)
    private String application;

    // Application data or arguments (e.g. SIP/1001, PJSIP/1001)
    @Column(name = "app_data", length = 255)
    private String appData;

    // Optional description or comment
    @Column(name = "description", length = 255)
    private String description;

    // დამხმარე მეთოდი - ავტომატურად აყენებს appData-ს PJSIP ფორმატში
    public void setPjsipAppData() {
        if (this.exten != null && !this.exten.isBlank()) {
            this.appData = "PJSIP/" + this.exten;
        }
    }
}
