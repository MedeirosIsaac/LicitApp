package enums;

public enum Modalidade {
    PREGAO_ELETRONICO("Pregão Eletrônico"),
    DISPENSA_LICITACAO("Dispensa de Licitação"),
    CONCORRENCIA_PUBLICA("Concorrência Pública");

    private final String descricao;

    //Construtor
    Modalidade(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
