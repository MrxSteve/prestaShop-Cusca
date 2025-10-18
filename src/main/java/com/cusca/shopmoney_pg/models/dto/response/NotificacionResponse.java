package com.cusca.shopmoney_pg.models.dto.response;

import com.cusca.shopmoney_pg.models.enums.TipoNotificacion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class NotificacionResponse {
    private Long id;
    private UsuarioResponse usuario;
    private TipoNotificacion tipo;
    private String asunto;
    private String mensaje;
    private LocalDateTime fechaEnvio;
}
