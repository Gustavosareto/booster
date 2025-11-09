BoosterMaster - Projeto fonte
=============================
Estrutura pronta para compilar (Maven).

Como compilar:
1. Tenha Maven instalado e um JDK 8.
2. Rode:
   mvn clean package
3. Copie o jar gerado em target/ para a pasta plugins/ do seu servidor.

Observações:
- O plugin usa Vault (se disponível) para compatibilidade com NextEconomy.
- Boosters são itens físicos (frasco de xp) que o jogador ativa ao clicar.
- As configurações (multiplicadores e durações) estão em config.yml.
