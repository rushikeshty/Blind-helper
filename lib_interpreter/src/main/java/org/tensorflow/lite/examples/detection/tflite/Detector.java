
package org.tensorflow.lite.examples.detection.tflite;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.speech.tts.TextToSpeech;

import java.util.List;

/** Generic interface for interacting with different recognition engines. */
public interface Detector {
  List<Recognition> recognizeImage(Bitmap bitmap);

  void enableStatLogging(final boolean debug);

  String getStatString();

  void close();

  void setNumThreads(int numThreads);

  void setUseNNAPI(boolean isChecked);

  /** An immutable result returned by a Detector describing what was recognized. */
  public class Recognition {

    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private final String id;

    /** Display name for the recognition. */
    private final String title;

    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */

    /** Optional location within the source image for the location of the recognized object. */
    private RectF location;
    private TextToSpeech texttospeech;

    public Recognition(
        final String id, final String title, final RectF location) {
      this.id = id;
      this.title = title;
      this.location = location;
    }

    public String getId() {
      return id;
    }

    public String getTitle() {
      return title;
    }



    public RectF getLocation() {
      return new RectF(location);
    }

    public void setLocation(RectF location) {
      this.location = location;
    }

    @Override
    public String toString() {
      String resultString = "";
      if (id != null) {
        resultString += "[" + id + "] ";
      }


      if (title != null) {
        resultString += title + " ";


      }

      if (location != null) {
        resultString += location + " ";
      }

      return resultString.trim();
    }
  }
}
