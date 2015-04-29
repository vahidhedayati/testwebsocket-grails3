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