package edu.demart_api.entity;

public enum ReturnExchangeStatus {
    PENDING,    // Customer submitted, awaiting staff review
    APPROVED,   // Staff approved — inventory adjusted, process complete
    REJECTED    // Staff rejected — item stays with customer
}
