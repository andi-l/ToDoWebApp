
# ToDoWebApp Projektbeschreibung

Das Projekt ist eine Webanwendung zur Verwaltung von Aufgaben (To-Do-Listen). Es ermöglicht Benutzern, Aufgaben zu erstellen, zu bearbeiten, zu löschen und zu verwalten.

## Architektur

Die Anwendung basiert auf einer RESTful- und GraphQL-Architektur. Sie wurde mit einem robusten Backend entwickelt, das eine einfache und intuitive Benutzererfahrung unterstützt. Die Anwendung ermöglicht CRUD-Operationen für die Verwaltung von Aufgaben und verwendet eine strukturierte Datenbank, um alle Benutzer- und Aufgabeninformationen zu speichern.

### Technologie-Stack:

- **Backend**: Spring Boot (Java)
- **Datenbank**: PostgreSQL
- **API-Technologie**: REST und GraphQL

## Endpunkte

### REST-API

| HTTP-Methode | URI                | Beschreibung                                   | HTTP Statuscodes                       |
|--------------|--------------------|-----------------------------------------------|---------------------------------------|
| POST         | `/api/tasks`       | Erstellt eine neue Aufgabe                    | 201 Created, 400 Bad Request          |
| GET          | `/api/tasks`       | Gibt alle Aufgaben zurück                     | 200 OK                                |
| GET          | `/api/tasks/{id}`  | Gibt eine spezifische Aufgabe zurück          | 200 OK, 404 Not Found                 |
| PUT          | `/api/tasks/{id}`  | Aktualisiert eine spezifische Aufgabe         | 200 OK, 400 Bad Request, 404 Not Found |
| DELETE       | `/api/tasks/{id}`  | Löscht eine spezifische Aufgabe               | 200 OK, 404 Not Found                 |

## Features

1. **Benutzerfreundliche API**: Intuitive REST- und GraphQL-Endpunkte zur Verwaltung von Aufgaben.
2. **Vollständige CRUD-Funktionalität**: Aufgaben können erstellt, gelesen, aktualisiert und gelöscht werden.
3. **Filter- und Suchfunktion**: Aufgaben können nach Status oder Stichwörtern gefiltert werden.
4. **Flexible Datenabfragen**: Dank GraphQL können Benutzer genau die Daten abrufen, die sie benötigen.
