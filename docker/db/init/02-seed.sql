-- =====================================================================
-- RestManager - Dados iniciais para DESENVOLVIMENTO e demonstração
-- Responsável: Lucas Ormenese Altieri
--
-- Usuários (todos com a senha: restmanager123)
--   codigo  | perfil
--   admin   | ADMIN
--   garcom1 | GARCOM
--   garcom2 | GARCOM
--   cozinha | COZINHA
--   caixa   | CAIXA
--   inativo | GARCOM (ativo = false, para testar USUARIO_INATIVO)
--
-- NÃO use estas senhas em produção.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Usuários (hash BCrypt de "restmanager123")
-- ---------------------------------------------------------------------
INSERT INTO tb_usuario (codigo, nome, email, senha, perfil, ativo) VALUES
    ('admin',   'Administrador',   'admin@restmanager.com',   '$2a$10$OJZ900Hq49PmfQlQD9G73eZnRQNx2SNH85e0Zp7.PG/7LYJGAVBwe', 'ADMIN',   TRUE),
    ('garcom1', 'Carlos Mendes',   'carlos@restmanager.com',  '$2a$10$OJZ900Hq49PmfQlQD9G73eZnRQNx2SNH85e0Zp7.PG/7LYJGAVBwe', 'GARCOM',  TRUE),
    ('garcom2', 'Ana Souza',       'ana@restmanager.com',     '$2a$10$OJZ900Hq49PmfQlQD9G73eZnRQNx2SNH85e0Zp7.PG/7LYJGAVBwe', 'GARCOM',  TRUE),
    ('cozinha', 'Equipe Cozinha',  'cozinha@restmanager.com', '$2a$10$OJZ900Hq49PmfQlQD9G73eZnRQNx2SNH85e0Zp7.PG/7LYJGAVBwe', 'COZINHA', TRUE),
    ('caixa',   'Paula Lima',      'paula@restmanager.com',   '$2a$10$OJZ900Hq49PmfQlQD9G73eZnRQNx2SNH85e0Zp7.PG/7LYJGAVBwe', 'CAIXA',   TRUE),
    ('inativo', 'Usuário Inativo', 'inativo@restmanager.com', '$2a$10$OJZ900Hq49PmfQlQD9G73eZnRQNx2SNH85e0Zp7.PG/7LYJGAVBwe', 'GARCOM',  FALSE);

-- ---------------------------------------------------------------------
-- Configuração (linha única)
-- ---------------------------------------------------------------------
INSERT INTO tb_configuracao (id, nome_restaurante, taxa_servico_percentual, imprimir_comanda_ao_aceitar)
VALUES (1, 'RestManager', 10.00, TRUE);

-- ---------------------------------------------------------------------
-- Mesas 1 a 10
-- ---------------------------------------------------------------------
INSERT INTO tb_mesa (numero, capacidade) VALUES
    (1, 2), (2, 2), (3, 4), (4, 4), (5, 4),
    (6, 4), (7, 6), (8, 6), (9, 8), (10, 4);

-- ---------------------------------------------------------------------
-- Cardápio
-- ---------------------------------------------------------------------
INSERT INTO tb_categoria (nome, ordem) VALUES
    ('Entradas', 1),
    ('Pratos principais', 2),
    ('Sobremesas', 3),
    ('Bebidas', 4);

INSERT INTO tb_ingrediente (nome) VALUES
    ('Pão italiano'), ('Tomate'), ('Manjericão'), ('Cebola'), ('Alho'),
    ('Picanha'), ('Arroz'), ('Feijão'), ('Batata'), ('Frango'),
    ('Massa'), ('Queijo parmesão'), ('Chocolate'), ('Leite condensado'), ('Limão');

INSERT INTO tb_produto (categoria_id, nome, descricao, preco)
SELECT c.id, p.nome, p.descricao, p.preco
FROM (VALUES
    ('Entradas',          'Bruschetta Italiana',  'Pão italiano com tomate, manjericão e azeite', 28.00),
    ('Entradas',          'Batata Frita',         'Porção de batata frita crocante',               24.00),
    ('Pratos principais', 'Picanha Grelhada',     'Picanha com arroz, feijão e batata',            89.00),
    ('Pratos principais', 'Frango à Parmegiana',  'Filé de frango empanado com molho e queijo',    54.00),
    ('Pratos principais', 'Espaguete ao Sugo',    'Massa com molho de tomate e manjericão',        42.00),
    ('Sobremesas',        'Petit Gâteau',         'Bolo de chocolate com sorvete',                 26.00),
    ('Sobremesas',        'Pudim',                'Pudim de leite condensado',                     18.00),
    ('Bebidas',           'Refrigerante Lata',    '350 ml',                                         7.00),
    ('Bebidas',           'Suco de Limão',        '500 ml',                                        12.00),
    ('Bebidas',           'Água sem Gás',         '500 ml',                                         5.00)
) AS p (categoria, nome, descricao, preco)
JOIN tb_categoria c ON c.nome = p.categoria;

INSERT INTO tb_produto_ingrediente (produto_id, ingrediente_id)
SELECT pr.id, i.id
FROM (VALUES
    ('Bruschetta Italiana', 'Pão italiano'), ('Bruschetta Italiana', 'Tomate'),
    ('Bruschetta Italiana', 'Manjericão'),   ('Bruschetta Italiana', 'Alho'),
    ('Batata Frita',        'Batata'),
    ('Picanha Grelhada',    'Picanha'),      ('Picanha Grelhada', 'Arroz'),
    ('Picanha Grelhada',    'Feijão'),       ('Picanha Grelhada', 'Batata'),
    ('Frango à Parmegiana', 'Frango'),       ('Frango à Parmegiana', 'Queijo parmesão'),
    ('Frango à Parmegiana', 'Tomate'),
    ('Espaguete ao Sugo',   'Massa'),        ('Espaguete ao Sugo', 'Tomate'),
    ('Espaguete ao Sugo',   'Manjericão'),
    ('Petit Gâteau',        'Chocolate'),
    ('Pudim',               'Leite condensado'),
    ('Suco de Limão',       'Limão')
) AS pi (produto, ingrediente)
JOIN tb_produto pr    ON pr.nome = pi.produto
JOIN tb_ingrediente i ON i.nome  = pi.ingrediente;

-- ---------------------------------------------------------------------
-- Um pedido em aberto na mesa 3, para testar o painel da cozinha (E4)
-- e o mapa de mesas (E6) sem precisar lançar nada antes.
-- ---------------------------------------------------------------------
UPDATE tb_mesa SET status = 'OCUPADA' WHERE numero = 3;

INSERT INTO tb_pedido (mesa_id, garcom_id, status, valor_total)
SELECT m.id, u.id, 'AGUARDANDO', 145.00
FROM tb_mesa m, tb_usuario u
WHERE m.numero = 3 AND u.codigo = 'garcom1';

INSERT INTO tb_item_pedido (pedido_id, produto_id, quantidade, preco_unitario, observacao)
SELECT ped.id, pr.id, it.quantidade, pr.preco, it.observacao
FROM (VALUES
    ('Bruschetta Italiana', 2, 'Sem cebola'),
    ('Picanha Grelhada',    1, 'Ao ponto')
) AS it (produto, quantidade, observacao)
JOIN tb_produto pr ON pr.nome = it.produto
CROSS JOIN (SELECT p.id FROM tb_pedido p JOIN tb_mesa m ON m.id = p.mesa_id WHERE m.numero = 3) AS ped;
-- total: 2 x 28,00 + 1 x 89,00 = 145,00
