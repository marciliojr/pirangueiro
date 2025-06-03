package com.marciliojr.pirangueiro.service;

import com.marciliojr.pirangueiro.model.Usuario;
import com.marciliojr.pirangueiro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HistoricoService historicoService;

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public Optional<Usuario> buscarPorNome(String nome) {
        return usuarioRepository.findByNome(nome);
    }

    public Usuario salvar(Usuario usuario) {
        if (usuario.getId() == null && usuarioRepository.existsByNome(usuario.getNome())) {
            throw new RuntimeException("Já existe um usuário com este nome");
        }
        
        Usuario salvo = usuarioRepository.save(usuario);
        
        // Registrar no histórico
        try {
            if (usuario.getId() == null) {
                // Criação
                historicoService.registrarCriacaoUsuario(salvo.getId(), salvo.toString(), null);
            } else {
                // Edição
                historicoService.registrarEdicaoUsuario(salvo.getId(), salvo.toString(), null);
            }
        } catch (Exception e) {
            // Log do erro mas não falha a operação principal
            System.err.println("Erro ao registrar histórico: " + e.getMessage());
        }
        
        return salvo;
    }

    public void excluir(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado");
        }
        
        try {
            // Buscar o usuário antes de excluir para registrar no histórico
            Usuario usuario = usuarioRepository.findById(id).orElse(null);
            
            usuarioRepository.deleteById(id);
            
            // Registrar exclusão no histórico
            if (usuario != null) {
                historicoService.registrarExclusaoUsuario(id, usuario.toString(), null);
            }
        } catch (Exception e) {
            // Log do erro
            System.err.println("Erro ao excluir usuário ou registrar histórico: " + e.getMessage());
            throw e;
        }
    }

    public boolean existePorNome(String nome) {
        return usuarioRepository.existsByNome(nome);
    }
} 