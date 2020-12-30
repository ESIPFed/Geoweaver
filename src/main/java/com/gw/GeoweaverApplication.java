package com.gw;

import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class GeoweaverApplication {

	public static void main(String[] args) {
		
//		BasicConfigurator.configure();
		
		SpringApplication.run(GeoweaverApplication.class, args);
		
	}
	
//	@Bean
//    public ServletContextAware endpointExporterInitializer(final ApplicationContext applicationContext) {
//		
//        return new ServletContextAware() {
//        	
//            @Override
//            public void setServletContext(ServletContext servletContext) {
//                ServerEndpointExporter exporter = new ServerEndpointExporter();
//                exporter.setApplicationContext(applicationContext);
//                exporter.afterPropertiesSet();
//            }
//            
//        };
//    }

}
