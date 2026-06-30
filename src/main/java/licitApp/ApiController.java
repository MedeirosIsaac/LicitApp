package licitApp;

import licitApp.model.Comentario;
import licitApp.model.Contratacao;
import licitApp.model.Documento;
import licitApp.service.ContratacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final ContratacaoService service;

    public ApiController(ContratacaoService service) {
        this.service = service;
    }

    // ── Contratações ─────────────────────────────────────────────────────────

    @GetMapping("/contratacoes")
    public ResponseEntity<List<Contratacao>> listarContratacoes() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/contratacoes/{id}")
    public ResponseEntity<Contratacao> buscarContratacao(@PathVariable String id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/contratacoes")
    public ResponseEntity<?> criarContratacao(@RequestBody Contratacao contratacao) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(contratacao));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErroResposta(e.getMessage()));
        }
    }

    @DeleteMapping("/contratacoes/{id}")
    public ResponseEntity<Void> deletarContratacao(@PathVariable String id) {
        return service.deletar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/contratacoes/{id}/aprovar")
    public ResponseEntity<?> aprovarContratacao(@PathVariable String id) {
        return service.buscarPorId(id).map(c -> {
            c.setStatus(licitApp.enums.StatusContratacao.APROVADO);
            service.salvar(c);
            return ResponseEntity.ok(c);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Documentos ───────────────────────────────────────────────────────────

    @GetMapping("/contratacoes/{id}/documentos")
    public ResponseEntity<?> listarDocumentos(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.listarDocumentos(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResposta(e.getMessage()));
        }
    }

    @PostMapping("/contratacoes/{id}/documentos")
    public ResponseEntity<?> adicionarDocumento(@PathVariable String id,
                                                @RequestBody Documento documento) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.adicionarDocumento(id, documento));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResposta(e.getMessage()));
        }
    }

    // ── Comentários ──────────────────────────────────────────────────────────

    @GetMapping("/documentos/{id}/comentarios")
    public ResponseEntity<List<Comentario>> listarComentarios(@PathVariable String id) {
        return ResponseEntity.ok(service.listarComentarios(id));
    }

    @PostMapping("/documentos/{id}/comentarios")
    public ResponseEntity<?> adicionarComentario(@PathVariable String id,
                                                 @RequestBody Comentario comentario) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.salvarComentario(id, comentario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErroResposta(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResposta(e.getMessage()));
        }
    }

    record ErroResposta(String mensagem) {}
}