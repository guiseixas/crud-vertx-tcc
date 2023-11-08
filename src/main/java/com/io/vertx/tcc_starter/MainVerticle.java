package com.io.vertx.tcc_starter;

import com.io.vertx.tcc_starter.entity.Categoria;
import com.io.vertx.tcc_starter.entity.Filme;
import com.io.vertx.tcc_starter.entity.Idioma;
import com.io.vertx.tcc_starter.entity.Usuario;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.http.HttpServer;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.RoutingContext;
import io.vertx.mutiny.ext.web.handler.BodyHandler;
import io.vertx.mutiny.ext.web.handler.CorsHandler;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.Persistence;
import io.vertx.core.http.HttpMethod;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  private Mutiny.SessionFactory emf;

  @Override
  public Uni<Void> asyncStart() {
    //Conexão banco e mapeamento relacional
    Uni<Void> startHibernate = Uni.createFrom().deferred(() -> {
      var pgPort = config().getInteger("pgPort", 5432);
      var props = Map.of("javax.persistence.jdbc.url", "jdbc:postgresql://localhost:" + pgPort + "/vertx-tcc");

      this.emf = Persistence.createEntityManagerFactory("demo", props).unwrap(Mutiny.SessionFactory.class);
      return Uni.createFrom().voidItem();
    });
    startHibernate = vertx.executeBlocking(startHibernate).onItem().invoke(() -> logger.info("✅ Hibernate Reactive is ready"));
    Router router = Router.router(vertx);

    BodyHandler bodyHandler = BodyHandler.create();
    router.post().handler(bodyHandler::handle);

    //Permissões de rotas e cabeçalho
    Set<String> allowHeaders = new HashSet<>();
    allowHeaders.add("x-requested-with");
    allowHeaders.add("Access-Control-Allow-Origin");
    allowHeaders.add("origin");
    allowHeaders.add("Content-Type");
    allowHeaders.add("accept");
    Set<HttpMethod> allowMethods = new HashSet<>();
    allowMethods.add(HttpMethod.GET);
    allowMethods.add(HttpMethod.POST);
    allowMethods.add(HttpMethod.DELETE);
    allowMethods.add(HttpMethod.PUT);

    router.route().handler(CorsHandler.create("*")
      .allowedHeaders(allowHeaders)
      .allowedMethods(allowMethods));
    router.route().handler(BodyHandler.create());

    //Idioma
    router.post("/idiomas").respond(this::createIdioma);
    router.put("/idiomas").respond(this::updateIdioma);
    router.get("/idiomas").respond(this::findIdiomas);
    router.get("/idiomas/:id").respond(this::findIdioma);
    router.delete("/idiomas/:id").respond(this::deleteIdioma);

    //Categoria
    router.post("/categorias").respond(this::createCategoria);
    router.put("/categorias").respond(this::updateCategoria);
    router.get("/categorias").respond(this::findCategorias);
    router.get("/categorias/:id").respond(this::findCategoria);
    router.delete("/categorias/:id").respond(this::deleteCategoria);

    //Filme
    router.post("/filmes").respond(this::createFilme);
    router.put("/filmes").respond(this::updateFilme);
    router.get("/filmes").respond(this::findFilmes);
    router.get("/filmes/:id").respond(this::findFilme);
    router.delete("/filmes/:id").respond(this::deleteFilme);

    //Usuário
    router.post("/usuarios").respond(this::createUsuario);
    router.put("/usuarios").respond(this::updateUsuario);
    router.get("/usuarios").respond(this::findUsuarios);
    router.get("/usuarios/:id").respond(this::findUsuario);
    router.delete("/usuarios/:id").respond(this::deleteUsuario);

    Uni<HttpServer> startHttpServer = vertx.createHttpServer()
      .requestHandler(router)
      .listen(8080)
      .onItem().invoke(() -> logger.info("✅ HTTP server listening on port 8080"));

    return Uni.combine().all().unis(startHibernate, startHttpServer).discardItems();
  }

  private Uni<List<Usuario>> findUsuarios(RoutingContext ctx) {
    return emf.withSession(session -> session.createQuery(
      "from Usuario", Usuario.class
    ).getResultList());
  }

  private Uni<Usuario> findUsuario(RoutingContext ctx) {
    long id = Long.parseLong(ctx.pathParam("id"));
    return emf.withSession(session -> session
        .find(Usuario.class, id))
      .onItem().ifNull().continueWith(Usuario::new);
  }

  private Uni<Usuario> createUsuario(RoutingContext ctx) {
    Usuario usuario = ctx.body().asPojo(Usuario.class);
    return emf.withSession(session -> session.persist(usuario)
      .call(session::flush)
      .replaceWith(usuario));
  }

  private Uni<Usuario> updateUsuario(RoutingContext ctx) {
    Usuario usuario = ctx.body().asPojo(Usuario.class);
    return emf.withTransaction((s,t) -> s.find(Usuario.class, usuario.getId())
      .onItem().ifNotNull().invoke(entity -> {
        if (usuario.getNome() != null) {
          entity.setNome(usuario.getNome());
        }
        if (usuario.getCpf() != null) {
          entity.setCpf(usuario.getCpf());
        }
        if (usuario.getTelefone() != null) {
          entity.setTelefone(usuario.getTelefone());
        }
        if (usuario.getEmail() != null) {
          entity.setEmail(usuario.getEmail());
        }
        if (usuario.getSenha() != null) {
          entity.setSenha(usuario.getSenha());
        }
        if (usuario.getIdioma() != null) {
          entity.setIdioma(usuario.getIdioma());
        }
      }));
  }

  private Uni<Void> deleteUsuario(RoutingContext ctx) {
    long id = Long.parseLong(ctx.pathParam("id"));
    return emf.withTransaction((s, t) ->
      s.find(Usuario.class, id)
        .onItem().ifNotNull()
        .transformToUni(entity -> s.remove(entity)
          .replaceWith(() -> "Usuario excluido com sucesso"))
        .onItem().ifNull().continueWith(() -> "Usuario não encontrado")).replaceWithVoid();
  }

  private Uni<List<Filme>> findFilmes(RoutingContext ctx) {
    return emf.withSession(session -> session.createQuery(
      "from Filme", Filme.class
    ).getResultList());
  }

  private Uni<Filme> findFilme(RoutingContext ctx) {
    long id = Long.parseLong(ctx.pathParam("id"));
    return emf.withSession(session -> session
        .find(Filme.class, id))
      .onItem().ifNull().continueWith(Filme::new);
  }

  private Uni<Filme> createFilme(RoutingContext ctx) {
    Filme filme = ctx.body().asPojo(Filme.class);
    return emf.withSession(session -> session.persist(filme)
      .call(session::flush)
      .replaceWith(filme));
  }

  private Uni<Filme> updateFilme(RoutingContext ctx) {
    Filme filme = ctx.body().asPojo(Filme.class);
    return emf.withTransaction((s,t) -> s.find(Filme.class, filme.getId())
      .onItem().ifNotNull().invoke(entity -> {
        if (filme.getTitulo() != null) {
          entity.setTitulo(filme.getTitulo());
        }
        if (filme.getSinopse() != null) {
          entity.setSinopse(filme.getSinopse());
        }
        if (filme.getImagem() != null) {
          entity.setImagem(filme.getImagem());
        }
        if (filme.getDataLancamento() != null) {
          entity.setDataLancamento(filme.getDataLancamento());
        }
        if (filme.getDuracao() != null) {
          entity.setDuracao(filme.getDuracao());
        }
        if (filme.getIdioma() != null) {
          entity.setIdioma(filme.getIdioma());
        }
        if (filme.getCategoria() != null) {
          entity.setCategoria(filme.getCategoria());
        }
      }));
  }

  private Uni<Void> deleteFilme(RoutingContext ctx) {
    long id = Long.parseLong(ctx.pathParam("id"));
    return emf.withTransaction((s, t) ->
      s.find(Filme.class, id)
        .onItem().ifNotNull()
        .transformToUni(entity -> s.remove(entity)
          .replaceWith(() -> "Filme excluido com sucesso"))
        .onItem().ifNull().continueWith(() -> "Filme não encontrado")).replaceWithVoid();
  }

  private Uni<List<Categoria>> findCategorias(RoutingContext ctx) {
    return emf.withSession(session -> session.createQuery(
      "from Categoria", Categoria.class
    ).getResultList());
  }

  private Uni<Categoria> findCategoria(RoutingContext ctx) {
    long id = Long.parseLong(ctx.pathParam("id"));
    return emf.withSession(session -> session
        .find(Categoria.class, id))
      .onItem().ifNull().continueWith(Categoria::new);
  }

  private Uni<Categoria> createCategoria(RoutingContext ctx) {
    Categoria categoria = ctx.body().asPojo(Categoria.class);
    return emf.withSession(session -> session.persist(categoria)
      .call(session::flush)
      .replaceWith(categoria));
  }

  private Uni<Categoria> updateCategoria(RoutingContext ctx) {
    Categoria categoria = ctx.body().asPojo(Categoria.class);
    return emf.withTransaction((s,t) -> s.find(Categoria.class, categoria.getId())
      .onItem().ifNotNull().invoke(entity -> {
        if(categoria.getNome() != null) {
          entity.setNome(categoria.getNome());
        }
        if(categoria.getTag() != null) {
          entity.setTag(categoria.getTag());
        }
        if(categoria.getIdioma() != null) {
          entity.setIdioma(categoria.getIdioma());
        }
      }));
  }

  private Uni<Void> deleteCategoria(RoutingContext ctx) {
    long id = Long.parseLong(ctx.pathParam("id"));
    return emf.withTransaction((s, t) ->
      s.find(Categoria.class, id)
        .onItem().ifNotNull()
        .transformToUni(entity -> s.remove(entity)
          .replaceWith(() -> "Categoria excluida com sucesso"))
        .onItem().ifNull().continueWith(() -> "Categoria não encontrada")).replaceWithVoid();
  }

  private Uni<Idioma> createIdioma(RoutingContext ctx) {
    Idioma idioma = ctx.body().asPojo(Idioma.class);
    return emf.withSession(session -> session.persist(idioma)
      .call(session::flush)
      .replaceWith(idioma));
  }

  private Uni<Idioma> updateIdioma(RoutingContext ctx) {
    Idioma idioma = ctx.body().asPojo(Idioma.class);
    return emf.withTransaction((s,t) -> s.find(Idioma.class, idioma.getId())
      .onItem().ifNotNull().invoke(entity -> {
        if(idioma.getNome() != null) {
          entity.setNome(idioma.getNome());
        }
        if(idioma.getTag() != null) {
          entity.setTag(idioma.getTag());
        }
        }));
  }

  private Uni<List<Idioma>> findIdiomas(RoutingContext ctx) {
    return emf.withSession(session -> session.createQuery(
      "from Idioma", Idioma.class
    ).getResultList());
  }

  private Uni<Idioma> findIdioma(RoutingContext ctx) {
    long id = Long.parseLong(ctx.pathParam("id"));
    return emf.withSession(session -> session
        .find(Idioma.class, id))
      .onItem().ifNull().continueWith(Idioma::new);
  }

  private Uni<Void> deleteIdioma(RoutingContext ctx) {
    long id = Long.parseLong(ctx.pathParam("id"));
    return emf.withTransaction((s, t) ->
      s.find(Idioma.class, id)
        .onItem().ifNotNull()
        .transformToUni(entity -> s.remove(entity)
          .replaceWith(() -> "Idioma excluido com sucesso"))
        .onItem().ifNull().continueWith(() -> "Idioma não encontrado")).replaceWithVoid();
  }


  public static void main(String[] args) {
    logger.info("🚀 Starting Vert.x");
    Vertx vertx = Vertx.vertx();
    DeploymentOptions options = new DeploymentOptions();
    vertx.deployVerticle(MainVerticle::new, options).subscribe().with(
      ok -> {
        logger.info("✅ Deployment success");
        logger.info("💡 Vert.x app started");
      },
      err -> logger.error("🔥 Deployment failure", err));
  }
}
