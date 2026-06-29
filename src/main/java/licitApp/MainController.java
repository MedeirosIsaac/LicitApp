package licitApp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import licitApp.enums.StatusContratacao;
import licitApp.model.Contratacao;
import licitApp.model.MinutaContratacao;
import licitApp.service.ContratacaoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
public class MainController {

    private final ContratacaoService service;

    public MainController(ContratacaoService service) {
        this.service = service;
    }

    // ── Redireciona raiz e /dashboard para /licitacoes ────────────────────────
    @GetMapping("/")
    public String raiz() {
        return "redirect:/licitacoes";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/licitacoes";
    }

    // ── Tela principal de Contratações ──────────────────────
    @GetMapping("/licitacoes")
    public String abrirLicitacoes(Model model) {
        model.addAttribute("usuario", new UsuarioLogado("Isaac", "Costa", "Analista CI"));
        model.addAttribute("stats", service.calcularStats());
        model.addAttribute("contratacoes", service.listarTodas());
        return "dashboard";
    }

    // ── Empresas (AGORA DINÂMICA) ─────────────────────────────────────────────
    @GetMapping("/empresas")
    public String abrirEmpresas(Model model) {
        model.addAttribute("usuario", new UsuarioLogado("Isaac", "Costa", "Analista CI"));

        // 1. Puxa todos os contratos
        List<Contratacao> todas = service.listarTodas();

        // 2. Agrupa os contratos pelo nome da empresa (ignora contratos sem empresa)
        Map<String, List<Contratacao>> contratosPorEmpresa = todas.stream()
                .filter(c -> c.getEmpresaLicitante() != null && !c.getEmpresaLicitante().isBlank())
                .collect(Collectors.groupingBy(Contratacao::getEmpresaLicitante));

        // 3. Transforma o grupo em um Resumo com a soma dos valores
        List<EmpresaResumo> empresas = contratosPorEmpresa.entrySet().stream()
                .map(entry -> {
                    String nome = entry.getKey();
                    List<Contratacao> lista = entry.getValue();
                    // Soma o valor de todos os contratos dessa empresa
                    double total = lista.stream().mapToDouble(Contratacao::getValorEstimado).sum();
                    return new EmpresaResumo(nome, lista.size(), total, lista);
                })
                .toList();

        // Envia a lista agrupada para o HTML
        model.addAttribute("empresas", empresas);
        return "empresas"; 
    }

    // ── Relatórios ────────────────────────────────────────────────────────────
    @GetMapping("/relatorios")
    public String abrirRelatorios(Model model) {
        model.addAttribute("usuario", new UsuarioLogado("Isaac", "Costa", "Analista CI"));
        model.addAttribute("contratacoes", service.listarTodas());
        return "relatorios";
    }

    @GetMapping("/analise/{id}")
    public String abrirAnalise(@PathVariable String id, Model model) {
        model.addAttribute("usuario", new UsuarioLogado("Isaac", "Costa", "Analista CI"));

        Contratacao contratacao = service.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Contratação não encontrada: " + id));
        
        if (contratacao.getStatus().name().equals("PENDENTE")) {
            contratacao.setStatus(StatusContratacao.EM_ANALISE);
            service.salvar(contratacao);    
        }

        model.addAttribute("contratacao", contratacao);

        // Fallback seguro: cria documento vazio com ID "0" se não houver documentos
        MinutaContratacao minutaVazia = new MinutaContratacao("sem_documento.pdf", 0L);
        minutaVazia.setId("0");

        licitApp.model.Documento minuta = contratacao.getDocumentos().stream()
                .filter(d -> "MINUTA".equals(d.getTipoDocumento()))
                .findFirst()
                .orElse(minutaVazia);

        licitApp.model.Documento regularizacao = contratacao.getDocumentos().stream()
                .filter(d -> !"MINUTA".equals(d.getTipoDocumento()))
                .findFirst()
                .orElse(minutaVazia);

        model.addAttribute("minuta", minuta);
        model.addAttribute("regularizacao", regularizacao);

        return "analise";
    }

    // ── Aprovar contratação ───────────────────────────────────────────────────
    @PostMapping("/contratacoes/{id}/aprovar")
    public String aprovarContratacao(@PathVariable String id) {
        service.buscarPorId(id).ifPresent(c -> {
            c.setStatus(StatusContratacao.APROVADO);
            service.salvar(c);
        });
        return "redirect:/analise/" + id;
    }

    // ── Novo Processo ─────────────────────────────────────────────────────────
    @GetMapping("/contratacoes/novo")
    public String novaContratacao(Model model) {
        model.addAttribute("usuario", new UsuarioLogado("Isaac", "Costa", "Analista CI"));
        return "nova-contratacao";
    }

    // ── Exportar JSON ─────────────────────────────────────────────────────────
    @GetMapping("/contratacoes/exportar")
    public ResponseEntity<byte[]> exportar() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        byte[] json = mapper.writeValueAsBytes(service.listarTodas());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contratacoes.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    // ── Tratamento de erros ───────────────────────────────────────────────────
    @ExceptionHandler(RuntimeException.class)
    public String handleError(RuntimeException ex, Model model) {
        model.addAttribute("mensagem", ex.getMessage());
        return "erro";
    }

    record UsuarioLogado(String nome, String sobrenome, String perfil) {}
    
    // Novo record para enviar os dados agrupados da empresa para a tela
    public record EmpresaResumo(String nome, int qtdContratos, double valorTotal, List<Contratacao> contratos) {}
}