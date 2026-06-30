package licitApp.model; 

import licitApp.enums.Modalidade;
import licitApp.enums.StatusContratacao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Contratacao {
    private String id;
    private String numeroProcesso;
    private String empresaLicitante;
    private String objeto;
    private String dataProcesso;
    private Modalidade modalidade;
    private StatusContratacao status;
    private double valorEstimado;

    private List<Documento> documentos;

    public Contratacao() {
        this.id = UUID.randomUUID().toString();
        this.documentos = new ArrayList<>();
    }

    
    public Contratacao(String numeroProcesso, String empresaLicitante, String objeto,
                       Modalidade modalidade, StatusContratacao status, double valorEstimado) {
        this.id = UUID.randomUUID().toString();
        this.numeroProcesso = numeroProcesso;
        this.empresaLicitante = empresaLicitante;
        this.objeto = objeto;
        this.modalidade = modalidade;
        this.status = status;
        this.valorEstimado = valorEstimado;
        this.documentos = new ArrayList<>();
    }

    public void adicionarDocumento(Documento documento) {
        if (documento != null) {
            this.documentos.add(documento);
        }
    }

    public boolean removerDocumento(String documentoId) {
        return this.documentos.removeIf(doc -> doc.getId().equals(documentoId));
    }

    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }

    public String getEmpresaLicitante() {
        return empresaLicitante;
    }

    public void setEmpresaLicitante(String empresaLicitante) {
        this.empresaLicitante = empresaLicitante;
    }

    public String getObjeto() {
        return objeto;
    }

    public void setObjeto(String objeto) {
        this.objeto = objeto;
    }

    public Modalidade getModalidade() {
        return modalidade;
    }

    public void setModalidade(Modalidade modalidade) {
        this.modalidade = modalidade;
    }

    public StatusContratacao getStatus() {
        return status;
    }

    public void setStatus(StatusContratacao status) {
        this.status = status;
    }

    public double getValorEstimado() {
        return valorEstimado;
    }

    public void setValorEstimado(double valorEstimado) {
        this.valorEstimado = valorEstimado;
    }

    public List<Documento> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<Documento> documentos) {
        this.documentos = documentos;
    }

    public String getDataProcesso() {
        return dataProcesso;
    }

    public void setDataProcesso(String dataProcesso) {
        this.dataProcesso = dataProcesso;
    }

    @Override
    public String toString() {
        return "Contratacao{" +
                "id='" + id + '\'' +
                ", numeroProcesso='" + numeroProcesso + '\'' +
                ", empresaLicitante='" + empresaLicitante + '\'' +
                ", status=" + status +
                ", totalDocumentos=" + documentos.size() +
                '}';
    }
}
