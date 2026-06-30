package licitApp.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import licitApp.model.Comentario;
import licitApp.model.Contratacao;
import licitApp.model.Documento;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ContratacaoRepository {

    private static final String FILE_PATH = "dados/contratacoes.json";
    private final ObjectMapper objectMapper;

    public ContratacaoRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        new File("dados").mkdirs();
    }

    public List<Contratacao> findAll() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(file, new TypeReference<List<Contratacao>>() {});
        } catch (IOException e) {
            System.err.println("[LicitApp] Erro ao ler " + FILE_PATH + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Optional<Contratacao> findById(String id) {
        return findAll().stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public Contratacao save(Contratacao contratacao) {
        List<Contratacao> lista = findAll();
        boolean atualizado = false;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId().equals(contratacao.getId())) {
                lista.set(i, contratacao);
                atualizado = true;
                break;
            }
        }
        if (!atualizado) lista.add(contratacao);
        persistir(lista);
        return contratacao;
    }

    public Optional<Comentario> adicionarComentario(String documentoId, Comentario comentario) {
        List<Contratacao> lista = findAll();
        for (Contratacao c : lista) {
            for (Documento doc : c.getDocumentos()) {
                if (doc.getId().equals(documentoId)) {
                    doc.adicionarComentario(comentario);
                    persistir(lista);
                    return Optional.of(comentario);
                }
            }
        }
        return Optional.empty();
    }

    public List<Comentario> findComentariosByDocumentoId(String documentoId) {
        return findAll().stream()
                .flatMap(c -> c.getDocumentos().stream())
                .filter(doc -> doc.getId().equals(documentoId))
                .flatMap(doc -> doc.getComentarios().stream())
                .toList();
    }

    public boolean deleteById(String id) {
        List<Contratacao> lista = findAll();
        boolean removido = lista.removeIf(c -> c.getId().equals(id));
        if (removido) persistir(lista);
        return removido;
    }

    private void persistir(List<Contratacao> lista) {
        try {
            objectMapper.writeValue(new File(FILE_PATH), lista);
        } catch (IOException e) {
            System.err.println("[LicitApp] Erro ao salvar " + FILE_PATH + ": " + e.getMessage());
        }
    }
}