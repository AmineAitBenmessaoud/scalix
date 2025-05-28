# Scalix: API TMDB Interaction Project

## **Description**
Scalix est un projet en **Scala** permettant d'interagir avec l'API **TMDB** (The Movie Database). Le projet inclut des fonctionnalités pour :
- Rechercher les **ID des acteurs** en utilisant leurs prénoms et noms.
- Récupérer les **films associés** à un acteur donné.
- Identifier le **réalisateur** d'un film spécifique.
- Trouver les **collaborations** entre deux acteurs (films en commun et réalisateurs).

Deux modes d'interaction avec l'API sont disponibles :
1. **SimpleRequest** : Interaction directe avec l'API.
2. **CachedRequest** : Utilisation d'un système de cache (JSON) pour éviter des requêtes répétées.

## **Technologies Utilisées**
- **Scala 3.3.4**
- **JSON4S** : Pour la manipulation des données JSON.
- **TMDB API** : Source de données pour les films, acteurs et réalisateurs.
- **SBT** : Build tool.

##  **Structure du Projet**

```
scalix/
│
├── cache/                     # Dossiers des fichiers de cache JSON
│   ├── actor_id.json
│   ├── actor_movies.json
│   └── movie.json
│
├── data/                      # Contient les modèles de données                   
│   ├── Director               # Réalisateur (placeholder)
    ├── Actor               
│   ├── FullName               # Classe pour gérer le nom complet d'une personne
│   └── Movie                  # Classe Movie
│
├── main/                      # Classes de test
│   ├── .env                   # Contient la clé API   
│   ├── TestCache              # Tests pour le mode cache
│   └── TestWithoutCache            # Tests pour le mode sans cache
│
├── request/                   # Requêtes vers l'API TMDB
│   ├── APIService             # Trait pour l'implémentation de base
│   ├── CachedRequest          # Requêtes avec système de cache
│   └── SimpleRequest          # Requêtes sans cache
│
└── build.sbt                  # Configuration SBT
```

##  **Pré-requis**

1. Installer **SBT** et **Scala 3.3.4** :
   ```bash
   brew install sbt
   ```

2. Obtenir une clé API depuis [TMDB](https://www.themoviedb.org/).

3. Créer un fichier `.env` dans `main/` s'il n'existe pas :
   ```text
   API_KEY=YOUR_API_KEY_HERE
   ```

##  **Installation et Exécution**

1. **Cloner le projet** :
   ```bash
   git clone https://github.com/AmineAitBenmessaoud/scalix.git
   cd scalix
   ```

2. **Lancer le projet** :
   ```bash
   sbt run
   ```

3. **Tester les fonctionnalités** :
   - En mode **SimpleRequest** (sans cache)
   - En mode **CachedRequest** (avec cache)

Pour tester les deux cas (sans cache, avec cache), on exécute:

    - main/TestCache.scala
    
    - main/TestWithoutCache.scala


##  **Fonctionnalités Principales**

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

##  **Dépendances**
Les dépendances principales sont listées dans le `build.sbt` :
```scala
libraryDependencies += "org.json4s" %% "json4s-ast" % "4.1.0-M8"
libraryDependencies += "org.json4s" %% "json4s-native" % "4.1.0-M8"
libraryDependencies += "com.squareup.okhttp3" % "okhttp" % "4.9.3"
```

#  **Partie 5: Architecture **

## Avantages de cette nouvelle organisation :

### 1. Encapsulation des données et comportements
- Les classes `Actor` et `Movie` et les autres classes dans le répertoire model encapsulent leurs propres données (ID, nom, etc.) et méthodes associées (ex. : `fetchId`, `fetchMovies`).

### 2. Lisibilité accrue
- Le code devient plus lisible, car les actions sont effectuées directement sur des objets `Actor` ou `Movie`, au lieu de passer par des méthodes séparées.

### 3. Réutilisabilité
- Les classes peuvent être réutilisées dans d'autres parties du projet ou dans de nouvelles fonctionnalités. Par exemple, si on veut étendre notre application.

### 4. Respect du paradigme orienté objet
- Cette approche respecte le paradigme de la programmation orientée objet en associant données et comportements dans des objets.

---

## Inconvénients :

### 1. Complexité initiale
- Cela demande un effort supplémentaire pour créer des classes et refactoriser le code existant.

## On a utilisé Github Copilot pour faire quelques commentaires et résoudre des bugs quand on est bloqué





---
