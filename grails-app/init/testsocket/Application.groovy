package testsocket

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

@Configuration
@EnableWebSocket
class Application extends GrailsAutoConfiguration implements  WebSocketConfigurer {

    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        HttpSessionHandshakeInterceptor interceptor = new HttpSessionHandshakeInterceptor()
        registry.addHandler(aWebSocketHandler(), "/echo").addInterceptors(interceptor)
    }

    @Bean
    public ServletListenerRegistrationBean<AnotherWebSocketHandler> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<AnotherWebSocketHandler>(new AnotherWebSocketHandler());
    }

    @Bean
    public WebSocketHandler aWebSocketHandler() {
        return new PerConnectionWebSocketHandler(AWebSocketHandler.class)
    }


    static void main(String[] args) {
        GrailsApp.run(Application)
    }


}