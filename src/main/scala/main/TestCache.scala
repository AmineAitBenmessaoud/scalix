package main
import request.{CachedRequest, SimpleRequest}
import model.FullName

import java.nio.file.{Files, Paths}
import scala.io.Source


object TestCache extends App{

  // Fonction pour charger et parser le fichier .env
  def loadEnv(filename: String): Map[String, String] = {
    val source = Source.fromFile(filename)
    val envVars = source.getLines()
      .filter(line => line.contains("=") && !line.startsWith("#")) // Ignorer les commentaires
      .map { line =>
        val Array(key, value) = line.split("=", 2) // Séparer clé et valeur
        key.trim -> value.trim.stripPrefix("\"").stripSuffix("\"") // Retirer les guillemets autour des valeurs
      }.toMap
    source.close()
    envVars
  }

  // Charger le fichier .env

  val envFilePath = "src/main/scala/main/.env"
  if (Files.exists(Paths.get(envFilePath))) {
    println(s"Le fichier .env existe à l'emplacement: $envFilePath")
  } else {
    println(s"Le fichier .env n'existe pas à l'emplacement: $envFilePath")
  }

  // Charger le fichier .env
  val env = loadEnv(envFilePath)

  // Récupérer des variables d'environnement spécifiques
  val API_KEY = env.getOrElse("API_KEY", "defaultApiKey") // Utiliser une valeur par défaut si la clé n'existe pas

  val request = CachedRequest(API_KEY)

  // Test 1: Trouver l'ID d'un acteur
  val actorId: Option[Int] = request.findActorId("John", "Doe")
  println(s"Actor ID for John Doe: $actorId")

  // Test 2: Trouver les films d'un acteur
  actorId.foreach { id =>
    val movies = request.findActorMovies(id)
    println(s"Movies for John Doe: $movies")
  }

  val actor1 = new FullName("John", "Doe")
  val actor2 = new FullName("Jane", "Smith")

  // Test 3: Trouver la collaboration entre deux acteurs
  private val collaborationMovies = request.collaboration(actor1, actor2)
  println(s"Movies with John Doe and Jane Smith: $collaborationMovies")

  println("Fiiiiiiiiiiiiiiiiiiiin deeeeeeees tests")
}
