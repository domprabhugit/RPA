/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */
 
1. Build project with mvn package command to generate RPA-spring-boot.war.

2. Run the RPA application with the command mvn spring-boot:run with the following VM arguments.

-Dhawtio.authenticationEnabled=true -Dhawtio.realm=hawtio -Dhawtio.keycloakEnabled=true -Dhawtio.roles=admin,viewer -Dhawtio.rolePrincipalClasses=org.keycloak.adapters.jaas.RolePrincipal
-Dhawtio.keycloakClientConfig=file:///D:/RPA/Deployment/package/rpa_properties/json/keycloak-hawtio-client.json -Dhawtio.keycloakServerConfig=D:/RPA/Deployment/package/rpa_properties/json/keycloak-hawtio.json -Djava.security.auth.login.config=file:///D:/RPA/Deployment/package/rpa_properties/conf/login.conf

3. Keycloak 

Goto the bin directory of Keycloak and execute the following command.

standalone -Djboss.http.port=9091 -Dkeycloak.import=D:\RPA\RPA_WEB\demorealm.json

4. To run the application as a standalone app, rename RPA-spring-boot.war as RPA.war, and in the command prompt, type "java -jar RPA.war"



