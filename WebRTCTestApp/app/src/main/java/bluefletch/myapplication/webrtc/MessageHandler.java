package bluefletch.myapplication.webrtc;

/**
 * Created by robertgross on 1/5/17.
 */

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.util.HashMap;

import bluefletch.myapplication.webrtc.interfaces.iCommand;
import bluefletch.myapplication.webrtc.interfaces.iRtcListener;
import bluefletch.myapplication.websocket.Emitter;
import bluefletch.myapplication.websocket.WebSocketClient;


public class MessageHandler {
    private static final String TAG = "MessageHandler";
    private HashMap<String, iCommand> commandMap;

    //Should be weak refs/ should be interfaces
    WebRTCContainer mContainer; //<- GetLocalMS, addPeer, getPeers, getEndpoints
    iRtcListener mListener;

    public MessageHandler(WebRTCContainer container, iRtcListener listener) {

        mListener = listener;
        mContainer = container;

        this.commandMap = new HashMap<>();
        commandMap.put("init", new CreateOfferCommand());
        commandMap.put("offer", new CreateAnswerCommand());
        commandMap.put("answer", new SetRemoteSDPCommand());
        commandMap.put("candidate", new AddIceCandidateCommand());
    }

    private Emitter onMessage = new Emitter() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                String from = null;

                if(!data.isNull("from")) {
                    from = data.getString("from");
                } else {
                    from = data.getString("to");
                }

                String type = data.getString("type");
                JSONObject payload = null;

                if(!type.equals("init")) {
                    payload = data.getJSONObject("payload");
                }
                // if peer is unknown, try to add him
                if(from == null || !mContainer.getPeers().containsKey(from)) {
                    // if MAX_PEER is reach, ignore the call
                    int endPoint = findEndPoint();
                    if(endPoint != WebRTCContainer.MAX_PEER) {
                        Peer peer = mContainer.addPeer(from, endPoint);
                        peer.pc.addStream(mContainer.getLocalMS());
                        commandMap.get(type).execute(from, payload);
                    }
                } else {
                    commandMap.get(type).execute(from, payload);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
;
    private Emitter onId = new Emitter() {
        @Override
        public void call(Object... args) {
            String id = (String) args[0];
            mListener.onCallReady(id);
        }
    };

    private int findEndPoint() {
        for(int i = 0; i < WebRTCContainer.MAX_PEER; i++) if (!mContainer.getEndPoint(i)) return i;
        return WebRTCContainer.MAX_PEER;
    }

    public void setupListeners(WebSocketClient client) {
        client.on("id", onId);
        client.on("message", onMessage);
    }


    //region Commands
    private class CreateOfferCommand implements iCommand {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG,"CreateOfferICommand");
            Peer peer = mContainer.getPeers().get(peerId);
            peer.pc.createOffer(peer, mContainer.getPCConstraints());
        }
    }

    private class CreateAnswerCommand implements iCommand {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG,"CreateAnswerICommand");
            Peer peer = mContainer.getPeers().get(peerId);
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);
            peer.pc.createAnswer(peer, mContainer.getPCConstraints());
        }
    }

    private class SetRemoteSDPCommand implements iCommand {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG,"SetRemoteSDPICommand");
            Peer peer = mContainer.getPeers().get(peerId);
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);
        }
    }

    private class AddIceCandidateCommand implements iCommand {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG,"AddIceCandidateICommand");
            PeerConnection pc = mContainer.getPeers().get(peerId).pc;
            if (pc.getRemoteDescription() != null) {
                IceCandidate candidate = new IceCandidate(
                        payload.getString("id"),
                        payload.getInt("label"),
                        payload.getString("candidate")
                );
                pc.addIceCandidate(candidate);
            }
        }
    }
    //endregion
}

