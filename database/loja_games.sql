-- =====================================================================
-- Loja Gamer/Nerd - Projeto final de Linguagem de Programacao 2
-- Esquema MySQL feito a partir do diagrama UML: cada classe vira uma tabela.
--
--   Vendedor            -> vendedor
--   Cliente             -> cliente
--   Produto             -> produto            (com quantidade_minima)
--   Venda               -> venda
--   ItemVenda           -> item_venda
--   Pagamento (abstract)-> pagamento
--   PagamentoDinheiro   -> pagamento_dinheiro
--   PagamentoPix        -> pagamento_pix
--   PagamentoDebito     -> pagamento_debito
--   PagamentoCredito    -> pagamento_credito
--   NotaFiscalCompra    -> nota_fiscal_compra
--   ItemNota            -> item_nota
--   Cotacao             -> cotacao            (NOVA)
--
-- ESTE SCRIPT PODE SER EXECUTADO QUANTAS VEZES QUISER, tanto em um
-- computador sem o banco quanto em um banco que ja existe:
--   * tabelas que ja existem sao mantidas (CREATE TABLE IF NOT EXISTS),
--     entao nenhum dado eh apagado;
--   * se a tabela produto ja existia sem a coluna quantidade_minima, a
--     coluna eh adicionada no final do script.
--
-- As tabelas estao na ordem certa: uma tabela so pode referenciar
-- (FOREIGN KEY) outra que ja foi criada antes.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS projetojava;
USE projetojava;

-- ---------------------------------------------------------------------
-- Vendedor
-- (a lista "vendas" do UML nao eh coluna: eh buscada na tabela venda
--  pelo vendedorID)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vendedor (
    vendedorID          INT AUTO_INCREMENT PRIMARY KEY,
    nomeVendedor        VARCHAR(100)  NOT NULL,
    telefone            VARCHAR(20),
    dataNasc            DATE,
    salarioBase         DECIMAL(10,2) NOT NULL DEFAULT 0,
    percentualComissao  DECIMAL(5,2)  NOT NULL DEFAULT 0,
    totalComissao       DECIMAL(10,2) NOT NULL DEFAULT 0
);

-- ---------------------------------------------------------------------
-- Cliente
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cliente (
    clientID     INT AUTO_INCREMENT PRIMARY KEY,
    nomeCliente  VARCHAR(100) NOT NULL,
    CPF          VARCHAR(14)  NOT NULL,
    email        VARCHAR(100),
    dataNasc     DATE         NOT NULL
);

-- ---------------------------------------------------------------------
-- Produto
-- (o lote nao fica aqui: fica em item_nota, como diz o UML)
--
-- quantidade_minima: controle de estoque. Quando, ao processar uma venda,
-- a quantidade do produto chega a este valor (ou fica abaixo dele), o
-- sistema pergunta se o produto deve ir para a Cotacao. Pode ser definida
-- ao cadastrar ou ao editar o produto.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS produto (
    produto_id          INT AUTO_INCREMENT PRIMARY KEY,
    nomeProduto         VARCHAR(100)  NOT NULL,
    marca               VARCHAR(50),
    valor               DECIMAL(10,2) NOT NULL,
    quantidade          INT           NOT NULL DEFAULT 0,
    ultima_data_compra  DATE,
    setor               VARCHAR(50)   NOT NULL,
    quantidade_minima   INT           NOT NULL DEFAULT 0
);

-- ---------------------------------------------------------------------
-- NotaFiscalCompra e ItemNota
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS nota_fiscal_compra (
    nota_id       INT AUTO_INCREMENT PRIMARY KEY,
    numero        VARCHAR(20)   NOT NULL,
    chave_acesso  VARCHAR(44)   NOT NULL,
    fornecedor    VARCHAR(100)  NOT NULL,
    data_entrada  DATE          NOT NULL,
    valor_total   DECIMAL(12,2) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS item_nota (
    item_nota_id    INT AUTO_INCREMENT PRIMARY KEY,
    nota_id         INT           NOT NULL,
    produto_id      INT           NOT NULL,
    qtd             INT           NOT NULL,
    valor_unitario  DECIMAL(10,2) NOT NULL,
    lote            VARCHAR(30),
    FOREIGN KEY (nota_id)    REFERENCES nota_fiscal_compra (nota_id),
    FOREIGN KEY (produto_id) REFERENCES produto (produto_id)
);

-- ---------------------------------------------------------------------
-- Cotacao
-- Lista de produtos que chegaram na quantidade minima e foram colocados
-- para cotacao (aba Cotacao do sistema).
--
--   id_cotacao          identificador da cotacao
--   data_cotacao        dia em que o produto foi colocado para cotacao
--   produto_id          produto cotado (nome vem de produto)
--   quantidade_cotacao  quanto sera comprado. Nasce 0 durante a venda e
--                       so eh definida depois, dentro da aba Cotacao
--   status              PENDENTE (padrao) ou COMPRADO
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cotacao (
    id_cotacao          INT AUTO_INCREMENT PRIMARY KEY,
    data_cotacao        DATE        NOT NULL,
    produto_id          INT         NOT NULL,
    quantidade_cotacao  INT         NOT NULL DEFAULT 0,
    status              VARCHAR(10) NOT NULL DEFAULT 'PENDENTE',
    FOREIGN KEY (produto_id) REFERENCES produto (produto_id)
);

-- ---------------------------------------------------------------------
-- Venda e ItemVenda
-- valor_unitario guarda o preco do produto NA HORA da venda.
-- finalizada: nao esta no UML, mas existe em Venda.java e eh usada pelo
-- VendedorDAOJDBC para calcular a comissao.
-- Ao finalizar a venda o sistema baixa a quantidade vendida de cada
-- produto (produto.quantidade) na mesma transacao.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS venda (
    venda_id     INT AUTO_INCREMENT PRIMARY KEY,
    clientID     INT           NOT NULL,
    vendedorID   INT           NOT NULL,
    data_venda   DATE          NOT NULL,
    valor_total  DECIMAL(10,2) NOT NULL DEFAULT 0,
    finalizada   BOOLEAN       NOT NULL DEFAULT FALSE,
    FOREIGN KEY (clientID)   REFERENCES cliente (clientID),
    FOREIGN KEY (vendedorID) REFERENCES vendedor (vendedorID)
);

CREATE TABLE IF NOT EXISTS item_venda (
    item_venda_id   INT AUTO_INCREMENT PRIMARY KEY,
    venda_id        INT           NOT NULL,
    produto_id      INT           NOT NULL,
    qtd             INT           NOT NULL,
    valor_unitario  DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (venda_id)   REFERENCES venda (venda_id),
    FOREIGN KEY (produto_id) REFERENCES produto (produto_id)
);

-- ---------------------------------------------------------------------
-- Pagamento (classe abstrata) e suas subclasses
-- A tabela pagamento guarda o que eh comum a todos. Cada subclasse tem
-- sua tabela com so o que eh dela, usando o MESMO pagamento_id.
-- A coluna "tipo" (DINHEIRO, PIX, DEBITO ou CREDITO) diz em qual tabela
-- de subclasse estao os dados daquele pagamento.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pagamento (
    pagamento_id    INT AUTO_INCREMENT PRIMARY KEY,
    venda_id        INT           NOT NULL,
    tipo            VARCHAR(10)   NOT NULL,
    valor           DECIMAL(10,2) NOT NULL,
    data_pagamento  DATE          NOT NULL,
    FOREIGN KEY (venda_id) REFERENCES venda (venda_id)
);

CREATE TABLE IF NOT EXISTS pagamento_dinheiro (
    pagamento_id    INT PRIMARY KEY,
    valor_recebido  DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pagamento_id) REFERENCES pagamento (pagamento_id)
);

CREATE TABLE IF NOT EXISTS pagamento_pix (
    pagamento_id  INT PRIMARY KEY,
    chave_pix     VARCHAR(77) NOT NULL,
    id_transacao  VARCHAR(50),
    FOREIGN KEY (pagamento_id) REFERENCES pagamento (pagamento_id)
);

CREATE TABLE IF NOT EXISTS pagamento_debito (
    pagamento_id     INT PRIMARY KEY,
    bandeira         VARCHAR(30) NOT NULL,
    ultimos_digitos  VARCHAR(4)  NOT NULL,
    FOREIGN KEY (pagamento_id) REFERENCES pagamento (pagamento_id)
);

CREATE TABLE IF NOT EXISTS pagamento_credito (
    pagamento_id     INT PRIMARY KEY,
    bandeira         VARCHAR(30)  NOT NULL,
    ultimos_digitos  VARCHAR(4)   NOT NULL,
    numero_parcelas  INT          NOT NULL,
    taxa_juros       DECIMAL(5,4) NOT NULL DEFAULT 0,
    FOREIGN KEY (pagamento_id) REFERENCES pagamento (pagamento_id)
);

-- ---------------------------------------------------------------------
-- ATUALIZACAO DE BANCO JA EXISTENTE
-- Se a tabela produto foi criada antes da quantidade minima existir, o
-- CREATE TABLE IF NOT EXISTS acima a manteve como estava (sem a coluna).
-- Este trecho adiciona quantidade_minima somente se ela ainda nao existir;
-- os produtos antigos ficam com minimo 0. (O MySQL nao tem
-- "ADD COLUMN IF NOT EXISTS", por isso a checagem em information_schema.)
-- ---------------------------------------------------------------------
SET @coluna_existe := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'produto'
      AND COLUMN_NAME  = 'quantidade_minima'
);

SET @sql_alterar := IF(@coluna_existe = 0,
    'ALTER TABLE produto ADD COLUMN quantidade_minima INT NOT NULL DEFAULT 0',
    'SELECT ''coluna quantidade_minima ja existe'' AS aviso');

PREPARE comando FROM @sql_alterar;
EXECUTE comando;
DEALLOCATE PREPARE comando;
