package org.tensorflow.lite.examples.detection.Calling.HandwrittenPhoneNumber;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.mlkit.vision.digitalink.Ink;

import org.tensorflow.lite.examples.detection.Home;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Main view for rendering content.
 *
 * <p>The view accepts touch inputs, renders them on screen, and passes the content to the
 * StrokeManager. The view is also able to draw content from the StrokeManager.
 */
public class DrawingView extends View implements StrokeManager.ContentChangedListener, RecognitionListener, TextToSpeech.OnUtteranceCompletedListener {
    private static final String AG = "MLKD.DrawingView";
    private static final int STROKE_WIDTH_DP = 3;
    static int count;
    static TextToSpeech textToSpeech;
    private static final int MIN_BB_WIDTH = 10;
    private static final int MIN_BB_HEIGHT = 10;
    private static final int MAX_BB_WIDTH = 256;
    private static final int MAX_BB_HEIGHT = 256;

    private final Paint recognizedStrokePaint;
    private final TextPaint textPaint;
    private final Paint currentStrokePaint;
    private final Paint canvasPaint;

    private final Path currentStroke;
    SpeechRecognizer speechRecognizer;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private StrokeManager strokeManager;

    public DrawingView(Context context) {
        this(context, null);
    }

    public DrawingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(this);

        currentStrokePaint = new Paint();
        currentStrokePaint.setColor(0xFFFF00FF); // pink.
        currentStrokePaint.setAntiAlias(true);
        // Set stroke width based on display density.
        currentStrokePaint.setStrokeWidth(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, STROKE_WIDTH_DP, getResources().getDisplayMetrics()));
        currentStrokePaint.setStyle(Paint.Style.STROKE);
        currentStrokePaint.setStrokeJoin(Paint.Join.ROUND);
        currentStrokePaint.setStrokeCap(Paint.Cap.ROUND);

        recognizedStrokePaint = new Paint(currentStrokePaint);
        recognizedStrokePaint.setColor(0xFFFFCCFF); // pale pink.

        textPaint = new TextPaint();
        textPaint.setColor(0xFF33CC33); // green.

        currentStroke = new Path();
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }


//React Bounding Box Component displays bounding boxes
    private static Rect computeBoundingBox(Ink ink) {
        float top = Float.MAX_VALUE;
        float left = Float.MAX_VALUE;
        float bottom = Float.MIN_VALUE;
        float right = Float.MIN_VALUE;
        for (Ink.Stroke s : ink.getStrokes()) {
            for (Ink.Point p : s.getPoints()) {
                top = Math.min(top, p.getY());
                left = Math.min(left, p.getX());
                bottom = Math.max(bottom, p.getY());
                right = Math.max(right, p.getX());
            }
        }
        float centerX = (left + right) / 2;
        float centerY = (top + bottom) / 2;
        Rect bb = new Rect((int) left, (int) top, (int) right, (int) bottom);
        // Enforce a minimum size of the bounding box such that recognitions for small inks are readable
        bb.union(
                (int) (centerX - MIN_BB_WIDTH / 2),
                (int) (centerY - MIN_BB_HEIGHT / 2),
                (int) (centerX + MIN_BB_WIDTH / 2),
                (int) (centerY + MIN_BB_HEIGHT / 2));
        // Enforce a maximum size of the bounding box, to ensure Emoji characters get displayed
        // correctly
        if (bb.width() > MAX_BB_WIDTH) {
            bb.set(bb.centerX() - MAX_BB_WIDTH / 2, bb.top, bb.centerX() + MAX_BB_WIDTH / 2, bb.bottom);
        }
        if (bb.height() > MAX_BB_HEIGHT) {
            bb.set(bb.left, bb.centerY() - MAX_BB_HEIGHT / 2, bb.right, bb.centerY() + MAX_BB_HEIGHT / 2);
        }
        return bb;
    }

    void setStrokeManager(StrokeManager strokeManager) {
        this.strokeManager = strokeManager;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
         canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        invalidate();
    }
//when user write something number after recognition display it gain i.e redraw
    public void redrawContent() {
        clear();
        Ink currentInk = strokeManager.getCurrentInk();
        drawInk(currentInk, currentStrokePaint);

        List<RecognitionTask.RecognizedInk> content = strokeManager.getContent();
        for (RecognitionTask.RecognizedInk ri : content) {
            drawInk(ri.ink, recognizedStrokePaint);
            final Rect bb = computeBoundingBox(ri.ink);
            drawTextIntoBoundingBox(ri.text, bb, textPaint);
        }
        invalidate();
    }

    private void drawTextIntoBoundingBox(String text, Rect bb, TextPaint textPaint) {
        final float arbitraryFixedSize = 20.f;
        // Set an arbitrary text size to learn how high the text will be.
        textPaint.setTextSize(arbitraryFixedSize);
        textPaint.setTextScaleX(1.f);

        // Now determine the size of the rendered text with these settings.
        Rect r = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), r);

        // Adjust height such that target height is met.
        float textSize = arbitraryFixedSize * (float) bb.height() / (float) r.height();
        textPaint.setTextSize(textSize);

        // Redetermine the size of the rendered text with the new settings.
        textPaint.getTextBounds(text, 0, text.length(), r);

        // Adjust scaleX to squeeze the text.
        textPaint.setTextScaleX((float) bb.width() / (float) r.width());

        // And finally draw the text.
        drawCanvas.drawText(text, bb.left, bb.bottom, textPaint);
    }

    private void drawInk(Ink ink, Paint paint) {
        for (Ink.Stroke s : ink.getStrokes()) {
            drawStroke(s, paint);
        }
    }

    private void drawStroke(Ink.Stroke s, Paint paint) {

        Path path = null;
        for (Ink.Point p : s.getPoints()) {
            if (path == null) {
                path = new Path();
                path.moveTo(p.getX(), p.getY());
            } else {
                path.lineTo(p.getX(), p.getY());
            }
        }
        drawCanvas.drawPath(path, paint);
    }

    public void clear() {
        currentStroke.reset();
        onSizeChanged(
                canvasBitmap.getWidth(),
                canvasBitmap.getHeight(),
                canvasBitmap.getWidth(),
                canvasBitmap.getHeight());
//        if(StrokeManager.phoneno.size()>2) {
//            textToSpeech.speak("Phone number is "+StrokeManager.phoneno.toString()+"."+"say yes to confirm ,or no to write the number again.",TextToSpeech.QUEUE_FLUSH,null,"hh");
//        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(currentStroke, currentStrokePaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
         int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                currentStroke.moveTo(x, y);

                break;
            case MotionEvent.ACTION_MOVE:
                currentStroke.lineTo(x, y);
                  break;
            case MotionEvent.ACTION_UP:
                currentStroke.lineTo(x, y);
                 drawCanvas.drawPath(currentStroke, currentStrokePaint);
                currentStroke.reset();

                if(StrokeManager.getphoneno().size()==10) {
                    count++;
                    //for double tap event
                    if(count==1){
                        startvoice();
                    }
                }
                break;
             default:

                break;
        }
        strokeManager.addNewTouchEvent(event);
        invalidate();
        return true;
    }

    @Override
    public void onContentChanged() {
        redrawContent();
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Toast.makeText(getContext(), "Listening...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {
        count=0;
        textToSpeech.speak("tap on the screen say yes to call or no to return in main menu",TextToSpeech.QUEUE_FLUSH,null);
    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(result.get(0).contains("yes")||result.get(0).contains("s")){
            Intent i = new Intent(Intent.ACTION_CALL);
            i.setData(Uri.parse("tel:" + StatusTextView1.separated));
            getContext().startActivity(i);
        }
        if(result.get(0).contains("no")){
           StrokeManager.getphoneno().clear();
           textToSpeech.speak("write the phone number or press long to return in main menu",TextToSpeech.QUEUE_FLUSH,null);
        }
        if(result.get(0).contains("menu")){
            Intent i = new Intent(getContext().getApplicationContext(),Home.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            getContext().startActivity(i);
        }

        Toast.makeText(getContext(), result.get(0),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    @Override
    public void onUtteranceCompleted(String s) {
        final Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                startvoice();
            }
        });


    }

    public void startvoice(){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext().getApplicationContext());
        speechRecognizer.setRecognitionListener(DrawingView.this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        speechRecognizer.startListening(intent);
    }
}
