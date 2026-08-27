package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "system_configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {

    @Id
    @Column(name = "config_key", nullable = false)
    private String configKey;

    @Column(name = "config_value", nullable = false, length = 1000)
    private String configValue;

    private String description;
}
