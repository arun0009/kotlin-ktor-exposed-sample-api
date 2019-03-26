## Kotlin Ktor Exposed Sample API  

[![Build Status](https://api.travis-ci.com/arun0009/kotlin-ktor-exposed-sample-api.svg?branch=master)](https://travis-ci.com/arun0009/kotlin-ktor-exposed-sample-api)

Sample Employee API is written in [Kotlin](https://kotlinlang.org/) and uses [Ktor async framework](https://ktor.io/) and uses [Exposed Kotlin SQL Framework](https://github.com/JetBrains/Exposed)  

Sample Employee API allows basic CRUD operations (**immutable database**) via REST API and JSON as data exchange format.  
 
Sample Employee Api by default uses H2 database but can be changed to Postgres by passing `DB_TYPE=postgres` environment variable. The connection settings can also be passed via environment variables (please check `application.conf` to see what variables can be overridden).  
  
Database tables are migrated at deployment via [Flyway](https://flywaydb.org/getstarted/) and scripts can be added in `db/migrations` folder to add new tables or execute queries like inserting data etc on server startup.  
  
`EmployeeApiTest` runs end to end test to make sure all api endpoints perform as expected.  
  
Logs are written to console and can be redirected using `tee` process. Logging with MDC (UUID) for tracing purpose and 
returning MDC UUID in case of error in API response. 
  
#### Running the application via docker  
`docker-compose up`

#### Metrics

The Metrics feature allows you to configure the Metrics to get useful information about the server and the requests.

Run `jconsole` on command line and choose EmployeeApiServerKt.

![](https://raw.githubusercontent.com/arun0009/kotlin-ktor-exposed-sample-api/master/jconsole.png)

Click on `metrics` folder to get server performance. 

![](https://raw.githubusercontent.com/arun0009/kotlin-ktor-exposed-sample-api/master/metrics.png) 