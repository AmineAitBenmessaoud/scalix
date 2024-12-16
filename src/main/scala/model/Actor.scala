package model

import request.APIService

case class Actor(firstName: String, lastName: String, id: Option[Int] = None) {

  // Méthode pour rechercher l'ID de l'acteur via l'API
  def fetchId(apiService: APIService): Actor = {
    val fetchedId = apiService.findActorId(firstName, lastName)
    this.copy(id = fetchedId)
  }

  // Méthode pour récupérer les films associés à l'acteur
  def fetchMovies(apiService: APIService): Set[Movie] = {
    id match {
      case Some(actorId) =>
        apiService.findActorMovies(actorId).map { case (movieId, title) =>
          Movie(movieId, title)
        }
      case None =>
        println(s"ID non trouvé pour l'acteur $firstName $lastName")
        Set.empty
    }
  }
}

