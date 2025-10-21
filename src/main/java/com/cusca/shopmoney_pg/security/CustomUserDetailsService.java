package com.cusca.shopmoney_pg.security;

import com.cusca.shopmoney_pg.models.entities.UsuarioEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoUsuario;
import com.cusca.shopmoney_pg.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Inicializar los roles dentro de la transacción
        usuario.getRoles().size();

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities(mapRolesToAuthorities(usuario))
                .accountLocked(usuario.getEstado() == EstadoUsuario.SUSPENDIDO)
                .disabled(usuario.getEstado() == EstadoUsuario.INACTIVO)
                .build();
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(UsuarioEntity usuario) {
        return usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre().toUpperCase()))
                .collect(Collectors.toList());
    }
}

