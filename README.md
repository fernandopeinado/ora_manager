OraManager
==========

Lightweight Oracle database monitor.

Compatibility
-------------

Tested on Oracle 11 and 12 (XE, Standard, Enterprise). Probably works on more
recent versions too.

Build
-----

### Requirements

JDK 8, Maven (3.3+ recommended).

__JDBC Driver__. Due to licensing restrictions a suitable JDBC driver must be
downloaded manually from Oracle's site. Rename the downloaded file to
`ojdbc-any.jar` and move it to the `ojdbc-repo/com/oracle/jdbc/ojdbc/any`
directory of the project.

### Maven Execution

Run from the root directory of the project:

```bash
mvn package -DskipTests=true
```

A file named `oraman-$version.tgz` will be created in the `target` directory.

Installation
------------

### Requirements

An Oracle user with access to some of the performance and data dictionary views
(`DBA_*`, `V$*`) is required. Some options:

- `SYSTEM` user
- a user with the `SELECT ANY DICTIONARY` privilege
- a user with `SELECT` privilege on the necessary views (a list of objects is
  printed to the log of the application during the startup)

If the Oracle user has the `ALTER SYSTEM` privilege a button to kill user
sessions will be enabled.

### Running

Unpack the `tgz` file (see the "Build" section), adjust the configuration file
and run the `oraman` script.

Foreground

```bash
oraman run
```

Background

```bash
oraman start
oraman stop
```

Source Code Style
-----------------

[Google Java Style][]: [Eclipse][], [IntelliJ][]

[Google Java Style]: https://github.com/google/styleguide/blob/gh-pages/javaguide.html
[Eclipse]: https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml
[IntelliJ]: https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml
