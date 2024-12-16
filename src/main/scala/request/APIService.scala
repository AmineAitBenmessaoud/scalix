package request

import scala.io.Source
import org.json4s.native.JsonMethods.*
import org.json4s.*

import model.FullName

/**
 * Service de base pour interagir avec l'API TMDB.
 * Fournit des méthodes pour récupérer des informations sur les acteurs, les films et les réalisateurs.
 * Toutes les requêtes API sont effectuées via cette classe.
 */
trait APIService(val apiKey: String) {

  /**
   * Recherche l'ID d'un acteur en utilisant son prénom et son nom.
   * @param firstName Prénom de l'acteur.
   * @param lastName Nom de famille de l'acteur.
   * @return Option contenant l'ID de l'acteur ou None si non trouvé.
   */
  def findActorId(firstName: String, lastName: String): Option[Int] = {
    val query = s"$firstName+$lastName"
    val url = f"https://api.themoviedb.org/3/search/person?api_key=$apiKey&query=$query"
    val source = Source.fromURL(url)
    val result = parse(source.mkString)

    (for {
      case JObject(root) <- result
      case JField("results", JArray(results)) <- root
      case JObject(actor) <- results
      case JField("id", JInt(id)) <- actor
    } yield id.toInt).headOption
  }

  /**
   * Récupère les films associés à un acteur via son ID.
   * @param actorId L'ID de l'acteur.
   * @return Ensemble des films sous forme de tuples (ID du film, titre).
   */
  def findActorMovies(actorId: Int): Set[(Int, String)] = {
    val url = f"https://api.themoviedb.org/3/person/$actorId/movie_credits?api_key=$apiKey"
    val source = Source.fromURL(url)
    val result = parse(source.mkString)
    (for {
      case JObject(root) <- result
      case JField("cast", JArray(cast)) <- root
      case JObject(movie) <- cast
      case JField("id", JInt(movieId)) <- movie
      case JField("title", JString(title)) <- movie
    } yield (movieId.toInt, title)).toSet
  }

  /**
   * Récupère le réalisateur d'un film via l'ID du film.
   * @param movieId L'ID du film.
   * @return Option contenant un tuple (ID du réalisateur, nom) ou None.
   */
  def findMovieDirector(movieId: Int): Option[(Int, String)] = {
    val url = f"https://api.themoviedb.org/3/movie/$movieId/credits?api_key=$apiKey"
    val source = Source.fromURL(url)
    val result = parse(source.mkString)
    (for {
      case JObject(root) <- result
      case JField("crew", JArray(crew)) <- root
      case JObject(member) <- crew
      case JField("job", JString(job)) <- member if job == "Director"
      case JField("id", JInt(id)) <- member
      case JField("name", JString(name)) <- member
    } yield (id.toInt, name)).headOption
  }

  /**
   * Trouve les collaborations entre deux acteurs (films en commun et réalisateur).
   * @param actor1 Premier acteur (prénom et nom).
   * @param actor2 Deuxième acteur (prénom et nom).
   * @return Ensemble des collaborations sous forme de tuples (nom du réalisateur, titre du film).
   */
  def collaboration(actor1: FullName, actor2: FullName): Set[(String, String)] = {
    val actor1_id = findActorId(actor1.firstName, actor1.lastName)
    val actor2_id = findActorId(actor2.firstName, actor2.lastName)

    (actor1_id, actor2_id) match {
      case (Some(id1), Some(id2)) =>
        val moviesActor1 = findActorMovies(id1)
        val moviesActor2 = findActorMovies(id2)
        val sharedMovies = moviesActor1.intersect(moviesActor2)
        sharedMovies.flatMap { case (movieId, movieTitle) =>
          findMovieDirector(movieId).map { case (_, directorName) =>
            (directorName, movieTitle)
          }
        }
      case _ => Set.empty
    }
  }
}
