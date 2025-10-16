package com.cusca.shopmoney_pg.models.entities;

import com.cusca.shopmoney_pg.models.enums.TipoMovimiento;
import com.cusca.shopmoney_pg.models.enums.TipoReferencia;
import com.cusca.shopmoney_pg.models.stamp.CreateUpdateStamp;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_cuenta")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MovimientoCuentaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_cliente_id", nullable = false)
    @ToString.Exclude
    private CuentaClienteEntity cuentaCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false)
    private TipoMovimiento tipoMovimiento;

    @Column(name = "concepto", nullable = false)
    private String concepto;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "saldo_anterior", nullable = false, precision = 10, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_nuevo", nullable = false, precision = 10, scale = 2)
    private BigDecimal saldoNuevo;

    @Enumerated(EnumType.STRING)
    @Column(name = "referencia_tipo")
    private TipoReferencia referenciaTipo;

    @CreationTimestamp
    @Column(name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private UsuarioEntity usuario;

    @Embedded
    private CreateUpdateStamp createUpdateStamp;
}
