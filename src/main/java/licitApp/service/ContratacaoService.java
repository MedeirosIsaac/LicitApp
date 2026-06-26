package licitApp.service;

import licitApp.model.Comentario;
import licitApp.model.Contratacao;
import licitApp.model.Documento;
import licitApp.repository.ContratacaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContratacaoService {

    private final ContratacaoRepository repository;

    public ContratacaoService(ContratacaoRepository repository) {
        this.repository = repository;
    }

    // ── Contratações ─────────────────────────────────────────────────────────

    public List<Contratacao> listarTodas() {
        return repository.findAll();
    }

    public Optional<Contratacao> buscarPorId(String id) {
        return repository.findById(id);
    }

    public Contratacao salvar(Contratacao contratacao) {
        if (contratacao.getNumeroProcesso() == null || contratacao.getNumeroProcesso().isBlank())
            throw new IllegalArgumentException("O número do processo é obrigatório.");
        if (contratacao.getEmpresaLicitante() == null || contratacao.getEmpresaLicitante().isBlank())
            throw new IllegalArgumentException("O nome da empresa licitante é obrigatório.");
        return repository.save(contratacao);
    }

    public boolean deletar(String id) {
        return repository.deleteById(id);
    }

    // ── Documentos ───────────────────────────────────────────────────────────

    public List<Documento> listarDocumentos(String contratacaoId) {
        return repository.findById(contratacaoId)
                .map(Contratacao::getDocumentos)
                .orElseThrow(() -> new RuntimeException("Contratação não encontrada: " + contratacaoId));
    }

    public Contratacao adicionarDocumento(String contratacaoId, Documento documento) {
        Contratacao contratacao = repository.findById(contratacaoId)
                .orElseThrow(() -> new RuntimeException("Contratação não encontrada: " + contratacaoId));
        contratacao.adicionarDocumento(documento);
        return repository.save(contratacao);
    }

    // ── Comentários ──────────────────────────────────────────────────────────

    public List<Comentario> listarComentarios(String documentoId) {
        return repository.findComentariosByDocumentoId(documentoId);
    }

    public Comentario salvarComentario(String documentoId, Comentario comentario) {
        if (comentario.getTexto() == null || comentario.getTexto().isBlank())
            throw new IllegalArgumentException("O texto do comentário não pode estar vazio.");
        return repository.adicionarComentario(documentoId, comentario)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado: " + documentoId));
    }

    // ── Stats para o dashboard ───────────────────────────────────────────────

    public DashboardStats calcularStats() {
        List<Contratacao> todas = repository.findAll();
        int total     = todas.size();
        int emAnalise = (int) todas.stream().filter(c -> c.getStatus().name().equals("EM_ANALISE")).count();
        int aprovados = (int) todas.stream().filter(c -> c.getStatus().name().equals("APROVADO")).count();
        int pendentes = (int) todas.stream().filter(c -> c.getStatus().name().equals("PENDENTE")).count();
        return new DashboardStats(total, emAnalise, aprovados, pendentes);
    }

    public record DashboardStats(int total, int emAnalise, int aprovados, int pendentes) {}
}