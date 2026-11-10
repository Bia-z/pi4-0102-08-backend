# Roteiro de demonstração

Mostra o critério de sucesso do escopo: o garçom lança um pedido, a cozinha recebe e atualiza o status em tempo real, e o caixa fecha a conta da mesa sem uso de papel.

## Preparação

1. Subir o sistema com `docker compose up -d --build` a partir de um banco novo (`docker compose down -v` antes, se necessário).
2. Abrir http://localhost:3000 em três janelas lado a lado, cada uma em uma janela anônima diferente, para manter três logins ao mesmo tempo.
3. Entrar como `garcom1`, `cozinha` e `caixa`. Senha de todos: `restmanager123`.

## Passos

| # | Quem | Ação | O que mostrar |
| :--- | :--- | :--- | :--- |
| 1 | Caixa | Abrir o turno | O caixa chega ao mapa de mesas com status e valor consumido de cada mesa (E6) |
| 2 | Garçom | No mapa, clicar em "Lançar pedido" na mesa 5, adicionar dois itens, um com observação, e enviar | O pedido fica registrado e a mesa 5 aparece ocupada no mapa do caixa sem recarregar (E3 e E6) |
| 3 | Cozinha | Observar o painel | O pedido da mesa 5 aparece sozinho, depois dos pedidos mais antigos (E4) |
| 4 | Cozinha | Clicar em "Iniciar preparo" e depois em "Marcar como pronto" | Na tela "Acompanhar pedidos" do garçom, o status muda e aparece o aviso de item pronto (E5) |
| 5 | Caixa | Transferir o pedido da mesa 5 para a mesa 6 | O pedido muda de mesa com os mesmos itens (E9) |
| 6 | Caixa | Abrir a mesa 6 e iniciar o pagamento | O garçom vê a mesa 6 como "Mesa em pagamento" e não consegue lançar nela (E14) |
| 7 | Caixa | Dividir o pagamento em PIX e cartão com valor menor que o total | O sistema recusa porque a soma não cobre o total (E7) |
| 8 | Caixa | Ajustar os valores para cobrir o total e confirmar | A conta é fechada e a mesa 6 volta a ficar disponível (E7) |
| 9 | Caixa | Baixar o comprovante | O PDF mostra os itens consumidos e o valor pago (E8) |
| 10 | Admin | Entrar como `admin`, cadastrar um funcionário de perfil cozinha e entrar com ele | O novo funcionário só vê a área da cozinha (E1) |
| 11 | Admin | Cadastrar um produto e uma mesa | Os dois aparecem para o garçom (E2) |
