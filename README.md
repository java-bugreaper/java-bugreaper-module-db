# BUGREAPER: module-db

for interaction with Databases

## [apiDOC](https://bug-reaper.gitlab.io/java-bugreaper-module-db/)
## [Wiki](https://gitlab.com/bug-reaper/java-bugreaper-module-db/-/wikis/home)

### [EXAMPLES.md](https://gitlab.com/bug-reaper/java-bugreaper-module-db/-/wikis/Examples)

### Requirements:

    JAVA >= 17
    Allure server >= 2.15

### IMPORTANT add jdbc for your database type(not included in helper):

    implementation 'org.mariadb.jdbc:mariadb-java-client:2.7.12'

    implementation "com.mysql:mysql-connector-j:8.2.0"
    implementation "mysql:mysql-connector-java:8.0.33"

    implementation 'org.postgresql:postgresql:42.7.1'

    implementation 'com.oracle.database.jdbc:ojdbc11:23.2.0.0'

    implementation 'com.microsoft.sqlserver:mssql-jdbc:12.4.2.jre11'

### Logging:

    <logger name="bugreaper-module-db" level="INFO"/>

### Tested with:
    
    mcr.microsoft.com/mssql/server:2025-RC1-ubuntu-24.04
    mariadb:10.10
    mysql:8.0.21
    postgres:14.1
    gvenzl/oracle-xe:21-slim

## Real examples here:
- ### [Report-db](https://bug-reaper.gitlab.io/java-bugreaper-sandbox/#behaviors)
- ### [Tests](https://gitlab.com/bug-reaper/java-bugreaper-sandbox/-/tree/main/java-test-db)


