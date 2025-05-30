-- Corrigir o tipo da coluna tipo_operacao para ser compatível com EnumType.STRING
ALTER TABLE historico MODIFY COLUMN tipo_operacao VARCHAR(50) NOT NULL; 