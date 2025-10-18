package com.cusca.shopmoney_pg.models.entities;

import com.cusca.shopmoney_pg.models.enums.EstadoCuenta;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenerationTime;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cuentas_cliente")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CuentaClienteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true, nullable = false)
    @ToString.Exclude
    private UsuarioEntity usuario;

    @Column(name = "limite_credito", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Column(name = "saldo_actual", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal saldoActual = BigDecimal.ZERO;

    @Column(name = "saldo_disponible", precision = 10, scale = 2, insertable = false, updatable = false)
    @org.hibernate.annotations.Generated(GenerationTime.ALWAYS)
    private BigDecimal saldoDisponible;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDate fechaApertura;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    @Builder.Default
    private EstadoCuenta estado = EstadoCuenta.ACTIVA;

    @OneToMany(mappedBy = "cuentaCliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<VentaEntity> ventas = new ArrayList<>();

    @OneToMany(mappedBy = "cuentaCliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<AbonoEntity> abonos = new ArrayList<>();

    @OneToMany(mappedBy = "cuentaCliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<MovimientoCuentaEntity> movimientos = new ArrayList<>();
}
