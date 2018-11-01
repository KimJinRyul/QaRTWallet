package jrkim.rcash.googlemap;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class TouchableWrapper extends FrameLayout {

    public interface OnTouchListener {
        void onTouch();
        void onRelease();
    }

    private OnTouchListener listener = null;

    public TouchableWrapper(@NonNull Context context) {
        super(context);
    }

    public void setTouchListener(OnTouchListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(listener != null)
                    listener.onTouch();
                break;
            case MotionEvent.ACTION_UP:
                if(listener != null)
                    listener.onRelease();
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}
