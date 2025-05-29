package com.marciliojr.pirangueiro.controller;

import com.marciliojr.pirangueiro.dto.UsuarioDTO;
import com.marciliojr.pirangueiro.model.Usuario;
import com.marciliojr.pirangueiro.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioDTO> listarTodos() {
        return usuarioService.listarTodos().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscarPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(convertToDTO(usuario));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> salvar(@RequestBody UsuarioDTO usuarioDTO) {
        Usuario usuario = convertToEntity(usuarioDTO);
        usuario = usuarioService.salvar(usuario);
        return ResponseEntity.ok(convertToDTO(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> atualizar(@PathVariable Long id, @RequestBody UsuarioDTO usuarioDTO) {
        usuarioDTO.setId(id);
        Usuario usuario = convertToEntity(usuarioDTO);
        usuario = usuarioService.salvar(usuario);
        return ResponseEntity.ok(convertToDTO(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/existe/{nome}")
    public ResponseEntity<Boolean> existePorNome(@PathVariable String nome) {
        return ResponseEntity.ok(usuarioService.existePorNome(nome));
    }

    private UsuarioDTO convertToDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        // Não retornar a senha no DTO por segurança
        return dto;
    }

    private Usuario convertToEntity(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setNome(dto.getNome());
        usuario.setSenha(dto.getSenha());
        return usuario;
    }
} 