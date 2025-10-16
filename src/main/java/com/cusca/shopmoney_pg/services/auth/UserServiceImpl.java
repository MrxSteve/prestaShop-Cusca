package com.cusca.shopmoney_pg.services.auth;

import com.cusca.shopmoney_pg.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UsuarioRepository usuarioRepository;

    @Override
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByDui(String dui) {
        return usuarioRepository.existsByDui(dui);
    }
}
