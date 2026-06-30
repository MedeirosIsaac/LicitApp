package licitApp.model;

public class Apostilamento extends Documento {
    

    public Apostilamento() {
        super();
    }

   
    public Apostilamento(String nomeArquivo, long tamanhoBytes) {
        super(nomeArquivo, tamanhoBytes);
    }

    @Override
    public String getTipoDocumento() {
        return "APOSTILAMENTO";
    }
}