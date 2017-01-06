package bluefletch.myapplication.webrtc.utils;

import android.hardware.Camera;
import android.util.Log;

import org.webrtc.VideoCapturerAndroid;

import static android.content.ContentValues.TAG;
import static org.webrtc.CameraEnumerationAndroid.getDeviceName;

/**
 * Created by robertgross on 1/4/17.
 */

public class CameraUtil {


    // Returns the name of the camera with camera index. Returns null if the
    // camera can not be used.
    public static String getDeviceName(int index) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        try {
            Camera.getCameraInfo(index, info);
        } catch (Exception e) {
            Log.e(TAG, "getCameraInfo failed on index " + index,e);
            return null;
        }
        String facing =
                (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) ? "front" : "back";
        return "Camera " + index + ", Facing " + facing
                + ", Orientation " + info.orientation;
    }
    // Returns the name of the front facing camera. Returns null if the
    // camera can not be used or does not exist.
    public static String getNameOfFrontFacingDevice() {
        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            try {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                    return getDeviceName(i);
            } catch (Exception e) {
                Log.e(TAG, "getCameraInfo failed on index " + i, e);
            }
        }
        return null;
    }

    // Returns the name of the back facing camera. Returns null if the
    // camera can not be used or does not exist.
    public static String getNameOfBackFacingDevice() {
        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            try {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
                    return getDeviceName(i);
            } catch (Exception e) {
                Log.e(TAG, "getCameraInfo failed on index " + i, e);
            }
        }
        return null;
    }
}
