package edu.demart_api.entity;

public enum OrderStatus {
    PLACED,
    CONFIRMED,
    PREPARING,
    READY_FOR_PICKUP,   // STORE_PICKUP only
    OUT_FOR_DELIVERY,   // HOME_DELIVERY only
    DELIVERED,
    CANCELLED
}
