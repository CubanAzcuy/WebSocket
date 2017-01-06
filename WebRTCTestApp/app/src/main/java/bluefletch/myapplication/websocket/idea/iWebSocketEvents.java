package bluefletch.myapplication.websocket.idea;

/**
 * Created by robertgross on 12/28/16.
 */

public interface iWebSocketEvents {
    void onWebSocketMessage(final WebSocketObserver.WebSocketConnectionState state, final String msg);
    void onWebSocketClose();
    void onWebSocketError(final String description);
    void onOpen();
}
