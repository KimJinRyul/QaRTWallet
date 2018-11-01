package jrkim.rcash.googlemap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;

public class TouchableMapFragment extends SupportMapFragment {

    private View originalContentView;
    private TouchableWrapper touchView;

    public void setTouchListener(TouchableWrapper.OnTouchListener listener) {
        touchView.setTouchListener(listener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        originalContentView = super.onCreateView(inflater, parent, savedInstanceState);
        touchView = new TouchableWrapper(getActivity());
        touchView.addView(originalContentView);
        return touchView;
    }

    @Override
    public View getView() {
        return originalContentView;
    }
}
