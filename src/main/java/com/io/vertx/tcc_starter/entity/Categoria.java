package com.io.vertx.tcc_starter.entity;

import javax.persistence.*;

@Entity
public class Categoria {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nome;

  private String tag;

  @ManyToOne
  private Idioma idioma;

  public Categoria() {
  }

  public Categoria(Long id, String nome, String tag, Idioma idioma) {
    this.id = id;
    this.nome = nome;
    this.tag = tag;
    this.idioma = idioma;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public Idioma getIdioma() {
    return idioma;
  }

  public void setIdioma(Idioma idioma) {
    this.idioma = idioma;
  }
}
