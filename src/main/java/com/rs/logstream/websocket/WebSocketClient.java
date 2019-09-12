package com.rs.logstream.websocket;

import com.google.common.io.CharStreams;
import com.rs.logstream.Application;
import com.rs.logstream.utils.JsonHelperUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

@ClientEndpoint
public class WebSocketClient {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClient.class);
    protected WebSocketContainer container;
    protected Session userSession = null;

    public WebSocketClient() {
        container = ContainerProvider.getWebSocketContainer();
        //container.setDefaultMaxBinaryMessageBufferSize(32000);

    }

    public void connect(String sServer) {
        try {
            userSession = container.connectToServer(this, new URI(sServer));
            /*
                The average size of incoming data is typically between 1000 and 5000
                However in some cases it can be up to 12000...
                If the session has to reconnect the data buffer is lost so this number is set large enough to
                handle even the edge cases.
             */
            userSession.setMaxBinaryMessageBufferSize(32768);
            //userSession.setMaxBinaryMessageBufferSize(50);

        } catch (DeploymentException | URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public void reconnect(int newBufferSize, String sServer) {
        try {
            userSession = container.connectToServer(this, new URI(sServer));
            userSession.setMaxBinaryMessageBufferSize(newBufferSize);
            logger.info("the session has restarted new buffersize={} ", newBufferSize);

        } catch (DeploymentException | URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }


    @OnOpen
    public void onOpen(Session session) {
        logger.info("Connected to mashery log stream");
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.error("the session has closed CloseReason={} " + closeReason);
        if (closeReason.getCloseCode() == CloseReason.CloseCodes.TOO_BIG) {
            reconnect(session.getMaxBinaryMessageBufferSize() * 2, Application.masherySocketURL);
        }
    }

    @OnMessage
    public void onMessage(Session s, InputStream is) {
        try {

            String socketMessage = CharStreams.toString(new InputStreamReader(is, "UTF-8"));
            JSONObject parsedMessage = JsonHelperUtil.loadJson(socketMessage);
            JSONArray masheryMessageData = JsonHelperUtil.getMasheryDataFromJson(parsedMessage);

            //for each event first check if they should be logged, remove unneeded fields, and then finally log the result
            masheryMessageData.forEach((masheryMessage) -> {
                //Check if the masheryMessage should be logged
                if ((!"200_OK_API".equalsIgnoreCase((String) ((JSONObject) masheryMessage).get("response_string"))) |
                        (!"-".equalsIgnoreCase((String) ((JSONObject) masheryMessage).get("traffic_manager_error_code")))) {
                    JSONObject filteredMasheryMessage = JsonHelperUtil.removeFieldsFromMasheryMessage((JSONObject) masheryMessage);
                    logger.info(filteredMasheryMessage.toJSONString());
                }
            });

        } catch (IOException e) {
            logger.error("Error parsing mashery message event IOException={}", e.getMessage());
        }
    }

    public void Disconnect() throws IOException {
        userSession.close();
    }
}
