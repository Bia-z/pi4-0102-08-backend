# RestManager

O RestManager é uma plataforma web de gestão de restaurante que substitui as comandas de papel, integrando em tempo real o lançamento de pedidos pelo garçom, o acompanhamento da cozinha e o fechamento do caixa — com perfis de acesso distintos para garçom, cozinha, caixa e administrador.

## Integrantes

- Beatriz Caroline Moreno Tavares — @Bia-z
- João Victor Teles Carneiro — @jvictortcarneiro-max
- Heitor Barros Silvério — @Heitorbs97
- Lucas Ormenese Altieri — @la-eds’s
- Nathalia Lopes Da Silva Lima — @NathyLopes
- Vitória Aparecida Brigo Mattos — @Vitoria-Mattos

## Link para o respositorio do frontend

https://github.com/Bia-z/pi4-0102-08-frontend/tree/main/docs/fluxos

## Segurança da API

O login `POST /api/auth/login` é público. As demais operações exigem um JWT
válido no cabeçalho `Authorization: Bearer <token>`. As permissões por método,
rota e perfil ficam declaradas em um único lugar, `PerfilAccessRules`; rotas sem
regra são recusadas.

Sem token, a API responde `401` com `{"erro":"TOKEN_AUSENTE"}`; token inválido
responde `401` com `{"erro":"TOKEN_INVALIDO"}`; perfil sem permissão responde
`403` com `{"erro":"ACESSO_NEGADO"}`.
