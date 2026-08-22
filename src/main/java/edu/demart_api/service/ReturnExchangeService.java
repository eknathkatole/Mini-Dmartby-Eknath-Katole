package edu.demart_api.service;

import edu.demart_api.dto.request.ProcessReturnRequest;
import edu.demart_api.dto.request.ReturnExchangeRequest;
import edu.demart_api.dto.response.ReturnExchangeResponse;

import java.util.List;

public interface ReturnExchangeService {

    // ─── Customer ─────────────────────────────────────────────────────────────

    /**
     * Submit a return or exchange request for an order item.
     * Validates: order ownership, order is DELIVERED, within 7-day window,
     * item hasn't already been returned/exchanged.
     */
    ReturnExchangeResponse requestReturn(Long userId, Long orderId, ReturnExchangeRequest request);

    /** All return/exchange requests submitted by the customer */
    List<ReturnExchangeResponse> getMyRequests(Long userId);

    // ─── Staff / Admin ────────────────────────────────────────────────────────

    /** All requests, optionally filtered by status */
    List<ReturnExchangeResponse> getAllRequests(String status);

    /**
     * Approve or reject a return/exchange.
     * On APPROVE:
     *  - RETURN: restocks original product
     *  - EXCHANGE: restocks original product + decrements target product stock
     * On REJECT: marks as REJECTED with staffNote.
     */
    ReturnExchangeResponse processRequest(Long requestId, ProcessReturnRequest request);
}
