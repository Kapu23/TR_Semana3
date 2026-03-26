package com.edgar.auth.services;
import com.edgar.auth.dto.LoginRequest;
import com.edgar.auth.dto.TokenResponse;

public interface AuthService {

    TokenResponse autenticar(LoginRequest request) throws Exception;
}
