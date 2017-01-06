package bluefletch.myapplication.webrtc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.widget.Toast;

import org.json.JSONException;
import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import bluefletch.myapplication.webrtc.utils.PeerConnectionParameters;
import bluefletch.myapplication.webrtc.WebRTCContainer;
import bluefletch.myapplication.webrtc.interfaces.iRtcListener;

/**
 * Created by robertgross on 1/5/17.
 */

public class WebRTCClient implements iRtcListener {
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";
    public final static int VIDEO_CALL_SENT = 666;

    final String mSocketAddress;
    Point mDisplaySize;

    private final GLSurfaceView mLocalView;
    private final Context mContext;
    private WebRTCContainer client;

    private String callerId;
    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks remoteRender;

    public WebRTCClient(Context context, String host, GLSurfaceView localView, Point displaySize) {
        mSocketAddress = host;
        mLocalView = localView;
        mDisplaySize = displaySize;

        mLocalView.setPreserveEGLContextOnPause(true);
        mLocalView.setKeepScreenOn(true);
        VideoRendererGui.setView(mLocalView, null);

        remoteRender = VideoRendererGui.create(0, 0, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_BALANCED, false);
        localRender = VideoRendererGui.create(0, 0, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_BALANCED, true);
        mContext = context;

        init();
    }

    private void init() {
        PeerConnectionParameters params = new PeerConnectionParameters(true, false, mDisplaySize.x, mDisplaySize.y, 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);
        client = new WebRTCContainer(mContext, this, mSocketAddress, params);
    }

    public void createPeerConnection() throws JSONException {
        client.createPeerConnection();
    }

    @Override
    public void onCallReady(String callId) {
        if (callerId != null) {
            try {
                answer(callerId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            call(callId);
        }
    }

    public void answer(String callerId) throws JSONException {
        client.sendMessage(callerId, "init", null);
        startCam();
    }

    public void startCam() {
        // Camera settings
        client.start("android_test");
    }

    public void call(String callId) {
        Intent msg = new Intent(Intent.ACTION_SEND);
        msg.putExtra(Intent.EXTRA_TEXT, mSocketAddress + callId);
        msg.setType("text/plain");
        getActivityForContext().startActivityForResult(Intent.createChooser(msg, "Call someone :"), VIDEO_CALL_SENT);
    }

    @Override
    public void onStatusChanged(final String newStatus) {
        getActivityForContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, newStatus, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLocalStream(MediaStream localStream) {
        localStream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
        VideoRendererGui.update(localRender, 0, 0, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_BALANCED, true);
    }

    @Override
    public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {
        remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
        VideoRendererGui.update(remoteRender, 0, 0, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_BALANCED, false);
        VideoRendererGui.update(localRender, 0, 0, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_BALANCED, true);
    }

    @Override
    public void onRemoveRemoteStream(int endPoint) {
        VideoRendererGui.update(localRender, 0, 0, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_BALANCED, true);
    }

    //Fixme don't do this!!! ok for right now
    public Activity getActivityForContext() {
        return (Activity) mContext;
    }
}

