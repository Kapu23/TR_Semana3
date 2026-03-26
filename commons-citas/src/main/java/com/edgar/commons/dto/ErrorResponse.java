package com.edgar.commons.dto;

public record ErrorResponse(
        int codigo,
        String mensaje
) {
}