# RAIDERIO-LADDER BACKEND

Backend of:
* [https://alcaland-ranks.netlify.app/c1b618e0-7540-467c-b96b-34d16ee15cb8](alcaland-ranks)
* [https://osborno-gestiones.netlify.app/](osborno-gestiones)

* Main language: https://kotlinlang.org/
* HTTP framework: https://ktor.io/docs/welcome.html
* SQL Library: https://github.com/JetBrains/Exposed
* Migrations Library: https://flywaydb.org/
* Enhanced functional programming: https://arrow-kt.io/learn/overview/


## How to start the server and test some api calls

* Start the database with docker-compose up. Be sure to set up the following environment variables first either with direnv allow or manually:
  * POSTGRES_DB
  * POSTGRES_USER
  * POSTGRES_PASSWORD
* Add a record on users table with a name and a password.
* Add a record on credentials_roles table with the name created before and role 'admin'.
* Set up the postman using the file in src/main/resources/raiderio-ladder-backend.postman_collection.json
* Change the user and password environment variables from postman to the ones added in the database.
* Run the Application.kt main function filling the required environment variables (those in .envrc file)
* Use the login api call, and after you are free to try all endpoints
