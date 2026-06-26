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

@Controller
public class MainController {

    private final ContratacaoService service;

    public MainController(ContratacaoService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public String abrirDashboard(Model model) {
        model.addAttribute("usuario", new UsuarioLogado("Isaac", "Costa", "Analista CI"));
        model.addAttribute("stats", service.calcularStats());
        model.addAttribute("contratacoes", service.listarTodas());
        return "dashboard";
    }

    @GetMapping("/empresas")
    public String abrirEmpresas(Model model) {
        model.addAttribute("usuario", new UsuarioLogado("Isaac", "Costa", "Analista CI"));
        return "empresas";
    }

    @GetMapping("/licitacoes")
    public String abrirLicitacoes(Model model) {
        model.addAttribute("usuario", new UsuarioLogado("Isaac", "Costa", "Analista CI"));
        model.addAttribute("stats", service.calcularStats());
        model.addAttribute("contratacoes", service.listarTodas());
        return "dashboard";
    }

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

    @PostMapping("/contratacoes/{id}/aprovar")
    public String aprovarContratacao(@PathVariable String id) {
        service.buscarPorId(id).ifPresent(c -> {
            c.setStatus(StatusContratacao.APROVADO);
            service.salvar(c);
        });
        return "redirect:/analise/" + id;
    }

    @GetMapping("/contratacoes/novo")
    public String novaContratacao(Model model) {
        model.addAttribute("usuario", new UsuarioLogado("Isaac", "Costa", "Analista CI"));
        return "nova-contratacao";
    }

    /**
     * Exportar — retorna arquivo JSON para download, com indentação legível.
     */
    @GetMapping("/contratacoes/exportar")
    public ResponseEntity<byte[]> exportar() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        List<Contratacao> lista = service.listarTodas();
        byte[] json = mapper.writeValueAsBytes(lista);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contratacoes.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleError(RuntimeException ex, Model model) {
        model.addAttribute("mensagem", ex.getMessage());
        return "erro";
    }

    record UsuarioLogado(String nome, String sobrenome, String perfil) {}
}