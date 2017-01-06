package bluefletch.myapplication.webrtc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import org.webrtc.*;

import bluefletch.myapplication.DeviceUuidFactory;
import bluefletch.myapplication.webrtc.interfaces.iRtcListener;
import bluefletch.myapplication.webrtc.utils.CameraUtil;
import bluefletch.myapplication.webrtc.utils.PeerConnectionParameters;
import bluefletch.myapplication.websocket.WebSocketClient;

public class WebRTCContainer {
    private final static String TAG = WebRTCContainer.class.getCanonicalName();
    public final static int MAX_PEER = 2;

    private final UUID uuid;
    private boolean[] endPoints = new boolean[MAX_PEER];
    private HashMap<String, Peer> peers = new HashMap<>();

    private PeerConnectionFactory factory;
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    private PeerConnectionParameters pcParams;
    private MediaConstraints pcConstraints = new MediaConstraints();
    private MediaStream localMS;
    private VideoSource videoSource;
    private iRtcListener mListener;
    private WebSocketClient client;
    private MediaConstraints mVideoConstraints;

    public WebRTCContainer(Context context, iRtcListener listener, String host, PeerConnectionParameters params) {
        mListener = listener;
        pcParams = params;
        PeerConnectionFactory.initializeAndroidGlobals(
                listener,  // Context
                true,  // Audio Enabled
                true,  // Video Enabled
                true); // Hardware Acceleration Enabled

        factory = new PeerConnectionFactory();
        MessageHandler messageHandler = new MessageHandler(this, listener);

        setupSocketStuff(host, messageHandler);

        uuid = new DeviceUuidFactory(context).getDeviceUuid();
        iceServers.add(new PeerConnection.IceServer("stun:23.21.150.121"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
    }

    public void setupSocketStuff(String host, MessageHandler messageHandler) {
        client = new WebSocketClient(host);
        messageHandler.setupListeners(client);
        client.connect();
    }

    public Peer addPeer(String id, int endPoint) {
        Peer peer = new Peer(this, mListener, client, factory, id, endPoint);
        peers.put(id, peer);
        endPoints[endPoint] = true;
        return peer;
    }

    public void removePeer(String id) {
        Peer peer = peers.get(id);
        mListener.onRemoveRemoteStream(peer.getEndPoint());
        peer.pc.close();
        peers.remove(peer.getId());
        endPoints[peer.getEndPoint()] = false;
    }


    //Fixme add this in later
    //region Activity LifeCycle Methods
    /**
     * Call this method in Activity.onPause()
     */
    public void onPause() {
        if(videoSource != null) videoSource.stop();
    }

    /**
     * Call this method in Activity.onResume()
     */
    public void onResume() {
        if(videoSource != null) videoSource.restart();
    }

    /**
     * Call this method in Activity.onDestroy()
     */
    public void onDestroy() {
        for (Peer peer : peers.values()) {
            peer.pc.dispose();
        }
        videoSource.dispose();
        factory.dispose();
        client.disconnect();
        client.close();
    }
    //endregion

    
    //// FIXME: This should be moved to a camera class
    /**
     * Start the client.
     *
     * Set up the local stream and notify the signaling server.
     * Call this method after onCallReady.
     *
     * @param name client name
     */
    public void start(String name){
        setCamera();
        try {
            JSONObject message = new JSONObject();
            message.put("name", name);
            client.emit("readyToStream", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setCamera(){
        localMS = factory.createLocalMediaStream("ARDAMS");
        if(pcParams.videoCallEnabled){
            mVideoConstraints = new MediaConstraints();
            mVideoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(pcParams.videoHeight)));
            mVideoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(pcParams.videoWidth)));
            mVideoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(pcParams.videoFps)));
            mVideoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(pcParams.videoFps)));

            videoSource = factory.createVideoSource(getVideoCapturer(), mVideoConstraints);
            localMS.addTrack(factory.createVideoTrack("ARDAMSv0", videoSource));
        }

        AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
        localMS.addTrack(factory.createAudioTrack("ARDAMSa0", audioSource));

        mListener.onLocalStream(localMS);
    }

    /*
    Logic I added to create init
    it tells you to invoke the get URL thing and insert in to this server :-(
    https://github.com/pchab/ProjectRTC
    */

    public void createPeerConnection() throws JSONException {
        JSONObject payload = new JSONObject();
        client.sendMessage(uuid.toString(), "init", null);
    }


    public void sendMessage(String callerId, String type, JSONObject payload) throws JSONException {
        client.sendMessage(callerId, type, payload);
    }


    //region MessageHandler Interface
    private VideoCapturer getVideoCapturer() {
        String frontCameraDeviceName = CameraUtil.getNameOfFrontFacingDevice();
        return VideoCapturerAndroid.create(frontCameraDeviceName);
    }

    public Map<String, Peer>  getPeers() {
        return peers;
    }

    public MediaStream getLocalMS() {
        return localMS;
    }

    public boolean getEndPoint(int i) {
        return endPoints[i];
    }

    public MediaConstraints getPCConstraints() {
        return pcConstraints;
    }

    public List<PeerConnection.IceServer> getICEServers() {
        return iceServers;
    }
    //endregion
}