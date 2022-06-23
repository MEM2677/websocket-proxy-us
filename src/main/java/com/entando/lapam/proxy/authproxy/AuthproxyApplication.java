package com.entando.lapam.proxy.authproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class AuthproxyApplication {

	public static void main(String[] args)
	{
		ConfigurableApplicationContext applicationContext =
			SpringApplication.run(AuthproxyApplication.class, args);

//		for (String beanName : applicationContext.getBeanDefinitionNames()) {
//			System.out.println(beanName);
//		}

	}



}
