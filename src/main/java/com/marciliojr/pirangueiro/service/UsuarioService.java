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
        return usuarioRepository.save(usuario);
    }

    public void excluir(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    public boolean existePorNome(String nome) {
        return usuarioRepository.existsByNome(nome);
    }
} 