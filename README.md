# 🏛️ LicitApp — Sistema de Controle Interno e Análise de Contratações

O **LicitApp** é uma plataforma web moderna e intuitiva de Controle Interno desenvolvida para centralizar, auditar e acompanhar processos de contratação pública e licitações. 

Projetado com foco na experiência do usuário (UX) e em fluxos automatizados de auditoria, o sistema permite que analistas gerenciem fornecedores, organizem cronogramas e avaliem peças jurídicas e fiscais de forma paralela e documentada.

---

## 🎯 Principais Funcionalidades

### 📊 1. Dashboard Executivo Inteligente
* **Indicadores em Tempo Real:** Painel consolidado com contadores automatizados para processos Totais, Em Análise, Aprovados e Pendentes.
* **Filtros Avançados:** Filtragem dinâmica combinada por status do processo, modalidade de contratação e ano fiscal.
* **Busca Instantânea:** Filtro de texto reativo por número do processo ou razão social do fornecedor sem necessidade de recarregar a página.

### 🏢 2. Mapeamento Dinâmico de Fornecedores (Aba Empresas)
* **Relacionamento 1:N Automatizado:** O back-end agrupa de forma inteligente todos os contratos pertencentes a uma mesma empresa cadastrada.
* **Consolidação Financeira:** Exibição do valor total acumulado empenhado por fornecedor e quantitativo de processos ativos.
* **Modais Interativos:** Janela flutuante integrada para listagem detalhada e navegação direta para as análises vinculadas a cada empresa.

### 🔍 3. Módulo de Análise Bifocal (Lado a Lado)
* **Visualização Paralela Nativa:** Renderização de documentos essenciais (Minuta do Contrato vs. Regularização Fiscal) na mesma tela.
* **Modo Leitura (Sidebar Retrátil):** Botão integrado ao logotipo institucional que recolhe suavemente a barra de menus para priorizar o espaço de tela para leitura dos arquivos.

### 💬 4. Sistema de Auditoria e Anotações Correlacionadas
* **Ancoragem de Texto:** O analista pode selecionar qualquer trecho de texto diretamente no PDF para criar um comentário contextual.
* **Persistência no Front-end:** Sistema reativo que monitora e salva as observações, garantindo integridade de dados durante a sessão de análise.

---

## 🛠️ Tecnologias Utilizadas

O ecossistema do projeto foi construído prezando por uma arquitetura limpa e alta performance de interface:

* **Back-end:**
  * **Java 17**
  * **Spring Boot 3.x**
  * **Spring MVC** (Roteamento e Controllers)
  * **Jackson ObjectMapper** (Motor de persistência de dados no formato JSON)

* **Front-end:**
  * **Thymeleaf** (Engine de renderização Server-Side)
  * **HTML5 & CSS3** (Design System proprietário, variáveis CSS e layout responsivo)
  * **Vanilla JavaScript (ES6+)** (Comunicação via Fetch API e manipulação de DOM)
  * **PDF.js (Mozilla)** (Renderização de PDFs)

---

## 🚀 Como Executar a Aplicação

### Pré-requisitos
* **Java JDK 17** ou superior instalado.
* **Maven** instalado (ou uso do wrapper `./mvnw`).

### Passo a Passo

1. **Clonar o Repositório:**
   ```bash
   git clone [https://github.com/SEU-USUARIO/SEU-REPOSITORIO.git](https://github.com/SEU-USUARIO/SEU-REPOSITORIO.git)
   cd licitapp
