import scala.io.Source
import okhttp3.{OkHttpClient, Request, Response}
import org.json4s._; import org.json4s.native.JsonMethods._
object Main extends App {
  import java.io.{File, PrintWriter}
  import scala.io.Source



  def writeToCache(filename: String, data: String): Unit = {
    val file = new File(s"data/$filename")
    file.getParentFile.mkdirs() // Crée les dossiers si nécessaires
    val writer = new PrintWriter(file)
    writer.write(data)
    writer.close()
  }

  def readFromCache(filename: String): Option[String] = {
    val file = new File(s"data/$filename")
    if (file.exists()) Some(Source.fromFile(file).mkString) else None
  }

  def findActorId(name: String, surname: String): Option[Int] = {
    val client = new OkHttpClient()
    val request = new Request.Builder()
      .url(s"https://api.themoviedb.org/3/search/person?query=${name}+${surname}&include_adult=false&language=en-US&page=1")
      .get()
      .addHeader("accept", "application/json")
      .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1ZDFkOTFlZWNmODdmNDkyNTFlYWQwMDI1Y2EzNWQ0NCIsIm5iZiI6MTczMjcyMDczMC4wNzUsInN1YiI6IjY3NDczODVhYzQ2ZWJkMWZkNGE0MmM0YiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.iD3MRVlWCQs9xsdbLT2DOo9EZOPlsL9_8LLnZCdRXMI") // Remplace par ta vraie clé API
      .build()

    val response: Response = client.newCall(request).execute()
    val jsonString = response.body().string()
    val json = parse(jsonString)

    // Extraire l'ID de l'acteur
    (json \ "results").children.headOption.flatMap { result =>
      (result \ "id") match {
        case JInt(id) =>
          // Utilise l'ID pour créer le nom du fichier cache
          val cacheFile = s"actor${id.toInt}.json"
          // Écrit la réponse dans le fichier cache spécifique à l'ID
          writeToCache(cacheFile, pretty(render(json)))
          Some(id.toInt)
        case _ => None
      }
    }
  }


  def findActorMovies(actorId: Int): Set[(Int, String)] = {
    val cacheFile = s"actor$actorId.json"
    readFromCache(cacheFile) match {
      case Some(data) =>
        val json = parse(data)
        (for {
          JObject(movie) <- (json \ "cast").children
          JField("id", JInt(id)) <- movie
          JField("title", JString(title)) <- movie
        } yield (id.toInt, title)).toSet
      case None =>
        val client = new OkHttpClient()
        val request = new Request.Builder()
          .url(s"https://api.themoviedb.org/3/person/$actorId/movie_credits?language=en-US")
          .get()
          .addHeader("accept", "application/json")
          .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1ZDFkOTFlZWNmODdmNDkyNTFlYWQwMDI1Y2EzNWQ0NCIsIm5iZiI6MTczMjcyMDczMC4wNzUsInN1YiI6IjY3NDczODVhYzQ2ZWJkMWZkNGE0MmM0YiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.iD3MRVlWCQs9xsdbLT2DOo9EZOPlsL9_8LLnZCdRXMI") // Remplace par ta vraie clé API
          .build()
        val response = client.newCall(request).execute()
        val json = response.body().string()
        writeToCache(cacheFile, json)
        (for {
          JObject(movie) <- (parse(json) \ "cast").children
          JField("id", JInt(id)) <- movie
          JField("title", JString(title)) <- movie
        } yield (id.toInt, title)).toSet
    }
  }

  def findMovieDirector(movieId: Int): Option[(Int, String)] = {
    val cacheFile = s"movie$movieId.json"

    readFromCache(cacheFile) match {
      case Some(data) =>
        val json = parse(data)
        (for {
          JObject(crewMember) <- (json \ "crew").children
          JField("job", JString("Director")) <- crewMember
          JField("id", JInt(id)) <- crewMember
          JField("name", JString(name)) <- crewMember
        } yield (id.toInt, name)).headOption

      case None =>
        val client = new OkHttpClient()
        val request = new Request.Builder()
          .url(s"https://api.themoviedb.org/3/movie/$movieId/credits?language=en-US")
          .get()
          .addHeader("accept", "application/json")
          .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1ZDFkOTFlZWNmODdmNDkyNTFlYWQwMDI1Y2EzNWQ0NCIsIm5iZiI6MTczMjcyMDczMC4wNzUsInN1YiI6IjY3NDczODVhYzQ2ZWJkMWZkNGE0MmM0YiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.iD3MRVlWCQs9xsdbLT2DOo9EZOPlsL9_8LLnZCdRXMI") // Remplace par ta vraie clé API
          .build()

        val response = client.newCall(request).execute()
        val json = response.body().string()

        writeToCache(cacheFile, json)

        val parsedJson = parse(json)
        (for {
          JObject(crewMember) <- (parsedJson \ "crew").children
          JField("job", JString("Director")) <- crewMember
          JField("id", JInt(id)) <- crewMember
          JField("name", JString(name)) <- crewMember
        } yield (id.toInt, name)).headOption
    }
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

  def mostFrequentCollaborations(): List[(Int, Int, Int)] = {
    val actorFiles = new File("data").listFiles().filter(_.getName.startsWith("actor"))
    val actorMovieMap = actorFiles.flatMap { file =>
      val actorId = file.getName.stripPrefix("actor").stripSuffix(".json").toInt
      val json = parse(Source.fromFile(file).mkString)
      val movieIds = (json \ "cast").children.flatMap {
        case JObject(fields) =>
          fields.collectFirst { case JField("id", JInt(id)) => id.toInt }
        case _ => None
      }.toSet
      Some(actorId -> movieIds)
    }.toMap

    val collaborations = actorMovieMap.toSeq.combinations(2).flatMap {
      case Seq((actor1, movies1), (actor2, movies2)) =>
        val commonMovies = movies1.intersect(movies2)
        if (commonMovies.nonEmpty) Some((actor1, actor2, commonMovies.size)) else None
    }.toList

    collaborations.sortBy(-_._3) // Trier par fréquence décroissante
  }


  // --- Classes et Objets orientés objet ---
  case class Actor(id: Int, name: String) {
    def movies: Set[Movie] = Movie.findByActorId(id)
  }

  case class Movie(id: Int, title: String) {
    def director: Option[Director] = Director.findByMovieId(id)
  }

  case class Director(id: Int, name: String)

  object Actor {
    def findByName(name: String, surname: String): Option[Actor] = {
      val actorId = Main.findActorId(name, surname)
      actorId.map(id => Actor(id, s"$name $surname"))
    }
  }

  object Movie {
    def findByActorId(actorId: Int): Set[Movie] = {
      Main.findActorMovies(actorId).map { case (id, title) => Movie(id, title) }
    }
  }

  object Director {
    def findByMovieId(movieId: Int): Option[Director] = {
      Main.findMovieDirector(movieId).map { case (id, name) => Director(id, name) }
    }
  }
}
