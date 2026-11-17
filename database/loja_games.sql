-- =====================================================================
-- Loja Gamer/Nerd - Projeto final de Linguagem de Programacao 2
-- Esquema MySQL feito a partir do diagrama UML: cada classe vira uma tabela.
--
--   Vendedor            -> vendedor
--   Cliente             -> cliente
--   Produto             -> produto
--   Venda               -> venda
--   ItemVenda           -> item_venda
--   Pagamento (abstract)-> pagamento
--   PagamentoDinheiro   -> pagamento_dinheiro
--   PagamentoPix        -> pagamento_pix
--   PagamentoDebito     -> pagamento_debito
--   PagamentoCredito    -> pagamento_credito
--   NotaFiscalCompra    -> nota_fiscal_compra
--   ItemNota            -> item_nota
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
CREATE TABLE vendedor (
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
CREATE TABLE cliente (
    clientID     INT AUTO_INCREMENT PRIMARY KEY,
    nomeCliente  VARCHAR(100) NOT NULL,
    CPF          VARCHAR(14)  NOT NULL,
    email        VARCHAR(100),
    dataNasc     DATE         NOT NULL
);

-- ---------------------------------------------------------------------
-- Produto
-- (o lote nao fica aqui: fica em item_nota, como diz o UML)
-- ---------------------------------------------------------------------
CREATE TABLE produto (
    produto_id          INT AUTO_INCREMENT PRIMARY KEY,
    nomeProduto         VARCHAR(100)  NOT NULL,
    marca               VARCHAR(50),
    valor               DECIMAL(10,2) NOT NULL,
    quantidade          INT           NOT NULL DEFAULT 0,
    ultima_data_compra  DATE,
    setor               VARCHAR(50)   NOT NULL
);

-- ---------------------------------------------------------------------
-- NotaFiscalCompra e ItemNota
-- ---------------------------------------------------------------------
CREATE TABLE nota_fiscal_compra (
    nota_id       INT AUTO_INCREMENT PRIMARY KEY,
    numero        VARCHAR(20)   NOT NULL,
    chave_acesso  VARCHAR(44)   NOT NULL,
    fornecedor    VARCHAR(100)  NOT NULL,
    data_entrada  DATE          NOT NULL,
    valor_total   DECIMAL(12,2) NOT NULL DEFAULT 0
);

CREATE TABLE item_nota (
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
-- Venda e ItemVenda
-- valor_unitario guarda o preco do produto NA HORA da venda.
-- finalizada: nao esta no UML, mas existe em Venda.java e eh usada pelo
-- VendedorDAOJDBC para calcular a comissao.
-- ---------------------------------------------------------------------
CREATE TABLE venda (
    venda_id     INT AUTO_INCREMENT PRIMARY KEY,
    clientID     INT           NOT NULL,
    vendedorID   INT           NOT NULL,
    data_venda   DATE          NOT NULL,
    valor_total  DECIMAL(10,2) NOT NULL DEFAULT 0,
    finalizada   BOOLEAN       NOT NULL DEFAULT FALSE,
    FOREIGN KEY (clientID)   REFERENCES cliente (clientID),
    FOREIGN KEY (vendedorID) REFERENCES vendedor (vendedorID)
);

CREATE TABLE item_venda (
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
CREATE TABLE pagamento (
    pagamento_id    INT AUTO_INCREMENT PRIMARY KEY,
    venda_id        INT           NOT NULL,
    tipo            VARCHAR(10)   NOT NULL,
    valor           DECIMAL(10,2) NOT NULL,
    data_pagamento  DATE          NOT NULL,
    FOREIGN KEY (venda_id) REFERENCES venda (venda_id)
);

CREATE TABLE pagamento_dinheiro (
    pagamento_id    INT PRIMARY KEY,
    valor_recebido  DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pagamento_id) REFERENCES pagamento (pagamento_id)
);

CREATE TABLE pagamento_pix (
    pagamento_id  INT PRIMARY KEY,
    chave_pix     VARCHAR(77) NOT NULL,
    id_transacao  VARCHAR(50),
    FOREIGN KEY (pagamento_id) REFERENCES pagamento (pagamento_id)
);

CREATE TABLE pagamento_debito (
    pagamento_id     INT PRIMARY KEY,
    bandeira         VARCHAR(30) NOT NULL,
    ultimos_digitos  VARCHAR(4)  NOT NULL,
    FOREIGN KEY (pagamento_id) REFERENCES pagamento (pagamento_id)
);

CREATE TABLE pagamento_credito (
    pagamento_id     INT PRIMARY KEY,
    bandeira         VARCHAR(30)  NOT NULL,
    ultimos_digitos  VARCHAR(4)   NOT NULL,
    numero_parcelas  INT          NOT NULL,
    taxa_juros       DECIMAL(5,4) NOT NULL DEFAULT 0,
    FOREIGN KEY (pagamento_id) REFERENCES pagamento (pagamento_id)
);