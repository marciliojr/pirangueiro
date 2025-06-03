package com.marciliojr.pirangueiro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Notificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String mensagem;

    @Column(nullable = false)
    private LocalDateTime dataGeracao;

    @Column(nullable = false)
    private boolean lida;

    @ManyToOne
    @JoinColumn(name = "cartao_id", nullable = false)
    private Cartao cartao;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Notificacao{");
        sb.append("id=").append(id);
        sb.append(", mensagem='").append(mensagem).append('\'');
        sb.append(", dataGeracao=").append(dataGeracao);
        sb.append(", lida=").append(lida);
        if (cartao != null) {
            sb.append(", cartao='").append(cartao.getNome()).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
} 