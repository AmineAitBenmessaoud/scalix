package request

import model.FullName
import org.json4s.*
import org.json4s.native.JsonMethods.*
import org.json4s.JsonDSL.*

import scala.io.Source
import java.io.{File, PrintWriter}

class CachedRequest(override val apiKey: String) extends APIService(apiKey) {
  // Définitions des URLs pour les fichiers de cache
  val cache_movie: String = "./src/main/scala/cache/movie.json"
  val cache_actor_id: String = "./src/main/scala/cache/actor_id.json"
  val cache_actor_movies: String = "./src/main/scala/cache/actor_movies.json"

  // Maps pour le cache en mémoire
  var actorIdCache: Map[(String, String), Int] = Map()
  var actorMoviesCache: Map[Int, Set[(Int, String)]] = Map()

  // Vérifie et initialise les fichiers de cache
  initializeCacheFile(cache_movie)
  initializeCacheFile(cache_actor_id)
  initializeCacheFile(cache_actor_movies)

  /**
   * Vérifie si un fichier de cache existe, sinon le crée avec un contenu vide JSON.
   * @param path Le chemin du fichier.
   */
  private def initializeCacheFile(path: String): Unit = {
    val file = new File(path)
    if (!file.exists()) {
      println(s"Fichier de cache non trouvé : $path. Création du fichier...")
      val writer = new PrintWriter(file)
      writer.println("{}") // Fichier JSON vide
      writer.close()
    }
  }

  /**
   * Recherche dans le cache primaire (en mémoire).
   */
  private def searchPrimaryCache[K, V](cache: Map[K, V], key: K): Option[V] = {
    cache.get(key)
  }

  /**
   * Charge un fichier JSON et retourne son contenu ainsi qu'un flux d'écriture.
   */
  private def loadCacheFile(path: String): (JObject, PrintWriter) = {
    val source = Source.fromURL("file:" + path)
    try {
      val contents = source.mkString
      val writer = new PrintWriter(path)
      val jsonContents = parse(contents).asInstanceOf[JObject]
      (jsonContents, writer)
    } finally {
      source.close() // Libère les ressources
    }
  }

  override def findActorId(firstName: String, lastName: String): Option[Int] = {
    println("Map : " + actorIdCache)
    val query = s"$firstName+$lastName"

    val primaryCacheResult = searchPrimaryCache(actorIdCache, (firstName, lastName))

    primaryCacheResult match {
      case Some(value) => println("Le cache primaire a été utilisé"); return Some(value)
      case None => // Continuer la recherche
    }

    val (jsonContents, writer) = loadCacheFile(cache_actor_id)

    val cachedValue = for {
      case JField(key, JInt(value)) <- jsonContents.obj
      if key == query
    } yield value.toInt

    if (cachedValue.nonEmpty) {
      println("le cache secondaire a été utilisé")
      actorIdCache = actorIdCache + ((firstName, lastName) -> cachedValue.head)
      writer.println(compact(render(jsonContents)))
      writer.close()
      return Some(cachedValue.head)
    }

    val apiResult = super.findActorId(firstName, lastName)

    apiResult match {
      case None => None
      case Some(value) =>
        val updatedJson = jsonContents ~ (query -> JInt(value))
        println("le fichier JSON a été mis à jour : " + compact(render(updatedJson)))
        writer.println(compact(render(updatedJson)))
        actorIdCache = actorIdCache + ((firstName, lastName) -> value)
        writer.close()
        Some(value)
    }
  }

  override def findActorMovies(actorId: Int): Set[(Int, String)] = {
    println("Map : " + actorMoviesCache)
    val primaryCacheResult = searchPrimaryCache(actorMoviesCache, actorId)

    primaryCacheResult match {
      case Some(value) => println("Le cache primaire a été utilisé"); return value
      case None => // Continuer la recherche
    }

    val (jsonContents, writer) = loadCacheFile(cache_actor_movies)

    val cachedMovies = (for {
      case JField(key, JArray(value)) <- jsonContents.obj
      case JObject(elem) <- value
      case JField("id", JInt(id)) <- elem
      case JField("title", JString(title)) <- elem
      if key == (actorId + "")
    } yield (id.toInt, title)).toSet

    if (cachedMovies.nonEmpty) {
      println("le cache secondaire a été utilisé")
      actorMoviesCache = actorMoviesCache + (actorId -> cachedMovies)
      writer.println(compact(render(jsonContents)))
      writer.close()
      return cachedMovies
    }

    val apiResult = super.findActorMovies(actorId)

    val actorMoviesJson = JArray(apiResult.map { case (id, title) =>
      JObject("id" -> JInt(id), "title" -> JString(title))
    }.toList)

    val updatedJson = jsonContents ~ ((actorId + "") -> actorMoviesJson)
    println("le fichier JSON a été mis à jour : " + compact(render(updatedJson)))
    writer.println(compact(render(updatedJson)))
    writer.close()
    actorMoviesCache = actorMoviesCache + (actorId -> apiResult)

    apiResult
  }

  override def findMovieDirector(movieId: Int): Option[(Int, String)] = {
    super.findMovieDirector(movieId) // Pas de cache pour cette méthode
  }

  override def collaboration(actor1: FullName, actor2: FullName): Set[(String, String)] = {
    super.collaboration(actor1, actor2)
    // Pas de cache
  }
}
