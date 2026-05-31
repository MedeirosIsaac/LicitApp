package enums;

public enum StatusContratacao {
    EM_ANALISE("Em Análise"),
    APROVADO("Aprovado"),
    REJEITADO("Rejeitado"),
    PENDENTE("Pendente");

    private final String descricao;

    //Construtor
    StatusContratacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
