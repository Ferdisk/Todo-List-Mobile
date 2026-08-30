# Todo-List Mobile

Application Android de gestion de tâches, écrite en Kotlin. Projet de BUT Informatique 2ᵉ année, IUT du Limousin.

**ADRACHI Ferdina · BARDET--TECHER Jordan**

## Fonctionnalités

- Création, édition et suppression de tâches, avec **priorité** et **état**.
- **Tâches récurrentes** : périodicité configurable.
- **Séries** (*streaks*) pour suivre la régularité sur les tâches répétées.
- **Notifications** de rappel.
- **Photo** attachable à une tâche, via le sélecteur système.
- **Filtres** de la liste, et une animation de confettis à la complétion.

## Architecture

Le projet suit un découpage MVVM avec injection de dépendances.

| Couche | Contenu |
|---|---|
| `model/` | `Task`, `Priority`, `State`, `Periodicity` |
| `data/` | `TaskDao`, `TaskDatabase`, `TaskRepository`, `StreakManager` |
| `di/` | `DatabaseModule` — fourniture de la base et du dépôt |
| `viewmodel/` | `TaskViewModel` — état exposé à l'interface |
| `ui/` | Écrans et composants Compose, thème Material 3 |
| `navigation/` | Graphe de navigation entre les écrans |

## Technologies

Kotlin · Jetpack Compose · Material 3 · Navigation Compose · Room (avec KSP) · Hilt · DataStore · Coil · ViewModel

## Construire

```sh
./gradlew assembleDebug
```
