package com.rpa.camel.processors.common;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

@Component
public class Jax2MarshallerGenerater {

	public static Jaxb2Marshaller getJax2MarshallerObject(){
		 Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		 return marshaller;
	}
}
