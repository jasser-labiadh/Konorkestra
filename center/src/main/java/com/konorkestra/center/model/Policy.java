package com.konorkestra.center.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Policy {
    private String uid;                // Unique identifier for the policy
    private Integer minNodesRequired;  // Optional: used only for QUORUM type policies
    private String type;
    private String description;
}