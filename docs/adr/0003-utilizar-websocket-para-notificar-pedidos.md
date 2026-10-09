# ADR 0003 — Utilizar WebSocket para notificar atualizações de pedidos 
**Status:** Proposto
**Contexto:** RestManager precisa permitir que os pedidos registrados pelo garçom sejam visualizados pela cozinha sem depender de atualizações manuais da página. Também é necessário comunicar alterações de status dos pedidos, como novo pedido, em preparo e pronto. A comunicação deve ser simples e compatível com o backend Spring Boot e o frontend React. 
**Decisão:** Utilizar WebSocket para enviar notificações em tempo real do backend Spring Boot às telas conectadas do sistema. As operações de criação e alteração de pedidos continuarão sendo realizadas por requisições HTTP, enquanto o WebSocket será utilizado para notificar as telas sobre as mudanças 
**Alternativas consideradas:**
- Atualização manual da página: descartada porque depende da ação do usuário e pode atrasar a visualização dos pedidos pela cozinha. 
- Requisições HTTP periódicas (polling): não foi escolhida porque a cozinha precisaria consultar o backend repetidamente para verificar a chegada de novos pedidos, mesmo quando não houvesse alterações.

**Consequências:**
- Positivas: a cozinha pode receber notificações sem precisar atualizar a página manualmente; as alterações de status podem ser comunicadas às telas conectadas; a solução utiliza recursos nativos do Spring e do navegador, sem exigir uma biblioteca adicional de WebSocket. 
- Negativas: A implementação exige configurar o canal de comunicação e controlar as conexões abertas, adicionando responsabilidades ao backend. 
