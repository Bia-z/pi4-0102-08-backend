# ADR 0001: Usar PostgreSQL como banco de dados relacional

**Status:** aceito

**Contexto:** O RestManager precisa persistir dados altamente estruturados e relacionais, como pedidos, itens do pedido, status de mesas, movimentações de caixa e pagamentos (incluindo split payment). É fundamental garantir integridade referencial, consistência ACID em transações financeiras do caixa e prevenção de perda de dados no ambiente operacional de restaurantes.

**Decisão:** Adotar o PostgreSQL como sistema gerenciador de banco de dados relacional (SGBD) da aplicação backend em Spring Boot.

**Alternativas consideradas:**
- MySQL: descartado por menor familiaridade e preferência técnica prévia da equipe em integrações com ecossistema Java/Spring Boot.
- Oracle Database: descartado por ser um SGBD proprietário, de alta complexidade de licenciamento/configuração e desnecessariamente pesado para o escopo e tamanho do projeto.

**Consequências:**
- Positivas: garantia de integridade referencial entre entidades críticas, forte suporte da comunidade, excelente integração nativa com Spring Data JPA/Hibernate e alta maturidade para execução via conteinerização em Docker.
- Negativas: necessidade de gerenciamento rigoroso de esquemas e migrações de dados à medida que o sistema evolui, além de maior consumo de recursos de memória em comparação a bancos relacionais mais simples.
