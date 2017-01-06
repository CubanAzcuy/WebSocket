package bluefletch.myapplication.webrtc.idea;

import android.content.Context;
import android.opengl.GLSurfaceView;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import bluefletch.myapplication.webrtc.utils.CameraUtil;

/**
 * Created by robertgross on 1/4/17.
 */

public class WebRTCClient {

    public static final String VIDEO_TRACK_ID = "videoLC";
    public static final String AUDIO_TRACK_ID = "audioLC";
    public static final String LOCAL_MEDIA_STREAM_ID = "localStreamLC";
    private final PeerConnectionFactory pcFactory;

    private VideoSource localVideoSource;
    private GLSurfaceView localView;
    private GLSurfaceView remoteView;
    private MediaStream localMediaStream;

    public WebRTCClient(Context context, GLSurfaceView localView, GLSurfaceView remoteView) {
        PeerConnectionFactory.initializeAndroidGlobals(
                context,  // Context
                true,  // Audio Enabled
                true,  // Video Enabled
                true); // Hardware Acceleration Enabled

        pcFactory = new PeerConnectionFactory();

        this.localView = localView;
       // this.remoteView = remoteView;

        try {
            getUserMediaSuccess(pcFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getUserMediaSuccess(PeerConnectionFactory pcFactory) throws Exception {

        // Creates a VideoCapturerAndroid instance for the device name
        VideoCapturer capturer = VideoCapturerAndroid.create(CameraUtil.getNameOfFrontFacingDevice());

        // First create a Video Source, then we can make a Video Track
        localVideoSource = pcFactory.createVideoSource(capturer, SignalingParams.defaultVideoConstraints());
        VideoTrack localVideoTrack = pcFactory.createVideoTrack(VIDEO_TRACK_ID, localVideoSource);


        // Then we set that view, and pass a Runnable to run once the surface is ready
        VideoRendererGui.setView(localView, null);

        // Now that VideoRendererGui is ready, we can get our VideoRenderer.
        // IN THIS ORDER. Effects which is on top or bottom
        int x = 0; int y = 0;
       // VideoRenderer remoteRenderer = VideoRendererGui.createGui(x, y, localView.getWidth(), localView.getHeight(), RendererCommon.ScalingType.SCALE_ASPECT_BALANCED, false);
        VideoRenderer localRenderer = VideoRendererGui.createGui(x, y, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_BALANCED, true);

        // And finally, with our VideoRenderer ready, we
        // can add our renderer to the VideoTrack.
        localVideoTrack.addRenderer(localRenderer);

//        // We start out with an empty MediaStream object, created with help from our PeerConnectionFactory
//        //  Note that LOCAL_MEDIA_STREAM_ID can be any string
        localMediaStream = pcFactory.createLocalMediaStream(LOCAL_MEDIA_STREAM_ID);

        // Now we can add our tracks.
        localMediaStream.addTrack(localVideoTrack);
    }

    public void setupAudio(PeerConnectionFactory pcFactory) {
        // First we create an AudioSource then we can create our AudioTrack
        AudioSource audioSource = pcFactory.createAudioSource(SignalingParams.defaultAudioConstraints());
        AudioTrack localAudioTrack = pcFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);

    }


//    public void start(boolean isCaller) {
//
//        PeerConnection peerConnection = pcFactory.createPeerConnection(
//                iceServers,
//                constraints,
//                observer);
//
//        peerConnection = new RTCPeerConnection(peerConnectionConfig);
//        peerConnection.onicecandidate = gotIceCandidate;
//        peerConnection.onaddstream = gotRemoteStream;
//        peerConnection.addStream(localStream);
//
//        if(isCaller) {
//            peerConnection.createOffer().then(createdDescription).catch(errorHandler);
//        }
//    }
//
//    function gotMessageFromServer(message) {
//        if(!peerConnection) start(false);
//
//        var signal = JSON.parse(message.data);
//
//        // Ignore messages from ourself
//        if(signal.uuid == uuid) return;
//
//        if(signal.sdp) {
//            peerConnection.setRemoteDescription(new RTCSessionDescription(signal.sdp)).then(function() {
//                // Only create answers in response to offers
//                if(signal.sdp.type == 'offer') {
//                    peerConnection.createAnswer().then(createdDescription).catch(errorHandler);
//                }
//            }).catch(errorHandler);
//        } else if(signal.ice) {
//            peerConnection.addIceCandidate(new RTCIceCandidate(signal.ice)).catch(errorHandler);
//        }
//    }
//
//    function gotIceCandidate(event) {
//        if(event.candidate != null) {
//            serverConnection.send(JSON.stringify({'ice': event.candidate, 'uuid': uuid}));
//        }
//    }
//
//    function createdDescription(description) {
//        console.log('got description');
//
//        peerConnection.setLocalDescription(description).then(function() {
//            serverConnection.send(JSON.stringify({'sdp': peerConnection.localDescription, 'uuid': uuid}));
//        }).catch(errorHandler);
//    }
//
//    function gotRemoteStream(event) {
//        console.log('got remote stream');
//        remoteVideo.src = window.URL.createObjectURL(event.stream);
//    }
//
//    function errorHandler(error) {
//        console.log(error);
//    }
//
//    // Taken from http://stackoverflow.com/a/105074/515584
//// Strictly speaking, it's not a real UUID, but it gets the job done here
//    function uuid() {
//        function s4() {
//            return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
//        }
//
//        return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
//    }
}
