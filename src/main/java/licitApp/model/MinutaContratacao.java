package licitApp.model;

public class MinutaContratacao extends Documento {
    

    public MinutaContratacao() {
        super();
    }


    public MinutaContratacao(String nomeArquivo, long tamanhoBytes) {
        super(nomeArquivo, tamanhoBytes);
    }

    @Override
    public String getTipoDocumento() {
        return "MINUTA";
    }
}