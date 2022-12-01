/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.rpa.configuration.ControlTower;
import com.rpa.util.UtilityFile;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@ComponentScan({"com.rpa"})
@EnableJpaRepositories("com.rpa.repository")
@SpringBootConfiguration
@EnableAutoConfiguration
public class Application {

	@Autowired
	ServletContext servletContext;

	public static void main(String[] args) throws Exception {

		String jarBasePath = UtilityFile.getCodeBasePath();
		
		/** VM ARGUMENTS **/
		System.setProperty("hawtio.authenticationEnabled", "true");
    	System.setProperty("hawtio.realm", "hawtio");
    	System.setProperty("hawtio.roles", "admin,viewer");
    	System.setProperty("hawtio.rolePrincipalClasses", "org.keycloak.adapters.jaas.RolePrincipal");
    	System.setProperty("hawtio.keycloakEnabled", "true");
    	System.setProperty("hawtio.keycloakClientConfig", "file:////" + jarBasePath +"/rpa_properties/json/keycloak-hawtio-client.json");
    	System.setProperty("hawtio.keycloakServerConfig", jarBasePath +"/rpa_properties/json/keycloak-hawtio.json");
    	System.setProperty("java.security.auth.login.config", "file:////" + jarBasePath +"/rpa_properties/conf/login.conf");

		//ControlTower controlTower = new ControlTower();
		//controlTower.configureHawtio();
		
		SpringApplication.run(Application.class, args);
	}
}

