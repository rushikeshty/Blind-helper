package org.tensorflow.lite.examples.detection.Navigation;


import java.util.List;

 public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
