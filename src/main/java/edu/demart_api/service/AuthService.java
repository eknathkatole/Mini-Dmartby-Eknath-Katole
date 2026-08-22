package edu.demart_api.service;

import edu.demart_api.dto.request.LoginRequest;
import edu.demart_api.dto.request.RegisterRequest;
import edu.demart_api.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}

