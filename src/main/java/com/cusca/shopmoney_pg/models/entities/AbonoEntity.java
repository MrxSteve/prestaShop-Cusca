package com.cusca.shopmoney_pg.models.entities;

import com.cusca.shopmoney_pg.models.enums.EstadoAbono;
import com.cusca.shopmoney_pg.models.enums.MetodoPago;
import com.cusca.shopmoney_pg.models.stamp.CreateUpdateStamp;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "abonos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AbonoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_cliente_id", nullable = false)
    @ToString.Exclude
    private CuentaClienteEntity cuentaCliente;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @CreationTimestamp
    @Column(name = "fecha_abono")
    private LocalDateTime fechaAbono;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    @Builder.Default
    private MetodoPago metodoPago = MetodoPago.EFECTIVO;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    @Builder.Default
    private EstadoAbono estado = EstadoAbono.APLICADO;

    @Embedded
    private CreateUpdateStamp createUpdateStamp;
}
