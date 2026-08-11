# BUGREAPER: module-db

https://bugreaper.net/

<a href="https://bugreaper.net/db">
  <img src="https://gitlab.com/uploads/-/system/project/avatar/75551143/bugreaper-db_192.png" alt="Project Logo" width="192" />
</a>

[![GitLab Build Status](https://gitlab.com/bug-reaper/java-bugreaper-module-db/badges/main/pipeline.svg)](https://gitlab.com/bug-reaper/java-bugreaper-module-db/-/pipelines?scope=all&ref=main)
[![GitLab Test Coverage](https://gitlab.com/bug-reaper/java-bugreaper-module-db/badges/main/coverage.svg?job=)](https://gitlab.com/bug-reaper/java-bugreaper-module-db)
[![License](https://img.shields.io/gitlab/license/bug-reaper/java-bugreaper-module-db?style=flat-square&color=green)](https://gitlab.com/bug-reaper/java-bugreaper-module-db/-/blob/main/LICENSE)

[![GitLab Latest Release](https://gitlab.com/bug-reaper/java-bugreaper-module-db/-/badges/release.svg)](https://gitlab.com/bug-reaper/java-bugreaper-module-db/-/releases)
[![Maven Latest Release](https://img.shields.io/maven-central/v/net.bugreaper/module-db)](https://mvnrepository.com/artifact/net.bugreaper/module-db)


Is part of the BugReaper JAVA test helper framework that simplifies SQL-databases interaction in automated tests.
It provides easy configuration, convenient APIs for working with SQL-database data, and built-in assertions,
allowing you to write readable and maintainable tests with minimal boilerplate.

## [apiDOC](https://bug-reaper.gitlab.io/java-bugreaper-module-db/)
## [Wiki](https://gitlab.com/bug-reaper/java-bugreaper-module-db/-/wikis/home)

### [EXAMPLES.md](https://gitlab.com/bug-reaper/java-bugreaper-module-db/-/wikis/Examples)

### Requirements:

    JAVA >= 17

### IMPORTANT! Add the JDBC dependency for the database type you are using (not included in the helper).
#### for example:


    implementation 'org.mariadb.jdbc:mariadb-java-client:2.7.12'

    implementation "com.mysql:mysql-connector-j:8.2.0"
    implementation "mysql:mysql-connector-java:8.0.33"

    implementation 'org.postgresql:postgresql:42.7.1'

    implementation 'com.oracle.database.jdbc:ojdbc11:23.2.0.0'

    implementation 'com.microsoft.sqlserver:mssql-jdbc:12.4.2.jre11'

### Logging:

    <logger name="bugreaper-module-db" level="INFO"/>

### Tested with:
    
    mcr.microsoft.com/mssql/server:2022-CU26-ubuntu-22.04
    mcr.microsoft.com/mssql/server:2025-CU7-ubuntu-22.04

    mariadb:10.11.18
    mariadb:12.3.2

    mysql:8.4.11
    mysql:26.7.0

    postgres:14.23
    postgres:18.4

    gvenzl/oracle-xe:21-slim-faststart
    gvenzl/oracle-free:23.26.2-slim-faststart

    clickhouse/clickhouse-server:25.8
    clickhouse/clickhouse-server:26.3.17.56

## Real examples here:
- ### [Report-db](https://bug-reaper.gitlab.io/java-bugreaper-sandbox/#behaviors)
- ### [Tests](https://gitlab.com/bug-reaper/java-bugreaper-sandbox/-/tree/main/java-test-db)
