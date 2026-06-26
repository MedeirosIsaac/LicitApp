package licitApp;

import licitApp.enums.Modalidade;
import licitApp.enums.StatusContratacao;
import licitApp.model.Aditivo;
import licitApp.model.Apostilamento;
import licitApp.model.Comentario;
import licitApp.model.Contratacao;
import licitApp.model.MinutaContratacao;
import licitApp.repository.ContratacaoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Popula o arquivo contratacoes.json com dados de exemplo
 * apenas na primeira vez (quando o arquivo está vazio).
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final ContratacaoRepository repository;

    public DataLoader(ContratacaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        List<Contratacao> existentes = repository.findAll();
        if (!existentes.isEmpty()) {
            System.out.println("[LicitApp] Dados já carregados (" + existentes.size() + " contratações).");
            return;
        }

        System.out.println("[LicitApp] Carregando dados de exemplo...");

        // ── Contratação 1 ─────────────────────────────────────────────────
        Contratacao c1 = new Contratacao(
                "PE 044/2026",
                "TechNorte Soluções Ltda.",
                "Aquisição de licenças de software para gestão hospitalar e suporte técnico especializado.",
                Modalidade.PREGAO_ELETRONICO,
                StatusContratacao.EM_ANALISE,
                1_250_400.00
        );
        MinutaContratacao minuta1 = new MinutaContratacao("minuta_PE044_2026.pdf", 204_800L);
        minuta1.adicionarComentario(new Comentario(
                "Cláusula 4.2 apresenta prazo de entrega ambíguo. Solicitar esclarecimento.",
                "Isaac Costa"
        ));
        Apostilamento apost1 = new Apostilamento(
                "regularizacao_fiscal_technorte.pdf", 512_000L);
        c1.adicionarDocumento(minuta1);
        c1.adicionarDocumento(apost1);
        repository.save(c1);

        // ── Contratação 2 ─────────────────────────────────────────────────
        Contratacao c2 = new Contratacao(
                "DL 012/2024",
                "Construtora Horizonte Verde",
                "Manutenção preventiva em infraestrutura de drenagem urbana — Setor Sul.",
                Modalidade.DISPENSA_LICITACAO,
                StatusContratacao.APROVADO,
                45_200.00
        );
        MinutaContratacao minuta2 = new MinutaContratacao("minuta_DL012_2024.pdf", 98_304L);
        Aditivo aditivo2 = new Aditivo(
                "aditivo_1_DL012.pdf", 65_536L
        );
        c2.adicionarDocumento(minuta2);
        c2.adicionarDocumento(aditivo2);
        repository.save(c2);

        // ── Contratação 3 ─────────────────────────────────────────────────
        Contratacao c3 = new Contratacao(
                "CP 005/2024",
                "Alimentos & Cia Logística",
                "Fornecimento de merenda escolar para a rede municipal de ensino fundamental.",
                Modalidade.CONCORRENCIA_PUBLICA,
                StatusContratacao.PENDENTE,
                3_840_000.00
        );
        MinutaContratacao minuta3 = new MinutaContratacao("minuta_CP005_2024.pdf", 307_200L);
        c3.adicionarDocumento(minuta3);
        repository.save(c3);

        System.out.println("[LicitApp] ✅ 3 contratações de exemplo carregadas.");
    }
}