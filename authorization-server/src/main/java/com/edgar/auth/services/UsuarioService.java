package com.edgar.auth.services;
import java.util.Set;

import com.edgar.auth.dto.UsuarioRequest;
import com.edgar.auth.dto.UsuarioResponse;

public interface UsuarioService {

    Set<UsuarioResponse> listar();

    UsuarioResponse registrar(UsuarioRequest request);

    UsuarioResponse eliminar(String username);
}
