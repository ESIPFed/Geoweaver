# dmulligan/docker-example-tomcat-mysql

A docker compose example project with a MySQL and a Tomcat container linked together.

To run: 

	$ docker-compose up

Containers:
- MySQL: on startup, the container executes a simple database initialisation script `./db/mysql-init.sql`, which
  creates a database containing a single table which is populated with a few records.
- Tomcat: a simple web application, located within `./tomcat/webapps`, is deployed. The application contains some JSPs
  to test the database link between the Tomcat and the MySQL containers.
