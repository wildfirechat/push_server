package cn.wildfirechat.push.admin;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DualPortConfig {

    @Value("${admin.server.port:8086}")
    private int adminPort;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer() {
        return factory -> {
            Connector adminConnector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            adminConnector.setPort(adminPort);
            factory.addAdditionalTomcatConnectors(adminConnector);
        };
    }
}
