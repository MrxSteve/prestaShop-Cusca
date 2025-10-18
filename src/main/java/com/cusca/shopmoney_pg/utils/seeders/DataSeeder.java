package com.cusca.shopmoney_pg.utils.seeders;

import com.cusca.shopmoney_pg.models.entities.RolEntity;
import com.cusca.shopmoney_pg.models.entities.UsuarioEntity;
import com.cusca.shopmoney_pg.models.enums.EstadoUsuario;
import com.cusca.shopmoney_pg.models.stamp.CreateUpdateStamp;
import com.cusca.shopmoney_pg.repositories.RolRepository;
import com.cusca.shopmoney_pg.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;
    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedUsuarios();
    }

    private void seedRoles() {
        log.info("Iniciando seed de roles...");

        // Crear rol ADMIN si no existe
        if (!rolRepository.existsByNombreIgnoreCase("ADMIN")) {
            RolEntity adminRole = RolEntity.builder()
                    .nombre("ADMIN")
                    .build();
            rolRepository.save(adminRole);
            log.info("Rol ADMIN creado exitosamente");
        } else {
            log.info("Rol ADMIN ya existe, omitiendo creación");
        }

        // Crear rol CLIENTE si no existe
        if (!rolRepository.existsByNombreIgnoreCase("CLIENTE")) {
            RolEntity clienteRole = RolEntity.builder()
                    .nombre("CLIENTE")
                    .build();
            rolRepository.save(clienteRole);
            log.info("Rol CLIENTE creado exitosamente");
        } else {
            log.info("Rol CLIENTE ya existe, omitiendo creación");
        }

        log.info("Seed de roles completado");
    }

    private void seedUsuarios() {
        log.info("Iniciando seed de usuarios...");

        // Obtener roles
        RolEntity rolAdmin = rolRepository.findByNombreIgnoreCase("ADMIN")
                .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));
        RolEntity rolCliente = rolRepository.findByNombreIgnoreCase("CLIENTE")
                .orElseThrow(() -> new RuntimeException("Rol CLIENTE no encontrado"));

        // Crear usuario ADMIN si no existe
        if (!usuarioRepository.existsByEmail(adminEmail)) {
            UsuarioEntity admin = UsuarioEntity.builder()
                    .nombreCompleto("Administrador del Sistema")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .telefono("6157-2882")
                    .direccion("Oficina Central, San Salvador, El Salvador")
                    .dui("12345678-9")
                    .fechaNacimiento(LocalDate.of(1985, 1, 15))
                    .estado(EstadoUsuario.ACTIVO)
                    .roles(List.of(rolAdmin))
                    .createUpdateStamp(CreateUpdateStamp.builder()
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build())
                    .build();
            usuarioRepository.save(admin);
            log.info("Usuario ADMIN creado: {}", admin.getEmail());
        } else {
            log.info("Usuario ADMIN ya existe, omitiendo creación");
        }

        // Crear usuarios CLIENTE de prueba
        crearClienteSiNoExiste("María Elena García", "maria.garcia@email.com", "cliente123",
                "7555-1234", "Col. Escalón, San Salvador", "98765432-1",
                LocalDate.of(1990, 3, 20), rolCliente);

        crearClienteSiNoExiste("Juan Carlos Mendoza", "juan.mendoza@email.com", "cliente456",
                "7555-5678", "Col. San Benito, San Salvador", "87654321-2",
                LocalDate.of(1985, 7, 10), rolCliente);

        crearClienteSiNoExiste("Ana Sofía Rodríguez", "ana.rodriguez@email.com", "cliente789",
                "7555-9012", "Col. Maquilishuat, San Salvador", "76543210-3",
                LocalDate.of(1992, 11, 5), rolCliente);

        log.info("Seed de usuarios completado");
    }

    private void crearClienteSiNoExiste(String nombre, String email, String password,
                                        String telefono, String direccion, String dui,
                                        LocalDate fechaNacimiento, RolEntity rolCliente) {
        if (!usuarioRepository.existsByEmail(email)) {
            UsuarioEntity cliente = UsuarioEntity.builder()
                    .nombreCompleto(nombre)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .telefono(telefono)
                    .direccion(direccion)
                    .dui(dui)
                    .fechaNacimiento(fechaNacimiento)
                    .estado(EstadoUsuario.ACTIVO)
                    .roles(List.of(rolCliente))
                    .createUpdateStamp(CreateUpdateStamp.builder()
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build())
                    .build();
            usuarioRepository.save(cliente);
            log.info("Cliente creado: {} - {}", nombre, email);
        } else {
            log.info("Cliente {} ya existe, omitiendo creación", email);
        }
    }
}
