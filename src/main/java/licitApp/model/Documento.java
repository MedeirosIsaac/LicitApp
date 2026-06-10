package licitApp.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "tipo" 
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MinutaContratacao.class, name = "MINUTA"),
        @JsonSubTypes.Type(value = Aditivo.class, name = "ADITIVO"),
        @JsonSubTypes.Type(value = Apostilamento.class, name = "APOSTILAMENTO")
})

public abstract class Documento {
    protected String id;
    protected String nomeArquivo;
    protected LocalDateTime dataUpload;
    protected long tamanhoBytes;

    
    protected List<Comentario> comentarios;

    
    public Documento() {
        this.id = UUID.randomUUID().toString();
        this.dataUpload = LocalDateTime.now();
        this.comentarios = new ArrayList<>();
    }

    public Documento(String nomeArquivo, long tamanhoBytes) {
        this.id = UUID.randomUUID().toString();
        this.nomeArquivo = nomeArquivo;
        this.dataUpload = LocalDateTime.now();
        this.tamanhoBytes = tamanhoBytes;
        this.comentarios = new ArrayList<>();
    }

    
    public void adicionarComentario(Comentario comentario) {
        if (comentario != null) {
            this.comentarios.add(comentario);
        }
    }

    
    public abstract String getTipoDocumento();

    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public LocalDateTime getDataUpload() {
        return dataUpload;
    }

    public void setDataUpload(LocalDateTime dataUpload) {
        this.dataUpload = dataUpload;
    }

    public long getTamanhoBytes() {
        return tamanhoBytes;
    }

    public void setTamanhoBytes(long tamanhoBytes) {
        this.tamanhoBytes = tamanhoBytes;
    }

    public List<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }
}
