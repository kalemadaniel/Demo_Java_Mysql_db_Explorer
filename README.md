# Demo_Java_Mysql_db_Explorer

Application de bureau développée en **Java Swing** pour explorer une base de données **MySQL ou MariaDB avec XAMPP**.

Elle permet de parcourir les tables, de consulter leurs données dans plusieurs fenêtres et d’exécuter des requêtes SQL.

## Fonctionnalités

- Connexion configurable : serveur, port, base, utilisateur et mot de passe.
  <img width="475" height="287" alt="Capture d&#39;écran 2026-11-05 130940" src="https://github.com/user-attachments/assets/28a4542a-1c1c-4749-98cb-003cde048403" />

- Navigation dans les tables et les vues.
- Affichage des données dans des fenêtres déplaçables et redimensionnables.
  <img width="1920" height="1020" alt="Capture d&#39;écran 2026-11-05 131249" src="https://github.com/user-attachments/assets/67f1b0aa-64a3-404c-984b-4df8b5b4014e" />

- Tri des résultats en cliquant sur les en-têtes.
- Actualisation des données et affichage de la requête utilisée.
- Éditeur SQL avec confirmation pour les instructions ne commençant pas par `SELECT`.
  <img width="1083" height="712" alt="Capture d&#39;écran 2026-11-05 131713" src="https://github.com/user-attachments/assets/39cbb029-ce8b-49dc-8a5d-4e17ca3903a3" />

- Gestion des erreurs de connexion et d’exécution SQL.

> Les grilles sont en lecture seule et affichent au maximum 500 lignes. Les modifications exécutées depuis l’éditeur SQL sont appliquées immédiatement.

## Technologies

- Java 8 ou supérieur
- Swing
- JDBC
- MariaDB Connector/J 3.5.11
- MySQL / MariaDB
- XAMPP
- NetBeans

## Prérequis

- **Java 8 ou supérieur** pour lancer l’application.
- Un **JDK** pour modifier et compiler le code.
- **XAMPP** pour le serveur de base de données et phpMyAdmin.
- **NetBeans**, si vous souhaitez travailler sur le projet dans un IDE.

## Installation

### 1. Récupérer le projet

Téléchargez le dépôt depuis **Code → Download ZIP**, puis décompressez-le.

Vous pouvez par exemple utiliser ce dossier :

```text
D:\projets\Demo_Java_Mysql_db_Explorer
```

### 2. Préparer la base de données

1. Ouvrez **XAMPP Control Panel**.
2. Démarrez **MySQL** et **Apache**.
3. Accédez à [phpMyAdmin](http://localhost/phpmyadmin).
4. Cliquez sur **Importer**.
5. Sélectionnez le fichier `sql/demo.sql`.
6. Exécutez l’import.

Le script crée la base :

```text
demo_java_mysql_db_explorer
```

> Importez le script une seule fois dans une nouvelle base. Il ne supprime pas une base existante et n’est pas prévu pour être réexécuté.

### 3. Lancer l’application

Double-cliquez sur :

```text
Lancer.bat
```

Conservez le dossier `lib` à côté de `app.jar` : il contient le pilote JDBC nécessaire.

### 4. Se connecter

Cliquez sur **Connexion**, puis renseignez les paramètres de votre serveur.

| Paramètre | Valeur de démonstration |
|---|---|
| Serveur | `127.0.0.1` |
| Port | `3306` |
| Base de données | `demo_java_mysql_db_explorer` |
| Utilisateur | `root` |
| Mot de passe | Celui configuré dans XAMPP |

Laissez le mot de passe vide uniquement si votre compte XAMPP est configuré ainsi.

Après connexion, double-cliquez sur une table pour afficher ses données.

## Utilisation avec NetBeans

1. Ouvrez le dossier du projet avec **File → Open Project**.
2. Dans **Properties → Sources**, vérifiez que le dossier des sources est `src/main/java`.
3. Dans **Properties → Run**, choisissez la classe principale :
   ```text
   demo.DatabaseExplorer
   ```
4. Dans **Properties → Libraries**, ajoutez le pilote s’il est absent :
   ```text
   lib/mariadb-java-client-3.5.11.jar
   ```
5. Exécutez **Clean and Build**, puis **Run Project** avec `F6`.

## Base de démonstration

La base représente une petite boutique et contient cinq tables :

| Table | Contenu |
|---|---|
| `T_Articles` | Articles, marques et prix |
| `T_Users` | Utilisateurs fictifs |
| `T_Admins` | Rôles d’administration |
| `T_Commands` | Commandes des utilisateurs |
| `T_CommandLines` | Articles et quantités commandés |

Les tables sont liées par des clés étrangères.

Les données sont fictives. Cette démonstration ne fournit pas de système d’authentification des utilisateurs.

## Exemples SQL

### Afficher les articles par prix décroissant

```sql
SELECT *
FROM T_Articles
ORDER BY UnitaryPrice DESC;
```

### Afficher les commandes et leurs utilisateurs

```sql
SELECT
    c.IdCommand,
    u.Login,
    c.CommandDate,
    c.Status
FROM T_Commands c
JOIN T_Users u ON u.IdUser = c.IdUser;
```

### Calculer le montant de chaque commande

```sql
SELECT
    IdCommand,
    SUM(Quantity * UnitaryPrice) AS Total
FROM T_CommandLines
GROUP BY IdCommand;
```

## Fichiers principaux

| Fichier | Rôle |
|---|---|
| `src/main/java/demo/DatabaseExplorer.java` | Interface et accès à la base |
| `sql/demo.sql` | Création de la base et données de démonstration |
| `lib/mariadb-java-client-3.5.11.jar` | Pilote JDBC |
| `app.jar` | Application compilée |
| `Lancer.bat` | Lancement sous Windows |
| `Compiler.bat` | Compilation avec un JDK |
| `LISEZ-MOI.txt` | Guide d’installation et d’utilisation |

## Dépannage

| Problème | Solution |
|---|---|
| Connexion refusée | Démarrer MySQL dans XAMPP et vérifier le port |
| `Access denied` | Vérifier l’utilisateur et le mot de passe |
| `Unknown database` | Importer `sql/demo.sql` ou corriger le nom de la base |
| `No suitable driver` | Vérifier le dossier `lib` et les bibliothèques NetBeans |
| Java introuvable | Installer Java et l’ajouter au `PATH` |
| Aucune fenêtre au lancement | Vérifier que la classe principale est `demo.DatabaseExplorer` |

Après une modification de structure avec `CREATE TABLE` ou `DROP TABLE`, cliquez sur **Actualiser** dans la barre principale.

## Limites

- Application destinée à une démonstration locale.
- Maximum de 500 lignes affichées par requête, sans pagination.
- Modification des données depuis l’éditeur SQL uniquement.
- Une instruction SQL exécutée à la fois.
- Paramètres de connexion non enregistrés entre les lancements.

## Dépendance tierce

Le projet utilise **MariaDB Connector/J**, distribué sous licence **LGPL-2.1-or-later**.

La licence du pilote est disponible dans `lib/LICENSE-MariaDB.txt`.
