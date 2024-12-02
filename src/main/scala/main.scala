import scala.io.Source
import okhttp3.{OkHttpClient, Request, Response}
import org.json4s._; import org.json4s.native.JsonMethods._
object Main extends App {
  def findActorId(name: String, surname: String): Option[Int] = {
    val client = new OkHttpClient()
    val request = new Request.Builder()
      .url(s"https://api.themoviedb.org/3/search/person?query=${name}+${surname}&include_adult=false&language=en-US&page=1")
      .get()
      .addHeader("accept", "application/json")
      .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1ZDFkOTFlZWNmODdmNDkyNTFlYWQwMDI1Y2EzNWQ0NCIsIm5iZiI6MTczMjcyMDczMC4wNzUsInN1YiI6IjY3NDczODVhYzQ2ZWJkMWZkNGE0MmM0YiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.iD3MRVlWCQs9xsdbLT2DOo9EZOPlsL9_8LLnZCdRXMI")
      .build()

    val response: Response = client.newCall(request).execute()
    val json = parse(response.body().string())

    // Affiche le JSON pour voir sa structure
    println(pretty(render(json)))

    // Extraire le premier résultat s'il existe
    val results = (json \ "results").children
    results.headOption.flatMap { result =>
      (result \ "id") match {
        case JInt(id) => Some(id.toInt)
        case _ => None
      }
    }
  }


  def findActorMovies(actorId: Int): Set[(Int, String)] = {
    val client = new OkHttpClient()
    val request = new Request.Builder()
      .url(s"https://api.themoviedb.org/3/person/$actorId/movie_credits?language=en-US")
      .get()
      .addHeader("accept", "application/json")
      .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1ZDFkOTFlZWNmODdmNDkyNTFlYWQwMDI1Y2EzNWQ0NCIsIm5iZiI6MTczMjcyMDczMC4wNzUsInN1YiI6IjY3NDczODVhYzQ2ZWJkMWZkNGE0MmM0YiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.iD3MRVlWCQs9xsdbLT2DOo9EZOPlsL9_8LLnZCdRXMI")
      .build()

    val response: Response = client.newCall(request).execute()
    val json = parse(response.body().string())

    val movies = for {
      JObject(movie) <- (json \ "cast").children
      JField("id", JInt(id)) <- movie
      JField("title", JString(title)) <- movie
    } yield (id.toInt, title)

    println(movies) // Debugging output
    movies.toSet
  }

  def findMovieDirector(movieId: Int): Option[(Int, String)] = {
    val client = new OkHttpClient()
    val request = new Request.Builder()
      .url(s"https://api.themoviedb.org/3/movie/$movieId/credits?language=en-US")
      .get()
      .addHeader("accept", "application/json")
      .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1ZDFkOTFlZWNmODdmNDkyNTFlYWQwMDI1Y2EzNWQ0NCIsIm5iZiI6MTczMjcyMDczMC4wNzUsInN1YiI6IjY3NDczODVhYzQ2ZWJkMWZkNGE0MmM0YiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.iD3MRVlWCQs9xsdbLT2DOo9EZOPlsL9_8LLnZCdRXMI")
      .build()

    val response: Response = client.newCall(request).execute()
    val json = parse(response.body().string())

    val director = (for {
      JObject(crewMember) <- (json \ "crew").children
      JField("job", JString("Director")) <- crewMember
      JField("id", JInt(id)) <- crewMember
      JField("name", JString(name)) <- crewMember
    } yield (id.toInt, name)).headOption

    println(director) // Debugging output
    director
  }

  case class FullName(name: String, surname: String)

  def collaboration(actor1: FullName, actor2: FullName): Set[(String, String)] = {
    val id1 = findActorId(actor1.name, actor1.surname)
    val id2 = findActorId(actor2.name, actor2.surname)

    (id1, id2) match {
      case (Some(a1), Some(a2)) =>
        val movies1 = findActorMovies(a1)
        val movies2 = findActorMovies(a2)
        val commonMovies = movies1.intersect(movies2)

        val results = commonMovies.flatMap { case (movieId, title) =>
          findMovieDirector(movieId).map { case (_, director) =>
            (title, director)
          }
        }
        println(results) // Debugging output
        results

      case _ => Set.empty
    }
  }

  val actor1 = FullName("Christian", "Bale")
  val actor2 = FullName("Michael", "Caine")

  val collaborations = collaboration(actor1, actor2)

  println("Films réalisés ensemble par Christian Bale et Michael Caine avec leur réalisateur :")
  collaborations.foreach { case (movie, director) =>
    println(s"Film: $movie, Réalisateur: $director")
  }


}