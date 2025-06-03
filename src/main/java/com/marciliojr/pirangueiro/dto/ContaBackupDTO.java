package com.marciliojr.pirangueiro.dto;

import lombok.Data;
import java.util.Base64;

/**
 * DTO para backup da entidade Conta.
 */
@Data
public class ContaBackupDTO {
    private Long id;
    private String nome;
    private String tipo; // TipoConta serializado como String
    private String imagemLogoBase64; // byte[] convertido para Base64
    
    public void setImagemLogo(byte[] imagemLogo) {
        if (imagemLogo != null && imagemLogo.length > 0) {
            this.imagemLogoBase64 = Base64.getEncoder().encodeToString(imagemLogo);
        }
    }
    
    public byte[] getImagemLogo() {
        if (imagemLogoBase64 != null && !imagemLogoBase64.isEmpty()) {
            return Base64.getDecoder().decode(imagemLogoBase64);
        }
        return null;
    }
} 