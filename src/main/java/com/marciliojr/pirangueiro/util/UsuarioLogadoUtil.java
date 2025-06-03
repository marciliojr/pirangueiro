package com.marciliojr.pirangueiro.util;

import org.springframework.stereotype.Component;

/**
 * Utilitário para capturar informações do usuário logado.
 * 
 * Esta classe fornece métodos para obter o ID do usuário logado no sistema.
 * Por enquanto, retorna null indicando que não há autenticação implementada,
 * mas está preparada para futura integração com Spring Security ou outra solução de autenticação.
 * 
 * @author Marcilio Jr
 * @version 1.0
 * @since 1.0
 */
@Component
public class UsuarioLogadoUtil {

    /**
     * Obtém o ID do usuário logado no sistema.
     * 
     * Por enquanto retorna null, pois não há sistema de autenticação implementado.
     * Quando a autenticação for implementada, este método deve ser atualizado para:
     * 
     * - Spring Security: usar SecurityContextHolder.getContext().getAuthentication()
     * - JWT: extrair do token atual
     * - Session: obter da sessão HTTP
     * 
     * @return ID do usuário logado ou null se não houver usuário logado
     */
    public Long obterIdUsuarioLogado() {
        // TODO: Implementar captura do usuário logado quando autenticação for implementada
        // Por enquanto retorna null indicando que não há usuário logado
        return null;
        
        /*
         * Exemplo para futura implementação com Spring Security:
         * 
         * Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         * if (authentication != null && authentication.isAuthenticated() && 
         *     !"anonymousUser".equals(authentication.getPrincipal())) {
         *     
         *     if (authentication.getPrincipal() instanceof UserDetails) {
         *         UserDetails userDetails = (UserDetails) authentication.getPrincipal();
         *         // Assumindo que o username é o ID ou que existe um campo customizado
         *         return Long.valueOf(userDetails.getUsername());
         *     }
         * }
         * return null;
         */
    }

    /**
     * Verifica se há um usuário logado no sistema.
     * 
     * @return true se há usuário logado, false caso contrário
     */
    public boolean temUsuarioLogado() {
        return obterIdUsuarioLogado() != null;
    }

    /**
     * Obtém o ID do usuário logado ou um valor padrão se não houver usuário logado.
     * 
     * @param valorPadrao Valor a ser retornado se não houver usuário logado
     * @return ID do usuário logado ou valor padrão
     */
    public Long obterIdUsuarioLogadoOuPadrao(Long valorPadrao) {
        Long usuarioId = obterIdUsuarioLogado();
        return usuarioId != null ? usuarioId : valorPadrao;
    }
} 