# testwebsocket-grails3

This is a basic Grails 3 application that attempts to use websockets via spring boot.


##### Success : We have websockets working using new WebSocketConfigurer  as well as traditional Default Java Websockets:




There are two index pages, when the application loads up 


http://localhost:8080/test/index2  -->  this is using traditional websocket 

http://localhost:8080/test/index  -->  this is using spring boot WebSocketConfigurer






How it is working:

[grails-app/init/Application.groovy](https://github.com/vahidhedayati/testwebsocket-grails3/blob/master/grails-app/init/testsocket/Application.groovy)

So we have declated /echo to use the new websocket configurator and the ServletListenerRegistrationBean registers the listener for the traditional websockets method

```groovy


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
```


Besides your application.groovy, if you are building a plugin you could do something like this:

In you [https://github.com/vahidhedayati/grails-wschat-plugin/blob/master/src/main/groovy/wschat/WschatGrailsPlugin.groovy)(plugin descriptor) you have something like this:
```groovy
Closure doWithSpring() {
        {->
            wsChatConfig DefaultWsChatConfig
        }
    }
```    
In this plugin I have left both methods of initiating the listener:, this is your [DefaultWsChatConfig.groovy](https://github.com/vahidhedayati/grails-wschat-plugin/blob/master/src/main/groovy/wschat/DefaultWsChatConfig.groovy) inside the same folder as your plugin descriptor (refer to grails-wschat-plugin)

```groovy
@Bean
    public ServletContextInitializer myInitializer() {
        return new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                servletContext.addListener(WsCamEndpoint)
                servletContext.addListener(WsChatFileEndpoint)

            }
        }
    }

    // Alternative way
    //@Bean
    //public ServletListenerRegistrationBean<WsChatEndpoint>  httpSessionEventPublisher() {
     //   return new ServletListenerRegistrationBean<WsChatEndpoint>(new WsChatEndpoint())
    //}
```

In this example I am registering the listeners using the top ServletContextInitializer, since as you can see the limitation of the latter method is that only 1 listener can be registered in either your application.groovy or your CustomConfig.groovy defined in your plugin. The top method allows you to interact with servletContext directly and thus ability to register multiple listeners or any other required task.

With this in place, spring boot now emulates the same as web.xml would when registering a listener. The actual groovy classes that load the websockets from there are as they were i.e. using default websocket calls such as onOpen onMessage etc..


[AnotherWebSocketHandler.groovy](https://github.com/vahidhedayati/testwebsocket-grails3/blob/master/src/main/groovy/testsocket/AnotherWebSocketHandler.groovy) this is our traditional calling method to use default websockets via a spring boot Grails application

```groovy
package testsocket

import grails.util.Environment
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener
import javax.websocket.*
import javax.websocket.server.ServerContainer
import javax.websocket.server.ServerEndpoint

@WebListener
@ServerEndpoint("/hello")
public class AnotherWebSocketHandler implements ServletContextListener {

    private final Logger log = LoggerFactory.getLogger(getClass().name)

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.servletContext
        final ServerContainer serverContainer = servletContext.getAttribute("javax.websocket.server.ServerContainer")
        if (Environment.current == Environment.DEVELOPMENT) {
                serverContainer.addEndpoint(AnotherWebSocketHandler)
        }
        serverContainer.defaultMaxSessionIdleTimeout = 0
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }

    @OnOpen
    public void handleOpen(Session userSession,EndpointConfig c) {
        println "---- WE HAVE OPEN SESSION"
    }

    @OnMessage
    public void handleMessage(String message,Session userSession)  throws IOException {
        println "-- we have  a message ${message}"
        userSession.basicRemote.sendText("Echo back: "+message)
        println "--- Message sent back check console output of browser"
    }

    @OnClose
    public void handeClose(Session userSession)  throws SocketException {
        println "-- Closing session"
    }

    @OnError
    public void handleError(Throwable t) {
        t.printStackTrace()
    }
}
```

The calling page to above endpoint is [index2.gsp](https://github.com/vahidhedayati/testwebsocket-grails3/blob/master/grails-app/views/test/index2.gsp):

```gsp
<html>
<body>
<script type="text/javascript">
  var socket = new WebSocket("ws://localhost:8080/hello");

socket.onopen = function() {
  //event handler when the connection has been established
  socket.send('Hello from index2');
};
socket.onmessage = function(message) {
  //event handler when data has been received from the server
  console.log(message.data);
  //alert(message.data);
};
socket.onclose = function() {
//event handler when the socket has been properly closed
}
socket.onerror = function() {
//event handler when an error has occurred during communication
}
</script>
</body>
</html>

```

-------------------------------------------------------


[AWebSocketHandler.groovy](https://github.com/vahidhedayati/testwebsocket-grails3/blob/master/src/main/groovy/testsocket/AWebSocketHandler.groovy) Is the new spring boot method
```groovy

package testsocket

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import org.springframework.web.socket.*

public class AWebSocketHandler implements WebSocketHandler {

    private final Logger log = LoggerFactory.getLogger(getClass().name)

    private TaskScheduler taskScheduler = new ConcurrentTaskScheduler()

    @Override
    public void afterConnectionEstablished(final WebSocketSession webSocketSession) throws Exception {
        String id = webSocketSession.id
        println "BinaryMessageSizeLimit : " + webSocketSession.binaryMessageSizeLimit
        println "Uri : " + webSocketSession.uri as String
        println "getAcceptedProtocol : "+webSocketSession.acceptedProtocol
        println "TextMessageSizeLimit : " + webSocketSession.textMessageSizeLimit
        println "Id : " + id
        final int num = 0 ;
        taskScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                int t = num+1;
                try {
                    webSocketSession.sendMessage(new TextMessage(t+""));
                }catch (Exception e){
                }
            }
        }, 2000);
    }

    @Override
    void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.debug "Handling $message"
        String mesg = message?.payload
        session.sendMessage(message)
        if (mesg == 'Hello' ) {
            println "--- WE HAVE MESSAGE of ${mesg} | from ${message}"
            TextMessage rmessage = new TextMessage("GOODBYE");
            session.sendMessage(rmessage)
        }else{
            session.sendMessage(message)
        }
    }

    @Override
    void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.debug "Exception $exception"
    }

    @Override
    void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.debug "closed: $closeStatus"
    }

    @Override
    boolean supportsPartialMessages() {
        return false
    }

}
```


This is called by [index.gsp](https://github.com/vahidhedayati/testwebsocket-grails3/blob/master/grails-app/views/index.gsp):

Although sockjs was implemented initially as you can see I reverted this back to use new WebSocket  - so no extra requirements:
```gsp

<html>
<body>

	<script src="https://cdn.jsdelivr.net/sockjs/0.3.4/sockjs.min.js"></script>
	<script type="text/javascript">
//I can now fallback to longpoll and do IE9!!!
//var socket = new SockJS("ws://localhost:8080/echo");
var socket = new WebSocket("ws://localhost:8080/echo");

socket.onopen = function() {
  //event handler when the connection has been established
  socket.send('Hello');
};
socket.onmessage = function(message) {
  //event handler when data has been received from the server
  console.log(message.data);
  //alert(message.data);
};
socket.onclose = function() {
//event handler when the socket has been properly closed
}
socket.onerror = function() {
//event handler when an error has occurred during communication
}


</script>

</body>
</html>

```











## Issues raised + bound to this project:

http://stackoverflow.com/questions/28902928/grails-grails3-dowithwebdescriptor/

https://groups.google.com/forum/#!topic/grails-dev-discuss/yeG_9Gi9K5M



### Resources found to help put all of this together:

https://github.com/spring-projects/spring-boot/tree/master/spring-boot-samples/spring-boot-sample-websocket-tomcat

http://hsilomedus.me/index.php/websockets-in-java/

http://kimrudolph.de/blog/spring-4-websockets-tutorial/

https://github.com/hsilomedus/web-sockets-samples

https://github.com/shangmin1990/websocket

http://stackoverflow.com/questions/25375152/how-to-call-the-websocket-server-to-sends-the-message-to-the-client-in-spring


http://translate.google.co.uk/translate?hl=en&sl=de&u=https://blog.rasc.ch/%3Fp%3D2574&prev=search

https://rmannibucau.wordpress.com/2014/09/23/tomee-embedded-a-spring-boot-air-without-no-ee-features-for-business-code/


https://github.com/spring-projects/spring-boot/issues/2070

http://stackoverflow.com/questions/22544214/spring-boot-and-jsf-primefaces-richfaces


http://stackoverflow.com/questions/24955534/spring-3-5-how-to-add-httpsessioneventpublisher-to-my-boot-configuration



