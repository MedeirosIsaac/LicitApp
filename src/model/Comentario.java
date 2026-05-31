package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Comentario {
    private String id;
    private String texto;
    private String autor;
    private LocalDateTime dataCriacao;

    //Construtores
    public Comentario(){
        this.id = UUID.randomUUID().toString();
        this.dataCriacao = LocalDateTime.now();
    }

    public Comentario(String texto, String autor) {
        this.id = UUID.randomUUID().toString();
        this.texto = texto;
        this.autor = autor;
        this.dataCriacao = LocalDateTime.now();
    }

    //Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    //Metodos
    @Override
    public String toString() {
        return "Comentario{" +
                "id='" + id + '\'' +
                ", autor='" + autor + '\'' +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}
