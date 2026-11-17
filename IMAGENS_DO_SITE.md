# Imagens de veículos do AutoSmart

Nesta versão, a vitrine pública (`/`) e a tela de estoque (`/veiculos`)
mostram automaticamente imagens locais para os **24 modelos de demonstração**.
As imagens ficam em `src/main/resources/static/img/veiculos/` e carregam
sem conexão à internet.

**Atenção:** são **imagens ilustrativas geradas digitalmente** de veículos;
não são fotografias dos carros específicos oferecidos pela concessionária.
Alguns modelos têm apenas uma representação aproximada da categoria ou marca.
Não use essas imagens para comprovar cor, versão, estado de conservação nem
características de um carro real.

## Como funciona

- A classe `ImagensVeiculos.java` procura uma imagem local por marca e modelo.
- Se o veículo tem uma URL HTTPS própria no campo `fotoUrl`, a vitrine usa
  essa foto personalizada primeiro.
- Se a URL não carregar, a imagem local correspondente entra como reserva.
- Se o modelo não estiver entre os 24 cadastrados, usa `img/car.svg`.
- Os dados existentes no PostgreSQL são preservados: nenhuma alteração
  no banco é necessária para as imagens aparecerem.

## Executar no Windows

Entre na pasta `autosmart`, configure sua senha do PostgreSQL se necessária,
e execute `mvn spring-boot:run`.
Abra `http://localhost:8080/` (ou a porta configurada no seu ambiente).
Se o servidor já estava rodando, encerre com `Ctrl+C` e inicie novamente.

Novos veículos podem ser cadastrados normalmente; imagens personalizadas
continuam sendo informadas por URL HTTPS, como no projeto original.
