package bluefletch.myapplication.webrtc.interfaces;

/**
 * Created by robertgross on 1/5/17.
 */

import org.webrtc.MediaStream;

/**
 * Implement this interface to be notified of events.
 */
public interface iRtcListener {
    void onCallReady(String callId);

    void onStatusChanged(String newStatus);

    void onLocalStream(MediaStream localStream);

    void onAddRemoteStream(MediaStream remoteStream, int endPoint);

    void onRemoveRemoteStream(int endPoint);
}
