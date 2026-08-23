package edu.demart_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "staff_applications")
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffApplication extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    private String storeName;

    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffApplicationStatus status = StaffApplicationStatus.PENDING;

    private String adminNote;

    private String generatedPassword;
}
