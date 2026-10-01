# Frontend

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 22.2.0.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## Cliente da API (gerado, não editar)

O cliente TypeScript da API (`src/app/core/api/`) é gerado a partir de
`../specs/000-fundacao/contracts/openapi.yaml` com o OpenAPI Generator
(gerador `typescript-angular`, D-19). Ele **não** é versionado no git — é
regenerado localmente e no CI antes de buildar/testar:

```bash
npm run api:generate
```

Rode esse comando sempre que o contrato (`openapi.yaml`) mudar, ou após um
`git clone`/`npm install` limpo, antes de `ng serve`, `ng build` ou `ng test`.
Nunca edite os arquivos dessa pasta manualmente; qualquer ajuste necessário
deve ser feito no `openapi.yaml` e regerado.

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
