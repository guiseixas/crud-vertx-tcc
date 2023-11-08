package com.io.vertx.tcc_starter.entity;

import javax.persistence.*;

@Entity
public class Usuario {

  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String nome;

  @Column(nullable = false)
  private String cpf;

  @Column(nullable = false)
  private String telefone;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String senha;

  @OneToOne
  private Idioma idioma;

  public Usuario() {
  }

  public Usuario(Long id, String nome, String cpf, String telefone, String email, String senha, Idioma idioma) {
    this.id = id;
    this.nome = nome;
    this.cpf = cpf;
    this.telefone = telefone;
    this.email = email;
    this.senha = senha;
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

  public String getCpf() {
    return cpf;
  }

  public void setCpf(String cpf) {
    this.cpf = cpf;
  }

  public String getTelefone() {
    return telefone;
  }

  public void setTelefone(String telefone) {
    this.telefone = telefone;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getSenha() {
    return senha;
  }

  public void setSenha(String senha) {
    this.senha = senha;
  }

  public Idioma getIdioma() {
    return idioma;
  }

  public void setIdioma(Idioma idioma) {
    this.idioma = idioma;
  }
}
