# Scalix: API TMDB Interaction Project

## 📋 **Description**
Scalix est un projet en **Scala** permettant d'interagir avec l'API **TMDB** (The Movie Database). Le projet inclut des fonctionnalités pour :
- Rechercher les **ID des acteurs** en utilisant leurs prénoms et noms.
- Récupérer les **films associés** à un acteur donné.
- Identifier le **réalisateur** d'un film spécifique.
- Trouver les **collaborations** entre deux acteurs (films en commun et réalisateurs).

Deux modes d'interaction avec l'API sont disponibles :
1. **SimpleRequest** : Interaction directe avec l'API.
2. **CachedRequest** : Utilisation d'un système de cache (JSON) pour éviter des requêtes répétées.

## 🚀 **Technologies Utilisées**
- **Scala 3.3.4**
- **JSON4S** : Pour la manipulation des données JSON.
- **TMDB API** : Source de données pour les films, acteurs et réalisateurs.
- **SBT** : Build tool.

## 📁 **Structure du Projet**

```
scalix/
│
├── cache/                     # Dossiers des fichiers de cache JSON
│   ├── actor_id.json
│   ├── actor_movies.json
│   └── movie.json
│
├── data/                      # Contient les modèles de données
│   ├── .env                   # Contient la clé API
│   ├── Director               # Réalisateur (placeholder)
│   ├── FullName               # Classe pour gérer le nom complet d'une personne
│   └── Movie.java             # Classe Java (optionnelle)
│
├── main/                      # Classes de test
│   ├── TestCache              # Tests pour le mode cache
│   └── TestNoCache            # Tests pour le mode sans cache
│
├── request/                   # Requêtes vers l'API TMDB
│   ├── APIService             # Trait pour l'implémentation de base
│   ├── CachedRequest          # Requêtes avec système de cache
│   └── SimpleRequest          # Requêtes sans cache
│
└── build.sbt                  # Configuration SBT
```

## 🛠️ **Pré-requis**

1. Installer **SBT** et **Scala 3.3.4** :
   ```bash
   brew install sbt
   ```

2. Obtenir une clé API depuis [TMDB](https://www.themoviedb.org/).

3. Créer un fichier `.env` dans `data/` :
   ```text
   TMDB_API_KEY=YOUR_API_KEY_HERE
   ```

## 🏋️‍♂️ **Installation et Exécution**

1. **Cloner le projet** :
   ```bash
   git clone https://github.com/username/scalix.git
   cd scalix
   ```

2. **Lancer le projet** :
   ```bash
   sbt run
   ```

3. **Tester les fonctionnalités** :
   - En mode **SimpleRequest** (sans cache)
   - En mode **CachedRequest** (avec cache)

   Exemple de commande dans un fichier de test :
   ```scala
   val apiKey = "VOTRE_API_KEY"
   val service = new CachedRequest(apiKey)

   val actor1 = FullName("Leonardo", "DiCaprio")
   val actor2 = FullName("Brad", "Pitt")

   val collaborations = service.collaboration(actor1, actor2)
   println(s"Films communs réalisés par les mêmes réalisateurs : $collaborations")
   ```

## 🧠 **Exécution des Tests**
Utilisez `sbt` pour compiler et tester le projet :
```bash
sbt test
```

## 🔑 **Fonctionnalités Principales**

### Recherche d'un ID d'Acteur
```scala
service.findActorId("Leonardo", "DiCaprio")
```

### Récupération des Films d'un Acteur
```scala
service.findActorMovies(actorId = 6193)
```

### Recherche du Réalisateur d'un Film
```scala
service.findMovieDirector(movieId = 550)
```

### Collaborations Entre Deux Acteurs
```scala
val collaborations = service.collaboration(actor1, actor2)
```

## 📝 **Dépendances**
Les dépendances principales sont listées dans le `build.sbt` :
```scala
libraryDependencies += "org.json4s" %% "json4s-ast" % "4.1.0-M8"
libraryDependencies += "org.json4s" %% "json4s-native" % "4.1.0-M8"
libraryDependencies += "com.squareup.okhttp3" % "okhttp" % "4.9.3"
```

## 📄 **Licence**
Ce projet est sous licence **MIT**. Voir [LICENSE](LICENSE) pour plus de détails.

---

N'hésitez pas à contribuer à ce projet ou à poser des questions si besoin ! 🌐
