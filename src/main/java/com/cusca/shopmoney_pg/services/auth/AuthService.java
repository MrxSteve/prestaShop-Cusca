package com.cusca.shopmoney_pg.services.auth;

import com.cusca.shopmoney_pg.models.dto.request.ChangePasswordRequest;
import com.cusca.shopmoney_pg.models.dto.request.LoginRequest;
import com.cusca.shopmoney_pg.models.dto.response.AuthResponse;
import com.cusca.shopmoney_pg.models.dto.response.UsuarioResponse;
import com.cusca.shopmoney_pg.models.entities.UsuarioEntity;
import com.cusca.shopmoney_pg.repositories.UsuarioRepository;
import com.cusca.shopmoney_pg.security.JwtService;
import com.cusca.shopmoney_pg.utils.exceptions.AuthenticationFailedException;
import com.cusca.shopmoney_pg.utils.exceptions.InvalidPasswordException;
import com.cusca.shopmoney_pg.utils.exceptions.PasswordMismatchException;
import com.cusca.shopmoney_pg.utils.exceptions.ResourceNotFoundException;
import com.cusca.shopmoney_pg.utils.exceptions.SamePasswordException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final IUserService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest request) {
        // Autenticar usuario
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Cargar detalles del usuario
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        // Generar token JWT
        String jwtToken = jwtService.generateToken(userDetails);

        // Obtener información del usuario
        UsuarioResponse usuario = usuarioService.buscarPorEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationFailedException("Usuario no encontrado después de la autenticación"));

        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .build();
    }

    public void logout() {
        // Para JWT stateless, simplemente limpiamos el contexto de seguridad
        // El cliente debe eliminar el token del lado del cliente
        SecurityContextHolder.clearContext();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // Obtener usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Validar contraseña actual
        if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getPassword())) {
            throw new InvalidPasswordException("La contraseña actual es incorrecta");
        }

        // Validar que las nuevas contraseñas coincidan
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("La nueva contraseña y su confirmación no coinciden");
        }

        // Validar que la nueva contraseña sea diferente a la actual
        if (passwordEncoder.matches(request.getNewPassword(), usuario.getPassword())) {
            throw new SamePasswordException("La nueva contraseña debe ser diferente a la actual");
        }

        // Actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usuario.getCreateUpdateStamp().setUpdatedAt(LocalDateTime.now());

        usuarioRepository.save(usuario);
    }

    public UsuarioResponse getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
