package edu.demart_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 * Embedded delivery address — stored directly as columns in the orders table.
 * Populated only for HOME_DELIVERY orders; all columns are nullable.
 */
@Embeddable
@Getter
@Setter
public class DeliveryAddress {

    @Column(name = "delivery_street")
    private String street;

    @Column(name = "delivery_city")
    private String city;

    @Column(name = "delivery_state")
    private String state;

    @Column(name = "delivery_pincode", length = 6)
    private String pincode;

    @Column(name = "delivery_landmark")
    private String landmark;
}
