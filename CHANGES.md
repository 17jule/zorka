Zorka 0.8 (2013-01-20)
---------------------

 * (TBD) improve test coverage (both unit tests, stress tests and benchmarks);
 * (TBD) finish ranking subsystem;
 * (TBD) 

Zorka 0.7 (2013-01-06)
----------------------

 * refactor spy API to use string keys (instead of convoluted stage-slot convention);
 * get rid od ON_COLLECT stage, use asynchronous queue collector;
 * refactor logger to act as aggregator of (attachable) trappers, remove loggger <-> file trapper redundancies;
 * lots of redundant code eliminated (eg. threading code in trappers, rank listers, agent integrations etc.)
 * lots of cleanups, javadocs, bugfixes, simplify package structure and limit inter-package dependencies;
 * performance optimization of ZorkaStatsCollector (and removal of JmxAttrCollector);
 * rudimentary stress testing / microbenchmarking framework implemented;
 * spy now supports OSGi-based frameworks (eg. WSO2 Carbon, see sample script on how to configure it);
 * remove custom pool executors, use standard ThreadPoolExecutor instead;
 * support for matching classes and methods by annotations;


Zorka 0.6 (2012-12-22)
----------------------

 * normalization of xQL queries (all major query languages);
 * normalization of LDAP search queries;
 * file trapper (logs events to files instead of syslog/zabbix/SNMP);
 * composite processing chains and comparator filters;
 * zorka API overhaul (yet more refactoring are on the way);


Zorka 0.5 (2012-12-07)
----------------------

 * syslog trapper support;
 * snmp trapper support (traps and value get interface);
 * zabbix trapper;
 * config scripts and zabbix templates for JBoss 7;
 * documentation: converted to `md` format; more interesting examples (eg. CAS auditing);


Zorka 0.4 (2012-11-26)
----------------------

 * documentation updates, cleanups and fixes (as usual);
 * nagios NRPE protocol support;
 * thread rank ported to new ranking framework;
 * new - circular buffer aggregate;


Zorka 0.3 (2012-11-10)
----------------------

 * basic ranking framework (working with Zorka MethodCallStats);
 * implement missing collectors of ZorkaSpy;
 * implement missing processors in ZorkaSpy;
 * support for IBM JDK and JRockit;
 * documentation updates, cleanups and fixes;


Zorka 0.2 (2012-11-04)
---------

 * new ZorkaSpy instrumentation engine (incomplete, yet functional);
 * get rid of lib/*.jar files, embed them into agent.jar;
 * get rid of j2ee dependencies (use reflection instead);
 * remove zorka5.sar module and jboss dependencies (use instrumentation instead);
 * documentation updates, many little cleanups and fixes;


Zorka 0.1 (2012-09-19)
----------------------

 * initial release;
 * zabbix integration;
 * basic functions for accessing JMX data;
 * rudimentary instrumentation engine (ZorkaSpy);