# Roadmap de features

Cada linha vira uma pasta `specs/NNN-nome/`. A ordem respeita dependências:
nada de rotina existe sem idoso e membros, por isso a fundação vem primeiro.

| # | Feature | Depende de | Status |
|---|---|---|---|
| 000 | Fundação: cadastro com aprovação, login, idoso e papéis | — | spec ✔ plan ✔ contrato ✔ tasks ✔ · implementação a fazer |
| 001 | Medicamentos: prescrições, horários, administração, atrasos | 000 | — |
| 002 | Refeições e hidratação | 000 | — |
| 003 | Sinais vitais | 000 | — |
| 004 | Diário, ocorrências e passagem de turno | 000 | — |
| 005 | Avisos entre cuidadores/família | 000 | — |
| 006 | Pedidos de compra (remédios, perfumaria, itens gerais) | 000 (001 opcional) | — |
| 007 | Visitas de médicos e profissionais de saúde | 000 | — |
| 008 | Tela "Meu turno" (painel que agrega pendências de 001–007) | 001–007 | — |
