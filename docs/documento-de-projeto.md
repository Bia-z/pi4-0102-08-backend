# 1. Visão geral
**Responsável:** Beatriz Caroline Moreno Tavares

**Problema.** Restaurantes de pequeno e médio porte controlam pedidos, mesas e caixa
com comandas de papel, planilhas e comunicação verbal entre garçom e cozinha. Isso
causa pedidos trocados ou esquecidos, atraso no atendimento e divergência na
conferência do caixa. O gestor não enxerga o salão em tempo real, e os sistemas de PDV
do mercado são caros e trazem módulos (fiscal, estoque, reservas) que um restaurante
pequeno não usa.

**Público**
- Garçom: lança pedidos nas mesas e acompanha o andamento na cozinha.
- Cozinha: vê os pedidos por ordem de chegada, muda o status e imprime a comanda.
- Caixa: abre e fecha o turno (com senha), controla as mesas, recebe o pagamento
  (inclusive dividido), faz sangria/reforço e consulta ou cancela comandas.
- Administrador: cadastra funcionários, mesas, categorias e cardápio.
- O cliente do restaurante não usa o sistema.

**Escopo**
- E1 Cadastro e autenticação de usuários, com acesso só à área do próprio perfil.
- E2 Cadastro de mesas e do cardápio (produtos e categorias).
- E3 Lançamento de pedido pelo garçom, com observação por item.
- E4 Painel da cozinha atualizado em tempo real, sem recarregar a página.
- E5 Mudança de status do pedido pela cozinha, refletida no garçom.
- E6 Controle de mesas, abertura e fechamento de turno do caixa.
- E7 Pagamento dividido em mais de uma forma, com cálculo de troco.
- E8 Comprovante de pagamento em PDF.
- E9 Transferência de pedido entre mesas.
- E10 Sangria e reforço de caixa, com comanda impressa de valor, horário, tipo e motivo.
- E11 Relatórios do dia e histórico de fechamentos de turno, com reimpressão.
- E12 Cancelamento de pedido já pago, com login e senha de administrador.

**Fora do escopo.** Aplicativo móvel nativo (o sistema é um site responsivo), pedido
feito pelo próprio cliente, reservas e fila de espera, emissão fiscal (NFC-e/SAT),
integração com maquininhas, controle de estoque e o cadastro de ingredientes por prato,
que estava nos wireframes e foi cortado.

# 2. Objetivos
**Responsável:** Beatriz Caroline Moreno Tavares

**Objetivo geral.** Construir uma plataforma web de gestão de restaurante que substitua
as comandas de papel e integre pedidos, cozinha, mesas e caixa em um fluxo único e em
tempo real.

**Objetivos específicos**
- OE1: Digitalizar o lançamento de pedidos pelo garçom, eliminando a comanda de papel.
- OE2: Mostrar o pedido à cozinha em tempo real, por ordem de chegada, e refletir a
  mudança de status no garçom.
- OE3: Integrar o fechamento da conta ao caixa, com controle de mesas, pagamento
  dividido e turno protegido por senha.
- OE4: Restringir cada funcionalidade ao perfil do usuário.
- OE5: Subir o sistema inteiro com um comando, em qualquer máquina com Docker.

**Critério de sucesso.** O garçom lança um pedido, a cozinha o recebe e muda o status, e
o garçom vê a mudança em menos de 2 segundos sem recarregar; o caixa fecha a conta
(dividida em mais de uma forma) e o turno, com senha, sem uso de papel; e o sistema sobe
com `docker compose up -d --build`.