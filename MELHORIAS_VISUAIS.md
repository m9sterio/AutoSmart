# AutoSmart — melhorias visuais e catálogo

- Identidade visual redesenhada com tema verde-esmeralda e azul profundo, cartões, responsividade e navegação interna.
- Campo `fotoUrl` adicionado a `Veiculo` para associar uma fotografia real via URL HTTPS, editável no cadastro e edição. Na vitrine, se não houver foto ou a imagem falhar, aparece o SVG ilustrativo anterior.
- **Fotos reais não estão pré-carregadas**: cada foto deve corresponder ao veículo/modelo correto e ter autorização de uso; o projeto não atribui fotos de outros modelos nem inventa correspondências.
- 10 modelos adicionais de veículos de demonstração, inseridos de forma idempotente com `AUTOSMART_DEMO_ENABLED=true`. Valores são fictícios para testes, não anúncios reais.
- O banco PostgreSQL existente é mantido; `ddl-auto=update` acrescenta o campo de imagem automaticamente em ambiente de desenvolvimento. Faça backup antes de executar em um banco importante.
- Para visualizar: iniciar PostgreSQL, definir `AUTOSMART_DEMO_ENABLED=true`, executar `mvn spring-boot:run`, abrir `http://localhost:8080`.
- Para foto: Área do funcionário > Veículos > Editar > Foto real do veículo (URL HTTPS). Use URL direta de imagem que você tenha direito de exibir.
- Não foram criadas integrações automáticas com sites de terceiros, nem fotos falsamente atribuídas a veículos específicos.
