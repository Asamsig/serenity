# Setup 

## PostgreSQL journal store

- DB schema: https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/test/resources/schema/postgres/postgres-schema.sql
- Environment variables:
  * POSTGRES_HOST
  * POSTGRES_SCHEMA
  * POSTGRES_USER
  * POSTGRES_PASSWORD

# Code style

This project uses scalafmt to format the code

> sbt scalafmt

If you're using IntelliJ IDEA you can use a plugin that formats the
code when it saves the file.


# Architecture

The application uses event sourcing for all the stored entities and embraces the CQRS. The
events are stored in a PostgreSQL database trough akka-persistence. A query view is
updated with its own tables.

## Aggregates 
`TODO`

## API

| Endpoint            | Description      |
|---------------------|------------------|
| /api/ping           | Ping             |
| /api/login          | Login            |
| /api/logout         | Logout           |
| /api/graphql        | GraphQL endpoint |
| /api/render-schema  | GraphQL schema   |
 
# Deploy

## Prod
The application runs on AWS elastic-beanstalk. The requires task to do the deploy is
present in the `deploy.sh` script.

> ./deploy.sh

## Local
Use `sbt run`. Requires an postgreSQL database running locally.

Starting a local database with docker:
> docker run --name serenity_db -e POSTGRES_PASSWORD=serenity -e POSTGRES_USER=serenity -p 5432:5432 -d postgres