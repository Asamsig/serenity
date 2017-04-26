# Setup 

## Postgresql journal store

- DB schema: https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/test/resources/schema/postgres/postgres-schema.sql
- Environment variables:
  * POSTGRES_HOST
  * POSTGRES_SCHEMA
  * POSTGRES_USER
  * POSTGRES_PASSWORD

# Code style

This project uses scalafmt to format the code

> sbt scalafmt

If your using Intellij IDEA you can use a plugin that format the
code when it saves the file.