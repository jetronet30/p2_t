package com.jaba.p2_t.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetModels {
    private String name;           
    private String ipAddress;       
    private String gateWay;        
    private String dns1;           
    private String dns2;           
    private String subNet;         
    private boolean status;         
    private String yamlFileName; 
    private String nickname;
    private String metric;
}
