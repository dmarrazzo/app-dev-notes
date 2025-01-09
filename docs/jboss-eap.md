Install and configure
==============================================================================

Install EAP
------------------------------------------------------------------------------

From [Red Hat Product Downloads](https://access.redhat.com/downloads) site look for **Red Hat JBoss Enterprise Application Platform**:

- Select *Download Latest*
- Select *7.4* version
- In the row *Red Hat JBoss Enterprise Application Platform 7.4.0* select *Download* 
- Select *Patch* tab and download the latest e.g. `jboss-eap-7.4.20-patch.zip`

Uncompress EAP:

```
unzip jboss-eap-7.4.0.zip
```

Upgrade EAP:

- In `<EAP_HOME>\bin`, launch the CLI `./jboss-cli.sh` or `jboss-cli.bat`
- In the CLI, issue the following command: `patch apply <download dir>/jboss-eap-7.4.20-patch.zip`

If you are using JDK 17 you have to enable Elytron:

```sh
bin/jboss-cli.sh --file=docs/examples/enable-elytron-se17.cli
bin/jboss-cli.sh --file=docs/examples/enable-elytron-se17.cli -Dconfig=standalone-full.xml
bin/jboss-cli.sh --file=docs/examples/enable-elytron-se17-domain.cli
```

See [Security Configuration Changes for Java SE 17 Support in Red Hat JBoss EAP](https://access.redhat.com/articles/6956863)

add management user:

    bin/add-user.sh -u admin -p <password>

Run EAP
------------------------------------------------------------------------------

Standalone web profile:

    ./standalone.sh

Standalone full profile:

    ./standalone.sh -c standalone-full.xml

Configure JDBC
------------------------------------------------------------------------------

### Install JDBC drivers

From command line:

    $ EAP_HOME/bin/jboss-cli.sh

Install the module:

    module add --name=MODULE_NAME --resources=PATH_TO_JDBC_JAR --dependencies=DEPENDENCIES

Examples:

    module add --name=org.apache.derby --resources=~/apps/db-derby-10.14.2.0-bin/lib/derby.jar --dependencies=javax.api,javax.transaction.api

Configure JDBC driver:

    /subsystem=datasources/jdbc-driver=derby:add(driver-name=derby,driver-module-name=org.apache.derby)

See [eap datasource](https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.0/html/configuration_guide/datasource_management)

### Oracle XA Datasource

1. Grant the permissions

    This is the EAP doc (it refer to a XA thin driver!):
    
    [Datasource management: oracle xa datasource](https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.0/html/configuration_guide/datasource_management#example_oracle_xa_datasource)

        GRANT SELECT ON sys.dba_pending_transactions TO user;
        GRANT SELECT ON sys.pending_trans$ TO user;
        GRANT SELECT ON sys.dba_2pc_pending TO user;
        GRANT EXECUTE ON sys.dbms_xa TO user;

2. Install the module (Using the jboss-cli)

        module add --name=com.oracle --resources=<path>/ojdbc8.jar --dependencies=javax.api,javax.transaction.api


3. Install the driver (Using the jboss-cli)

/subsystem=datasources/jdbc-driver=oracle:add(driver-name=oracle,driver-module-name=com.oracle,driver-xa-datasource-class-name=oracle.jdbc.xa.client.OracleXADataSource,driver-class-name=oracle.jdbc.OracleDriver)

Do it even of the server where you installed through the Dashbuilder console, just to be sure that everything is aligned-

4. Configure the XA data source (from the Admin Console)

    URL:

        jdbc:oracle:thin:@oracleHostName:1521:serviceName
    
    Other option:
    
        url="jdbc:oracle:thin:@(DESCRIPTION=
        (LOAD_BALANCE=on)
        (ADDRESS_LIST=
         (ADDRESS=(PROTOCOL=TCP)(HOST=host1) (PORT=1521))
         (ADDRESS=(PROTOCOL=TCP)(HOST=host2)(PORT=1521)))
         (CONNECT_DATA=(SERVICE_NAME=service_name)))"

    Datasource properties (for OpenShift image):

        DATASOURCES=ORACLE
        ORACLE_DATABASE=jbpm
        ORACLE_JNDI=java:jboss/datasources/jbpm
        ORACLE_DRIVER=oracle
        ORACLE_USERNAME=jbpmuser
        ORACLE_PASSWORD=jbpmpass
        ORACLE_TX_ISOLATION=TRANSACTION_READ_UNCOMMITTED
        ORACLE_JTA=true
        ORACLE_SERVICE_HOST=1.2.3.4
        ORACLE_SERVICE_PORT=1521


    
5. RELOAD THE CONFIG
   (for jboss-cli)

        :reload

    Or restart the application server

### PostgreSQL

module add --name=org.postgresql --resources=/home/donato/apps/jdbc/postgresql-42.2.9.jar --dependencies=javax.api,javax.transaction.api


```xml
            <datasource jndi-name="java:jboss/PostgresDS" pool-name="PostgresDS">
               <connection-url>jdbc:postgresql://localhost/test</connection-url>
               <driver>postgresql</driver>
               <security>
                  <user-name>donato</user-name>
                  <password>donato</password>
               </security>
               <validation>
                  <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker" />
                  <validate-on-match>true</validate-on-match>
                  <background-validation>false</background-validation>
                  <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter" />
               </validation>
            </datasource>
            <drivers>
               <driver name="postgresql" module="org.postgresql">
                  <xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>
               </driver>
            </drivers>
```

### MySql


```xml
            <xa-datasource jndi-name="java:jboss/MySqlXADS" pool-name="MySqlXADS">
              <xa-datasource-property name="ServerName">
                mysqlhost
              </xa-datasource-property>
              <xa-datasource-property name="DatabaseName">
                rhpamdb
              </xa-datasource-property>
              <driver>mysql</driver>
              <security>
                <user-name>dbadmin</user-name>
                <password>Passw0rd!</password>
              </security>
              <validation>
                <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLValidConnectionChecker"/>
                <validate-on-match>true</validate-on-match>
                <background-validation>false</background-validation>
                <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLExceptionSorter"/>
              </validation>
            </xa-datasource>
           <drivers>
               <driver name="mysql" module="com.mysql">
                 <driver-class>com.mysql.jdbc.Driver</driver-class>
                 <xa-datasource-class>com.mysql.cj.jdbc.MysqlXADataSource</xa-datasource-class>
               </driver>
            </drivers>
```

Grant XA priviledge:

```sql
GRANT XA_RECOVER_ADMIN ON *.* TO 'username'@'%';
FLUSH PRIVILEGES;
```
