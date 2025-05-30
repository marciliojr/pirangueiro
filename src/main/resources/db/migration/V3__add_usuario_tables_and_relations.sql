-- Criar tabela Usuario
CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL
);

-- Criar tabela Historico
CREATE TABLE historico (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_operacao VARCHAR(50) NOT NULL,
    entidade VARCHAR(50) NOT NULL,
    entidade_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    data_hora DATETIME NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- Adicionar coluna usuario_id nas tabelas existentes
ALTER TABLE conta ADD COLUMN usuario_id BIGINT;
ALTER TABLE cartao ADD COLUMN usuario_id BIGINT;
ALTER TABLE despesa ADD COLUMN usuario_id BIGINT;
ALTER TABLE receita ADD COLUMN usuario_id BIGINT;

-- Criar um usuário padrão para dados existentes
INSERT INTO usuario (nome, senha) VALUES ('admin', 'admin123');

-- Atualizar registros existentes para referenciar o usuário padrão
UPDATE conta SET usuario_id = 1 WHERE usuario_id IS NULL;
UPDATE cartao SET usuario_id = 1 WHERE usuario_id IS NULL;
UPDATE despesa SET usuario_id = 1 WHERE usuario_id IS NULL;
UPDATE receita SET usuario_id = 1 WHERE usuario_id IS NULL;

-- Adicionar constraints de foreign key
ALTER TABLE conta ADD CONSTRAINT fk_conta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id);
ALTER TABLE cartao ADD CONSTRAINT fk_cartao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id);
ALTER TABLE despesa ADD CONSTRAINT fk_despesa_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id);
ALTER TABLE receita ADD CONSTRAINT fk_receita_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id);

-- Tornar as colunas NOT NULL após a atualização
ALTER TABLE conta MODIFY COLUMN usuario_id BIGINT NOT NULL;
ALTER TABLE cartao MODIFY COLUMN usuario_id BIGINT NOT NULL;
ALTER TABLE despesa MODIFY COLUMN usuario_id BIGINT NOT NULL;
ALTER TABLE receita MODIFY COLUMN usuario_id BIGINT NOT NULL; 