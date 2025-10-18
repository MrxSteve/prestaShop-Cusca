package com.cusca.shopmoney_pg.models.dto.request;

import com.cusca.shopmoney_pg.models.enums.TipoNotificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class NotificacionRequest {
    @NotNull(message = "El usuario es requerido")
    private Long usuarioId;

    @NotNull(message = "El tipo de notificación es requerido")
    private TipoNotificacion tipo;

    @NotBlank(message = "El asunto es requerido")
    @Size(max = 255, message = "El asunto no debe exceder 255 caracteres")
    private String asunto;

    @NotBlank(message = "El mensaje es requerido")
    private String mensaje;
}
