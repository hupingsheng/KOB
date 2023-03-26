package com.hps.backend;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@SpringBootApplication
@Slf4j
public class BackendApplication {

    //解决request路径字符异常报错
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                connector.setProperty("relaxedQueryChars", "|{}[]");
            }
        });
        return factory;
    }


    public static void main(String[] args) {
        log.info("=====begin======");
        SpringApplication.run(BackendApplication.class, args);
        log.info("=====successful======");
    }

}
