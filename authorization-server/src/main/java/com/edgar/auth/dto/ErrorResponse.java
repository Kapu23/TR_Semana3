package com.edgar.auth.dto;
public record ErrorResponse(
        int codigo,
        String mensaje
) { }
