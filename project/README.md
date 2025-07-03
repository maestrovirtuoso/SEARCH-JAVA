# Search Service

Service de recherche spécialisé utilisant Elasticsearch pour les requêtes de recherche avancées et ScyllaDB pour le stockage des données de référence.

## Fonctionnalités

- **Recherche full-text** dans les documents
- **Recherche multi-champs** avec pondération
- **Filtrage avancé** par catégorie, auteur, date
- **Pagination** et **tri** des résultats
- **Highlighting** des termes de recherche
- **APIs RESTful** pour l'intégration
- **Architecture asynchrone** pour de meilleures performances
- **Gestion d'erreurs** complète avec validation

## Architecture

### Technologies utilisées
- **Spring Boot 3.2.0** - Framework principal
- **Elasticsearch 8.11.0** - Moteur de recherche
- **ScyllaDB** - Base de données NoSQL haute performance
- **Java 17** - Version Java
- **Maven** - Gestionnaire de dépendances

### Structure du projet
```
src/
├── main/
│   ├── java/com/company/search/
│   │   ├── SearchServiceApplication.java
│   │   ├── config/          # Configuration Elasticsearch et ScyllaDB
│   │   ├── controller/      # Contrôleurs REST
│   │   ├── service/         # Services métier
│   │   ├── repository/      # Accès aux données
│   │   ├── model/           # Modèles et DTOs
│   │   ├── exception/       # Gestion des exceptions
│   │   └── util/            # Utilitaires
│   └── resources/
│       ├── application*.yml # Configuration
│       └── schema/          # Schémas de base de données
└── test/                    # Tests unitaires et d'intégration
```

## Endpoints API

### Recherche
- `POST /api/search` - Recherche avancée avec filtres
- `GET /api/search?query={query}&page={page}&size={size}` - Recherche simple
- `GET /api/search/fields?query={query}&fields={fields}` - Recherche dans des champs spécifiques

### Exemples d'utilisation

#### Recherche simple
```bash
curl "http://localhost:8080/api/search?query=java&page=0&size=10"
```

#### Recherche avancée avec filtres
```bash
curl -X POST http://localhost:8080/api/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "spring boot",
    "filters": {
      "category": "technology",
      "author": "john.doe"
    },
    "page": 0,
    "size": 20,
    "sortBy": "createdAt",
    "sortOrder": "desc"
  }'
```

#### Recherche dans des champs spécifiques
```bash
curl "http://localhost:8080/api/search/fields?query=elasticsearch&fields=title,content&page=0&size=5"
```

## Démarrage rapide

### Prérequis
- Java 17+
- Maven 3.6+
- Docker et Docker Compose (pour les bases de données)

### Lancement avec Docker Compose
```bash
# Lancer tous les services (Elasticsearch, ScyllaDB, Application)
docker-compose up -d

# Vérifier les logs
docker-compose logs -f search-service
```

### Lancement en développement
```bash
# Lancer uniquement les bases de données
docker-compose up -d scylla elasticsearch

# Lancer l'application en mode développement
mvn spring-boot:run
```

### Configuration des schémas

#### ScyllaDB
```bash
# Se connecter à ScyllaDB
docker exec -it scylla-search cqlsh

# Exécuter le schéma
SOURCE '/app/src/main/resources/schema/scylla-schema.cql';
```

#### Elasticsearch
```bash
# Créer l'index avec le mapping
curl -X PUT "localhost:9200/documents" \
  -H "Content-Type: application/json" \
  -d @src/main/resources/schema/elasticsearch-mapping.json
```

## Configuration

### Variables d'environnement

#### Développement
- `SPRING_PROFILES_ACTIVE=dev`
- `ELASTICSEARCH_HOST=localhost`
- `SCYLLA_HOSTS=localhost`

#### Production
- `SPRING_PROFILES_ACTIVE=prod`
- `ELASTICSEARCH_HOST=elasticsearch-cluster`
- `ELASTICSEARCH_PORT=9200`
- `SCYLLA_HOSTS=scylla-cluster`
- `SCYLLA_PORT=9042`
- `SCYLLA_KEYSPACE=search_data`
- `SCYLLA_DATACENTER=datacenter1`

### Profils de configuration
- `dev` - Développement local
- `prod` - Production
- `test` - Tests automatisés

## Tests

### Exécution des tests
```bash
# Tests unitaires
mvn test

# Tests avec couverture
mvn test jacoco:report

# Tests d'intégration
mvn verify
```

### Types de tests
- **Tests unitaires** - Services et utilitaires
- **Tests de contrôleurs** - API REST
- **Tests d'intégration** - Avec Testcontainers

## Monitoring et Observabilité

### Endpoints de monitoring
- `/actuator/health` - État de santé
- `/actuator/info` - Informations sur l'application
- `/actuator/metrics` - Métriques

### Logs
- Niveau DEBUG en développement
- Niveau INFO en production
- Logs structurés avec timestamps
- Fichiers de logs dans `/logs/`

## Performance

### Optimisations
- **Recherche asynchrone** avec CompletableFuture
- **Pool de threads** configuré pour les opérations async
- **Pagination** efficace
- **Cache** au niveau Elasticsearch
- **Index optimisés** ScyllaDB

### Métriques clés
- Temps de réponse des recherches
- Throughput des requêtes
- Utilisation mémoire
- Connexions aux bases de données

## Sécurité

### Validation
- Validation des entrées avec Bean Validation
- Échappement des caractères spéciaux
- Limitation de la taille des requêtes

### Gestion d'erreurs
- Exceptions métier personnalisées
- Réponses d'erreur standardisées
- Logs d'audit des erreurs

## Déploiement

### Docker
```bash
# Build de l'image
docker build -t search-service:latest .

# Lancement
docker run -p 8080:8080 search-service:latest
```

### Production
- Utiliser des clusters Elasticsearch et ScyllaDB
- Configurer la réplication des données
- Mettre en place la surveillance
- Configurer les sauvegardes

## Développement

### Structure des packages
- `config` - Configuration Spring
- `controller` - Contrôleurs REST
- `service` - Logique métier
- `repository` - Accès aux données
- `model` - Entités et DTOs
- `exception` - Gestion des erreurs
- `util` - Utilitaires

### Bonnes pratiques
- Code modulaire et testable
- Séparation des responsabilités
- Documentation du code
- Tests automatisés
- Gestion des erreurs

## Contribution

1. Fork du projet
2. Création d'une branche feature
3. Commits avec messages descriptifs
4. Tests et documentation
5. Pull request

## Licence

Ce projet est sous licence MIT.