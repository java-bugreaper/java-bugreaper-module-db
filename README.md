# BUGREAPER: module-db

for interact with Databases

## [apiDOC](https://bug-reaper.gitlab.io/java-bugreaper-module-db/)

### [EXAMPLES.md](EXAMPLES.md)

### Requirements:

    JAVA >= 17
    Allure server >= 2.15

### IMPORTANT add jdbc for your database type(not included in helper):

    testImplementation 'org.mariadb.jdbc:mariadb-java-client:2.7.12'
    testImplementation "com.mysql:mysql-connector-j:8.2.0"
    testImplementation 'org.postgresql:postgresql:42.7.1'
    testImplementation 'com.oracle.database.jdbc:ojdbc11:23.2.0.0'
    testImplementation 'com.microsoft.sqlserver:mssql-jdbc:12.4.2.jre11'

### Logging:

    <logger name="bugreaper-module-db" level="INFO"/>

### Tested with:
    
    mcr.microsoft.com/mssql/server:2025-RC1-ubuntu-24.04
    mariadb:10.10
    mysql:8.0.21
    postgres:14.1
    gvenzl/oracle-free:23.9-slim

## Real examples here:
- ### [Report-db](https://bug-reaper.gitlab.io/java-bugreaper-sandbox/#behaviors)
- ### [Tests](https://gitlab.com/bug-reaper/java-bugreaper-sandbox/-/tree/main/java-test-db)

### Dependencies
| Lib                                                                                   | Version  | Description       |
|---------------------------------------------------------------------------------------|----------|-------------------|
| [bugreaper-core](https://mvnrepository.com/artifact/io.gitlab.ambu550/bugreaper-core) | 0.0.8-rc | bugreaper CORE    |

