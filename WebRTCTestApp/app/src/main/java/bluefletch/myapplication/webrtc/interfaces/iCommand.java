package bluefletch.myapplication.webrtc.interfaces;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by robertgross on 1/5/17.
 */

public interface iCommand {
    void execute(String peerId, JSONObject payload) throws JSONException;
}
