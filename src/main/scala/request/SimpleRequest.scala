package request

import model.FullName

/**
 * Implémentation de base utilisant `APIService`.
 * Toutes les méthodes se contentent d'appeler les fonctions de `APIService`.
 */
class SimpleRequest(override val apiKey: String) extends APIService(apiKey) {

    // Trouve l'ID d'un acteur
  override def findActorId(firstName: String, lastName: String): Option[Int] = {
    super.findActorId(firstName, lastName)
  }
    // Trouve les films d'un acteur
  override def findActorMovies(actorId: Int): Set[(Int, String)] = {
    super.findActorMovies(actorId)
  }
   
   // Trouve le réalisateur d'un film
  override def findMovieDirector(movieId: Int): Option[(Int, String)] = {
    super.findMovieDirector(movieId)
  }

  // Trouve les collaborations entre deux acteurs
  override def collaboration(actor1: FullName, actor2: FullName): Set[(String, String)] = {
    super.collaboration(actor1, actor2)
  }
}
