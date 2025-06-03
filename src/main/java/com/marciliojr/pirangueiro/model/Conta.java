package com.marciliojr.pirangueiro.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade que representa uma conta bancária.
 */
@Entity
@Data
public class Conta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private TipoConta tipo;

    @Lob
    @Column(name = "imagem_logo", columnDefinition = "LONGBLOB")
    private byte[] imagemLogo;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Conta{");
        sb.append("id=").append(id);
        sb.append(", nome='").append(nome).append('\'');
        sb.append(", tipo=").append(tipo);
        sb.append(", temImagem=").append(imagemLogo != null && imagemLogo.length > 0);
        sb.append('}');
        return sb.toString();
    }
} 