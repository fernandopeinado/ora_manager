OraManager
==========

Lightweight Oracle database monitor.

Compatibility
-------------

- __Oracle 10__: Not supported. Though you can probably make it work with a bit
    of creativity.
- __Oracle 11__: Target version.
- __Oracle 12__: Might work.

Works on XE, Standard, Enterprise.

Installation
------------

Just deploy the WAR file on your favorite Web container using Java 8.

### Connection Pools (exposed via JNDI)

#### jdbc/oraman (required)

The connection pool used for monitoring purposes. The Oracle user configured on
this pool must have access to some of the `V$` performance views. A few options:

- `SYSTEM` user
- a user with the  `SELECT ANY DICTIONARY` privilege
- a user with `SELECT` privileges on the necessary `V$` views (see _Advanced_ 
  below)

You __MUST__ add the `v$session.program=OraManager` JDBC connection property
when you configure this pool. If you don't, the monitor will end up monitoring
... itself; surely not what you want.

#### jdbc/oramanAdmin (optional)

Configure this one if you want a big red button to kill user sessions.

Sessions are killed using the `ALTER SYSTEM KILL SESSION` command, so the
Oracle user on this pool must have the `ALTER SYSTEM` privilege.

### Java Heap

In most cases, `-Xmx128m` should be enough (up to ~100 concurrent active
sessions, maybe more).

### Authentication

Basic authentication in the `web.xml` file. Set up a user with the `oraman`
role and you're good to go.

### Tomcat Configuration Example

#### server.xml

```
<Resource name="oraman" auth="Container" type="javax.sql.DataSource"
    driverClassName="oracle.jdbc.OracleDriver"
    url="jdbc:oracle:thin:@localhost:1521:xe" username="oraman" password="pass"
    connectionProperties="v$session.program=OraManager" />
```

#### context.xml

```
<ResourceLink name="jdbc/oraman" global="oraman" type="javax.sql.DataSource"/>
<ResourceLink name="jdbc/oramanAdmin" global="oraman" type="javax.sql.DataSource"/>
```

#### tomcat-users.xml

```
<role rolename="oraman"/>
<user username="admin" password="admin" roles="oraman"/>
```

Advanced
--------

TODO
