# ADR 0003 — Utilizar WebSocket para notificar atualizações de pedidos e de status das mesas

**Status:** Proposto (atualizado em 19/10/2026)

**Contexto:** O RestManager precisa permitir que os pedidos registrados pelo garçom sejam visualizados pela cozinha sem depender de atualizações manuais da página. Também é necessário comunicar alterações de status dos pedidos, como novo pedido, em preparo e pronto. A comunicação deve ser simples e compatível com o backend Spring Boot e o frontend web (HTML, JavaScript e Bootstrap).

Além dos pedidos, o status das mesas também muda enquanto o salão está em operação: a mesa fica ocupada quando o garçom lança o primeiro pedido, é paga no caixa e volta a ficar disponível. Enquanto o caixa está recebendo o pagamento de uma mesa, o garçom não pode lançar novos itens nela, pois o valor da conta já está sendo fechado. Sem uma notificação em tempo real, o garçom só descobriria o bloqueio ao tentar enviar o pedido.

**Decisão:** Utilizar WebSocket para enviar notificações em tempo real do backend Spring Boot às telas conectadas do sistema. As operações de criação e alteração de pedidos continuarão sendo realizadas por requisições HTTP, enquanto o WebSocket será utilizado para notificar as telas sobre as mudanças.

O mesmo mecanismo será usado para o status das mesas, com as regras abaixo:

1. **Novo status de mesa:** `em_pagamento`, somando-se a `disponivel`, `ocupada` e `reservada`.
2. **Ciclo da mesa no pagamento:**
   - Ao abrir a tela de pagamento, o caixa chama `iniciarPagamento` (CT19). A mesa passa de `ocupada` para `em_pagamento` e o backend publica `statusMesaAtualizado` (CT21).
   - Ao confirmar o pagamento (`processarPagamento`, CT14), a mesa passa para `disponivel` e o backend publica CT21.
   - Se o caixa desistir, chama `cancelarPagamento` (CT20). A mesa volta para `ocupada` e o backend publica CT21.
3. **O backend é a fonte da verdade.** O lançamento de pedido (`criarPedido`, CT7) é recusado com o erro `MESA_EM_PAGAMENTO` sempre que a mesa estiver nesse status, mesmo que a tela do garçom esteja desatualizada ou desconectada. O WebSocket apenas avisa as telas para que o bloqueio apareça antes da tentativa (botão de lançar desabilitado e mesa destacada no mapa).
4. **Concorrência:** a leitura e a troca de status da mesa em CT7 e CT19 ocorrem dentro de uma transação com controle de concorrência sobre a linha da mesa (lock ou versionamento no JPA). Assim, se o garçom enviar o pedido e o caixa iniciar o pagamento no mesmo instante, apenas uma das operações prevalece e a outra recebe erro.
5. **Mesa travada:** o pagamento iniciado fica vinculado ao operador de caixa que o abriu. Se a sessão WebSocket desse operador cair e não voltar em até 60 segundos (valor inicial, ajustável), o backend devolve a mesa para `ocupada` e publica CT21. O Administrador também pode liberar a mesa chamando CT20.
6. **Reconexão:** como nos demais canais, uma tela que perde a conexão não recebe as mensagens enviadas nesse intervalo. Ao reconectar, o frontend busca o estado atual das mesas via HTTP (`obterMapaMesas`, CT12).

Canais WebSocket (STOMP) do sistema:

| Canal | Contrato | Quem recebe |
| :--- | :--- | :--- |
| `/topic/cozinha` | CT9 `pedidoCriado` | Painel da Cozinha |
| `/topic/pedidos` | CT11 `statusPedidoAtualizado` | Painéis do Garçom e da Cozinha |
| `/topic/mesas` | CT21 `statusMesaAtualizado` | Painéis do Garçom e do Caixa |

**Alternativas consideradas:**
- Atualização manual da página: descartada porque depende da ação do usuário e pode atrasar a visualização dos pedidos pela cozinha.
- Requisições HTTP periódicas (polling): não foi escolhida porque a cozinha precisaria consultar o backend repetidamente para verificar a chegada de novos pedidos, mesmo quando não houvesse alterações.
- Bloquear a mesa apenas no backend, sem notificação: descartada porque o garçom só saberia do bloqueio ao receber o erro, depois de montar o pedido inteiro.
- Bloquear a mesa apenas na tela, sem validação no backend: descartada porque uma tela desatualizada ou desconectada ainda permitiria lançar pedido em uma mesa em pagamento.

**Consequências:**
- Positivas: a cozinha pode receber notificações sem precisar atualizar a página manualmente; as alterações de status podem ser comunicadas às telas conectadas; o garçom vê o bloqueio da mesa em tempo real e a conta paga não recebe itens novos durante o fechamento; a validação no backend garante a regra mesmo com falha de conexão; a solução utiliza recursos nativos do Spring e do navegador, sem exigir uma biblioteca adicional de WebSocket.
- Negativas: A implementação exige configurar o canal de comunicação e controlar as conexões abertas, adicionando responsabilidades ao backend. O novo status exige tratar mesas travadas (queda do caixa) e controle de concorrência. Durante o pagamento a mesa inteira fica bloqueada, inclusive no pagamento de apenas parte dos itens, e o garçom só volta a lançar na mesa se o pagamento for cancelado.
