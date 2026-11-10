# Plano de testes

## Testes automatizados

| Parte | Ferramenta | Como rodar | Onde |
| :--- | :--- | :--- | :--- |
| Backend | JUnit 5, Mockito e Spring Boot Test | `mvn verify` | `src/test/java` |
| Frontend | `node:test` | `npm test` | `tests/` no repositório do frontend |

O CI roda os testes do backend a cada push.

## Testes de aceitação

Ambiente: `docker compose up -d --build` com os dados de teste do `02-seed.sql`. Senha de todos os usuários: `restmanager123`.

| Entrega | Cenário | Resultado esperado |
| :--- | :--- | :--- |
| E1 | `admin` cadastra um funcionário com nome, CPF, telefone, código, perfil e senha | O funcionário aparece na lista e consegue entrar com o código e a senha |
| E1 | `garcom1` tenta abrir uma tela ou operação de administrador | A tela volta para a área do garçom e a API responde `403 ACESSO_NEGADO` |
| E1 | Login com o usuário `inativo` | Mensagem de usuário inativo |
| E2 | `admin` cadastra um produto com nome, preço e categoria, e uma mesa com número e capacidade | Produto e mesa aparecem no cardápio e no mapa de mesas do garçom |
| E3 | `garcom1` escolhe uma mesa disponível, adiciona itens com observação e envia | O pedido aparece registrado para a mesa e a mesa fica ocupada |
| E4 | Com a tela da cozinha aberta, o garçom envia um pedido | O pedido aparece na cozinha sem recarregar a página, na ordem de chegada |
| E5 | A cozinha clica em "Iniciar preparo" e depois em "Marcar como pronto" | O status muda na tela de acompanhamento do garçom em tempo real |
| E5 | Tentar voltar um item pronto para aguardando | A API recusa com `TRANSICAO_STATUS_INVALIDA` |
| E6 | `caixa` abre o mapa de mesas | Cada mesa mostra o status e o valor consumido |
| E7 | O caixa paga a conta com duas formas cuja soma é menor que o total | O pagamento é recusado com `VALOR_PAGAMENTO_INSUFICIENTE` |
| E7 | O caixa paga a conta com duas formas cuja soma cobre o total | A conta é fechada e a mesa volta a ficar disponível |
| E8 | Depois de fechar a conta, baixar o comprovante | O PDF traz os itens consumidos e o valor pago |
| E9 | O caixa transfere um pedido em aberto para uma mesa disponível | O pedido passa para a nova mesa com os mesmos itens e status |
| E14 | Com a mesa em pagamento, o garçom tenta lançar um pedido nela | A tela mostra "Mesa em pagamento" e a API recusa com `MESA_EM_PAGAMENTO` |
