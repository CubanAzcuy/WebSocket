package bluefletch.myapplication.webrtc;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import bluefletch.myapplication.webrtc.interfaces.iRtcListener;
import bluefletch.myapplication.websocket.WebSocketClient;

import static android.content.ContentValues.TAG;

/**
 * Created by robertgross on 1/5/17.
 */

public class Peer implements SdpObserver, PeerConnection.Observer{
    public PeerConnection pc;
    private String id;
    private int endPoint;


    //Should be weak refs/ should be interfaces
    WebRTCContainer mContainer; //<- GetLocalMS, addPeer, getPeers, getEndpoints
    iRtcListener mListener;
    WebSocketClient mClient;

    public Peer(WebRTCContainer container, iRtcListener listener, WebSocketClient client, PeerConnectionFactory factory, String id, int endPoint) {

        Log.d(TAG,"new Peer: "+id + " " + endPoint);
        mContainer = container;
        mListener = listener;
        mClient = client;

        this.pc = factory.createPeerConnection(mContainer.getICEServers(), mContainer.getPCConstraints(), this);
        this.id = id;
        this.endPoint = endPoint;

        pc.addStream(mContainer.getLocalMS());

        mListener.onStatusChanged("CONNECTING");
    }

    @Override
    public void onCreateSuccess(final SessionDescription sdp) {
        // TODO: modify sdp to use pcParams prefered codecs
        try {
            JSONObject payload = new JSONObject();
            payload.put("type", sdp.type.canonicalForm());
            payload.put("sdp", sdp.description);
            mClient.sendMessage(id, sdp.type.canonicalForm(), payload);
            pc.setLocalDescription(Peer.this, sdp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSetSuccess() {}

    @Override
    public void onCreateFailure(String s) {}

    @Override
    public void onSetFailure(String s) {}

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {}

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        if(iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
            mListener.onStatusChanged("DISCONNECTED");
            mContainer.removePeer(id);
        }
    }
    @Override
    public void onIceConnectionReceivingChange(boolean b) {

    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("label", candidate.sdpMLineIndex);
            payload.put("id", candidate.sdpMid);
            payload.put("candidate", candidate.sdp);
            mClient.sendMessage(id, "candidate", payload);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(TAG,"onAddStream "+mediaStream.label());
        // remote streams are displayed from 1 to MAX_PEER (0 is localStream)
        mListener.onAddRemoteStream(mediaStream, endPoint+1);
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(TAG,"onRemoveStream "+mediaStream.label());
        mContainer.removePeer(id);
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {}

    @Override
    public void onRenegotiationNeeded() {

    }

    public int getEndPoint() {
        return endPoint;
    }

    public String getId() {
        return id;
    }
}

