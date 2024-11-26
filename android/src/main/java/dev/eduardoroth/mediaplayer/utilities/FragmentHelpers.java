package dev.eduardoroth.mediaplayer.utilities;

import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.getcapacitor.Bridge;

import dev.eduardoroth.mediaplayer.R;

public class FragmentHelpers {

    private Bridge bridge;
    private int layoutId;
    private ViewGroup layout;

    public FragmentHelpers(Bridge bridge) {
        this.bridge = bridge;
    }

    public void loadFragment(Fragment vpFragment, int frameLayoutId) {
        this.layoutId = frameLayoutId;
        FragmentManager fm = bridge.getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(frameLayoutId, vpFragment, String.valueOf(layoutId));
        fragmentTransaction.commit();
    }

    public void updateFragmentLayout(Fragment vpFragment, boolean onlyVideo){
        View playerView = vpFragment.getView();
        if(onlyVideo){
            layout = (ViewGroup)playerView.getParent();
            layout.removeView(playerView);
            layout.setVisibility(View.GONE);
            ViewGroup newLayout = ((ViewGroup) bridge.getWebView().getParent()).findViewWithTag("VIDEO_ONLY");
            newLayout.animate();
            newLayout.addView(playerView);
            newLayout.setVisibility(View.VISIBLE);
            newLayout.bringToFront();
            newLayout.bringChildToFront(playerView);
        } else {
            ViewGroup videoOnlyLayout = (ViewGroup) playerView.getParent();
            videoOnlyLayout.removeView(playerView);
            videoOnlyLayout.setVisibility(View.GONE);
            layout.animate();
            layout.addView(playerView);
            layout.setVisibility(View.VISIBLE);
            layout.bringToFront();
        }

    }

    public void removeFragment(Fragment vpFragment) {
        FragmentManager fm = bridge.getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.remove(vpFragment);
        fragmentTransaction.commit();
    }

    public int getIdFromPlayerId(String playerId) {
        int layoutId = 0;
        for (char c : playerId.toCharArray())
            layoutId += (int) c;
        return layoutId;
    }
}
