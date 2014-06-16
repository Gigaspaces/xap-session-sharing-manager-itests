GigaSpaces XAP HTTP Session Sharing
============================================

Directory Structure
-------------------
1. data-model - pojos for system tests usage only
2. web - web application for system tests and demo usage
3. sys-tests - system tests

Prerquest
---------
1. JMeter installation
2. JBoss installation
3. Tomcat installation
4. Jetty installation
5. Gigaspaces XAP installation

Configuration
-------------
All system tests configurations files are located in: src/test/resources/config.
edit config.properties:

* JBOSS_HOME - jboss installation directory
* project.basedir - path/to/http-session-itests
* TOMCAT_HOME - tomcat installation directory
* JMETER_HOME - jmeter installation directory
* GS_HOME - Gigaspaces installtion directory
* JETTY_HOME - jetty installation directory
* SPACE_URL - space url
* HOST - host ip address

Build
-----

1. navigate to the `cd path/to/http-session-itests`
2. build `mvn package -DskipTests`
3. run tests `mvn test`