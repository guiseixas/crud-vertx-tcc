package com.io.vertx.tcc_starter.entity;

import javax.persistence.*;

@Entity
public class Idioma {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String nome;

  @Column(nullable = false)
  private String tag;

  public Idioma() {
  }

  public Idioma(Long id, String nome, String tag) {
    this.id = id;
    this.nome = nome;
    this.tag = tag;
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
}
