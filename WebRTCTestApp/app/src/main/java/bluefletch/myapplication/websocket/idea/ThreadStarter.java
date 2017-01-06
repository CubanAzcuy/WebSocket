package bluefletch.myapplication.websocket.idea;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by robertgross on 12/28/16.
 */

public class ThreadStarter {

    private final Handler handler;
    final HandlerThread handlerThread;

    public ThreadStarter(String thread) {
        handlerThread = new HandlerThread(thread);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public boolean isAlive() {
        return handlerThread.isAlive();
    }
    public Handler get() {
        return handler;
    }
}

