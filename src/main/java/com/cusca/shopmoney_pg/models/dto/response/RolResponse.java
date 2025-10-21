package com.cusca.shopmoney_pg.models.dto.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RolResponse {
    private Long id;
    private String nombre;
}
