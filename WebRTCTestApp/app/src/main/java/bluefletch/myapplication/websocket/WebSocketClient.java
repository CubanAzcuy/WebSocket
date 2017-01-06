package bluefletch.myapplication.websocket;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import de.tavendo.autobahn.WebSocket;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;

/**
 * Created by robertgross on 12/28/16.
 */

public class WebSocketClient {
//    private final ThreadStarter mThreadStarter;
//    private WebSocketObserver mObserver;
//
//
//    public WebSocketClient(iWebSocketEvents events) {
//        mThreadStarter = new ThreadStarter("WebSocketClient");
//        mObserver = new WebSocketObserver(events);
//    }

    private final WebSocketConnection mConnection = new WebSocketConnection();
    private final String host;
    private HashMap<String, Emitter> commandMap;

    public WebSocketClient(String hostName) {
        host = hostName;
        commandMap = new HashMap<>();
    }

    public void connect() {
        try {
            mConnection.connect(host, new WebSocket.ConnectionHandler() {
                @Override
                public void onOpen() {
                    Log.d("WebSocketClient", "onOpen");

                }

                @Override
                public void onClose(int code, String reason) {
                    Log.d("WebSocketClient", "onOpen: " + reason);

                }

                @Override
                public void onTextMessage(String payload) {
                    try {
                        Log.d("WebSocketClient", "onTextMessage: " + payload);
                        JSONObject json = new JSONObject(payload);
                        for(String id: commandMap.keySet()) {
                            if (json.has(id)) {
                                commandMap.get(id).call(json.get(id));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onRawTextMessage(byte[] payload) {

                }

                @Override
                public void onBinaryMessage(byte[] payload) {

                }
            });
        } catch (WebSocketException e) {
            Log.d("STATIC STRING", e.toString());
        }
    }

    public void on(String id, Emitter command) {
        commandMap.put(id, command);
    }

    /**
     * Send a message through the signaling server
     *
     * @param to id of recipient
     * @param type type of message
     * @param payload payload of message
     * @throws JSONException
     */
    public void sendMessage(String to, String type, JSONObject payload) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("to", to);
        message.put("type", type);
        message.put("payload", payload);
        mConnection.sendTextMessage(message.toString());
    }

    public void emit(String readyToStream, JSONObject externalMessage) throws JSONException {
        JSONObject message = new JSONObject();
        message.put(readyToStream, externalMessage);
        mConnection.sendTextMessage(message.toString());
    }

    public void disconnect() {
        mConnection.disconnect();
    }

    public void close() {

    }
}
