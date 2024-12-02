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
    val id = (json \"results" \"id")
    println(id) // Print response body

    Some(id)
  }
}
