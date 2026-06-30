package licitApp.model;

public class Aditivo extends Documento {
    
    
    public Aditivo() {
        super();
    }

    
    public Aditivo(String nomeArquivo, long tamanhoBytes) {
        super(nomeArquivo, tamanhoBytes);
    }

    @Override
    public String getTipoDocumento() {
        return "ADITIVO";
    }
}