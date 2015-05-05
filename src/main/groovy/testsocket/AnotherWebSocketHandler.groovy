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