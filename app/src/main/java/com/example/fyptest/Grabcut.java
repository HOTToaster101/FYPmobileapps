package com.example.fyptest;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.fyptest.tflite.Classifier;
import com.example.fyptest.tflite.TensorFlowImageClassifier;
import com.example.fyptest.ui.Sticker.AddStickerFragment;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.threshold;

public class Grabcut extends Fragment implements OnTouchListener {

    //private static final Bitmap.Config ARGB_8888 = ;
    ImageView iv;
    Bitmap bitmap, bitmapResult;
    FrameLayout fl;
    Mat img;

    ProgressBar progressBar;

    Scalar Red = new Scalar(0, 0, 255);
    Scalar Blue = new Scalar(255, 0, 0);
    Scalar Green = new Scalar(0, 255, 0);

    int radius = 15;
    int width = -1;

    Mat image = new Mat();
    Mat mask = new Mat();
    Mat bgdModel = new Mat();
    Mat fgdModel = new Mat();
    Mat fgmask = new Mat();
    Mat bgmask = new Mat();
    Mat mask255 = new Mat();
    Mat fgdPxls = new Mat();
    Mat bgdPxls = new Mat();
    Rect rect;

    boolean initialized;
    boolean processing;

    boolean edit_fg = true;

    int iterCount;
    String emotion;

    float mDips = 1;
    float mMul = 1;

    private Classifier classifier;
    FragmentManager manager;

    public static final String TAG = "Grabcut";

    private static final String MODEL_PATH = "emotion60.tflite";
    private static final String LABEL_PATH = "dict.txt";
    private static final int INPUT_SIZE = 48;

    private Executor executor = Executors.newSingleThreadExecutor();

    //Opencv loading
    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

    public Grabcut(Bitmap imagebitmap){
        this.bitmap = imagebitmap;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.grabcut_main, container, false);

        manager = ((AppCompatActivity)v.getContext()).getSupportFragmentManager();
        emotion = "";
        initTensorFlowAndLoadModel(); // initialize the variable classifier
        progressBar = v.findViewById(R.id.progressBar);
        fl = v.findViewById(R.id.fl_grab);
        iv = (ImageView) v.findViewById(R.id.imageView);
        iv.setOnTouchListener(this);
        TextView text = v.findViewById(R.id.tv_result);
        Button btn1 = (Button) v.findViewById(R.id.button1);
        btn1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                nextIteration();
                bitmapResult = getSaveImage();
                bitmapResult = toGrayscale(bitmapResult); //adjust the bitmap to greyscale

                // Adjust the bitmap to 48x48
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmapResult.compress(Bitmap.CompressFormat.PNG, 100, stream);
                bitmapResult = Bitmap.createScaledBitmap(bitmapResult, INPUT_SIZE, INPUT_SIZE, false);
                iv.setImageBitmap(bitmapResult);

                //initiating Emotion Recognition
                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmapResult);
                String topResult = results.get(0).getTitle(); // highest precision result (label)
                Float topPrecision = results.get(0).getConfidence();// highest precision result (possibility)

                emotion = topResult;
                text.setText(topResult + ", " + topPrecision);
                for(int i = 0; i < 7; i++){
                    System.out.println("the percision: " + results.get(i).getTitle() + results.get(i).getConfidence());
                }

                //Toast.makeText(getApplicationContext(), topResult + ", " + topPrecision, Toast.LENGTH_LONG).show();

                progressBar.setVisibility(View.GONE);
            }

        });
        Button fgbutton = (Button) v.findViewById(R.id.buttonFG);
        fgbutton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                edit_fg = true;
            }

        });
        Button bgbutton = (Button) v.findViewById(R.id.buttonBG);
        bgbutton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                edit_fg = false;
            }

        });
        Button savebutton = (Button) v.findViewById(R.id.buttonsave);
        savebutton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                System.out.println("save function is running");
                getPreference();
            }

        });
        /*Button recoverbutton = (Button) v.findViewById(R.id.button);
        savebutton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                System.out.println("save function is running");
                getPreference();
            }

        });*/
        Button recButton = (Button) v.findViewById(R.id.buttonRec);
        recButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmapResult = getSaveImage();
                bitmapResult = toGrayscale(bitmapResult); //adjust the bitmap to greyscale
                iv.setImageBitmap(bitmapResult);

                // Adjust the bitmap to 48x48
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmapResult.compress(Bitmap.CompressFormat.PNG, 100, stream);
                bitmapResult = Bitmap.createScaledBitmap(bitmapResult, INPUT_SIZE, INPUT_SIZE, false);


                //initiating Emotion Recognition
                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmapResult);
                String topResult = results.get(0).getTitle(); // highest precision result (label)
                Float topPrecision = results.get(0).getConfidence();// highest precision result (possibility)

                emotion = topResult;
                text.setText(topResult + ", " + topPrecision);
                //Toast.makeText(getApplicationContext(), topResult + ", " + topPrecision, Toast.LENGTH_LONG).show();
            }
        });


        /**if (iv.getDrawable() instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        } else {
            //Drawable d = iv.getDrawable();
            // bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            if (getActivity().getIntent() != null) {
                //String filename = getIntent().getStringExtra("pictureinput");
                try {
                    FileInputStream is = this.getActivity().openFileInput("myImage");
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    //Toast.makeText(this, "error: cannot retrieve captured image", Toast.LENGTH_LONG).show();
                }

            }
           // Bitmap newbitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            //iv.setImageBitmap(newbitmap);
           // Canvas canvas = new Canvas(newbitmap);
            //Canvas canvas = new Canvas(bitmap);
            //d.draw(canvas);
        }**/
        //BitmapDrawable
        //Bitmap _bitmap = ((BitmapDrawable)iv.getDrawable()).getBitmap();
        Log.i(TAG, "_bitmap getWidth=" + bitmap.getWidth() + ",_bitmap.getHeight()=" + bitmap.getHeight());

        DisplayMetrics metrics = this.getResources()
                .getDisplayMetrics();

        int widthValue = metrics.widthPixels;
        int heightValue = metrics.heightPixels;
        mDips = getResources().getDisplayMetrics().density;
        Log.i(TAG, "widthValue=" + widthValue + ",heightValue=" + heightValue + ",mDips=" + mDips);

        // float mMul = (512 *100)/widthValue;
        Log.i(TAG, "mMul=" + mMul);
        //bitmap = _bitmap;//BitmapFactory.decodeResource(getResources(), R.drawable.xxx);


        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        bitmapResult = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        /*if(bitmap != null){
            Toast.makeText(this, "bitmap is NOT NULL", Toast.LENGTH_LONG).show();
        }*/
        bitmap = getResizedBitmap(bitmap, 512);
        bitmapResult = getResizedBitmap(bitmapResult, 512);
        iv.setImageBitmap(bitmap);

        //bitmapResult
        img = new Mat();
        Utils.bitmapToMat(bitmap, img);
        mask = new Mat(img.size(), CvType.CV_8UC1);
        bitmap.getWidth();
        setImage(bitmap);

        return v;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getActivity().getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }


    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;

        Log.i(TAG, "bitmapRatio=" + bitmapRatio);
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG, "counter=0 :::  tl.x=" + event.getX() + "  , tl.y=" + event.getY());
        mMul = (float) 0.5;
        Log.i(TAG, "counter*mDips :::  tl.x=" + event.getX() * mMul + "  , tl.y=" + event.getY() * mMul);
        Point tapPoint = new Point(event.getX() * mMul, event.getY() * mMul);
        maskLabel(tapPoint, edit_fg);
        bitmapResult = getImage();
        iv.setImageBitmap(bitmapResult);
        return true;
    }

    private void reset() {
        log("reset");
        /*if (!mask.empty()) {
            mask.setTo(Scalar.all(Imgproc.GC_BGD));
            Toast.makeText(this, "mask NULL", Toast.LENGTH_LONG).show();
        }*/

        bgdPxls = Mat.zeros(image.size(), CvType.CV_8UC1);
        fgdPxls = Mat.zeros(image.size(), CvType.CV_8UC1);

        fgmask.create(image.size(), CvType.CV_8UC3);
        fgmask.setTo(Red);
        bgmask.create(image.size(), CvType.CV_8UC3);
        bgmask.setTo(Blue);

        mask255.create(image.size(), CvType.CV_8UC1);
        mask255.setTo(new Scalar(255));
        int off_x = 1; //image.cols * 0.1;
        int off_y = 1; //image.rows * 0.1;

        rect = new Rect(off_x, off_y, image.cols() - 2 * off_x, image.rows() - 2 * off_y);
        setRectInMask();
        initialized = false;
        iterCount = 0;
    }

    private void setRectInMask() {
        log("setRectInMask");
        mask.setTo(new Scalar(Imgproc.GC_BGD));
        rect.x = Math.max(0, rect.x);
        rect.y = Math.max(0, rect.y);
        rect.width = Math.min(rect.width, image.cols() - rect.x);
        rect.height = Math.min(rect.height, image.rows() - rect.y);
        mask.setTo(new Scalar(Imgproc.GC_PR_FGD));

        log("setRectInMask rect.width" + rect.width);
        log("setRectInMask rect.height" + rect.height);
    }


    public void setImage(Bitmap bitmap) {
        log("setImage");

        Utils.bitmapToMat(bitmap, image);
        List<Mat> planes = new ArrayList<Mat>(4);
        List<Mat> rgb = new ArrayList<Mat>(4);

        Core.split(image, planes);
        rgb.add(planes.get(0));
        rgb.add(planes.get(1));
        rgb.add(planes.get(2));
        Core.merge(rgb, image);

        mask.create(image.size(), CvType.CV_8UC1);
        reset();
    }

    public Bitmap getImage() {

        log("getImage");
        Mat result = new Mat();
        Mat binarySeqc;

        if (!initialized) {
            image.copyTo(result);
        } else {
            binarySeqc = getBinMask(mask);
            image.copyTo(result, binarySeqc);
        }
        // alpha blending
        fgmask.copyTo(result, fgdPxls);
        bgmask.copyTo(result, bgdPxls);
        Imgproc.rectangle(result, new Point(rect.x, rect.y), new Point(rect.x + rect.width - 1, rect.y + rect.height - 1), Green, 2);
        Utils.matToBitmap(result, bitmapResult);
        return bitmapResult;
    }

    private void nextIteration() {
        progressBar.setVisibility(View.VISIBLE);
        log("nextIteration");
        if (processing) {
            return;
        }
        processing = true;

        if (initialized) {
            Imgproc.grabCut(image, mask, rect, bgdModel, fgdModel, 1, Imgproc.GC_INIT_WITH_RECT);

        } else {
            Imgproc.grabCut(image, mask, rect, bgdModel, fgdModel, 1, Imgproc.GC_INIT_WITH_MASK);
            initialized = true;
        }
        iterCount++;

        bgdPxls.setTo(new Scalar(0));
        fgdPxls.setTo(new Scalar(0));

        processing = false;

    }

    public Bitmap getSaveImage() {
        log("getSaveImage");
        Mat result = new Mat();
        Mat binarySeqc = new Mat();

        if (!initialized) {
            image.copyTo(result);

        } else {

            binarySeqc = getBinMask(mask);
            image.copyTo(result, binarySeqc);

            // add alpha channel from mask
            Mat alpha = new Mat();
            mask255.copyTo(alpha, binarySeqc);
            List<Mat> v = new ArrayList<Mat>(2);

            v.add(result);
            v.add(alpha);
            Core.merge(v, result);
        }
        return transparentBG(result);
  /*
    Utils.matToBitmap(dst, bitmapResult);

    return bitmapResult;
    */
    }


    public void maskLabel(Point point, boolean isForeground) {

        log("maskLabel");
        Point p = new Point(point.x, point.y);
        if (isForeground) {
            Imgproc.circle(fgdPxls, p, radius, new Scalar(1), width);
            Imgproc.circle(bgdPxls, p, radius, new Scalar(0), width);
            Imgproc.circle(mask, p, radius, new Scalar(Imgproc.GC_FGD), width);
        } else {
            Imgproc.circle(bgdPxls, p, radius, new Scalar(1), width);
            Imgproc.circle(fgdPxls, p, radius, new Scalar(0), width);
            Imgproc.circle(mask, p, radius, new Scalar(Imgproc.GC_BGD), width);
        }


    }


    private Mat getBinMask(Mat comMask) {
        Mat binarySeqc = new Mat();
        if (comMask.empty() || comMask.type() != CvType.CV_8UC1)
            //  CV_Error( CV_StsBadArg, "comMask is empty or has incorrect type (not CV_8UC1)" );
            if (binarySeqc.empty() || binarySeqc.rows() != comMask.rows() || binarySeqc.cols() != comMask.cols())
                binarySeqc.create(comMask.size(), CvType.CV_8UC1);

        Mat src2 = new Mat(comMask.size(), CvType.CV_8UC1, new Scalar(1));
        Core.bitwise_and(comMask, src2, binarySeqc);

        return binarySeqc;
    }


    private Bitmap transparentBG(Mat src) {

        Mat dst = new Mat(src.size(), CvType.CV_8UC4);  //(src.rows,src.cols,CV_8UC4);
        Mat tmp = new Mat();
        Mat thr = new Mat();

        cvtColor(src, tmp, Imgproc.COLOR_BGR2GRAY);
        threshold(tmp, thr, 100, 255, THRESH_BINARY);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfInt4 hierarchy = new MatOfInt4();

        int largest_contour_index = 0;
        int largest_area = 0;

        Mat alpha = new Mat(src.size(), CvType.CV_8UC1, new Scalar(0));

        Imgproc.findContours(tmp, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE); // Find the contours in the image
        for (int i = 0; i < contours.size(); i++) // iterate through each contour.
        {
            double a = Imgproc.contourArea(contours.get(i), false);  //  Find the area of contour
            if (a > largest_area) {
                largest_area = (int) a;
                largest_contour_index = i;                //Store the index of largest contour
            }
        }

        Imgproc.drawContours(alpha, contours, largest_contour_index, new Scalar(255), Core.FILLED, 8, hierarchy, Integer.MAX_VALUE, new Point());
        List<Mat> rgb = new ArrayList<Mat>(3);
        List<Mat> rgba = new ArrayList<Mat>(4);

        Core.split(src, rgb);
        rgba.add(rgb.get(0));
        rgba.add(rgb.get(1));
        rgba.add(rgb.get(2));
        rgba.add(alpha);

        Core.merge(rgba, dst);

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, output);
        return output;

    }


    public void saveaspic() {
        /*bitmapResult = finaltransparentbackground();*/
        log("SavingImage");
        ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
        //File sdkPath = cw.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "grabcut.png";
        String state = Environment.getExternalStorageState();
        int countId = getStickerCount();
        if(state.equals(Environment.MEDIA_MOUNTED)) {
            String path = getActivity().getExternalFilesDir(null) + File.separator + Integer.toString(countId) + "grabcut.webp";
            File file = new File(path);
            if (!file.exists()) {
                Log.d("path", file.toString());
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(file);
                    if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                        bitmapResult.compress(Bitmap.CompressFormat.WEBP_LOSSY, 100, fileOutputStream);
                    }else{
                        bitmapResult.compress(Bitmap.CompressFormat.WEBP, 100, fileOutputStream);
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    //Toast.makeText(this, "Save successfully! path = " + path, Toast.LENGTH_LONG).show();
                    System.out.println("Save successfully! path = " + path);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        }
        AddStickerFragment f = new AddStickerFragment(bitmapResult, countId);
        //frameTransit();
        manager.beginTransaction()
                .setCustomAnimations(
                        R.anim.fragment_fade_enter,
                        R.anim.fragment_fade_exit
                )
                .replace(R.id.fl_camera, f, null)
                .commit();

        //finish();
    }

    public int getStickerCount(){
        String state = Environment.getExternalStorageState();
        int count = 0;
        if(state.equals(Environment.MEDIA_MOUNTED)) {
            String path = getActivity().getExternalFilesDir(null) + "/config.txt";
            File file = new File(path);
            //file.delete();
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(Integer.toString(1).getBytes());
                    fos.close();
                    //Toast.makeText(this, "Save successfully! path = " + Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "grabcut.webp", Toast.LENGTH_LONG).show();
                    //System.out.println("Save successfully! path = " + Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "grabcut.png");
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line = br.readLine();
                    count = Integer.parseInt(line);

                    br.close();
                    updateCount(count + 1);
                }
                catch (IOException e) {
                    //You'll need to add proper error handling here
                }

            }
        }
        System.out.println("the current count is " + count);
        return count;
    }

    public void updateCount(int i){
        String path = getActivity().getExternalFilesDir(null) + "/config.txt";
        File file = new File(path);
        file.delete();
        try{
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(Integer.toString(i).getBytes());
            fos.close();
        }catch(Exception e){
            e.printStackTrace();
        }


    }

    private void getPreference(){
        String path = getActivity().getExternalFilesDir(null) + File.separator +"pref.txt";
        File file = new File(path);
        try{
            if(file.exists()){
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = br.readLine();

                if(line.equals("sticker")){
                    saveaspic();
                }else if(line.equals("emoji")){
                    System.out.println("generate emoji to clipboard");
                    createEmoji();
                }

                br.close();
            }else{
                saveaspic();
            }

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void createEmoji(){
        ClipboardManager clipboard = (ClipboardManager)
                getContext().getSystemService(Context.CLIPBOARD_SERVICE);

        String emoji;
        String laughEmoji1 = new String(Character.toChars(0x1F602));
        switch(emotion){
            case "Happy":
                emoji = laughEmoji1;
                System.out.println("happy is generated ");
                break;
            case "Angry":
                emoji = new String(Character.toChars(0x1F620)); //angry face
                System.out.println("angry is generated ");
                break;
            case "Sad":
                emoji = new String(Character.toChars(0x1F62D)); //laoudly crying face
                System.out.println("sad is generated ");
                break;
            case "Neutral":
                emoji = new String(Character.toChars(0x1F610)); //neural face
                System.out.println("neutral is generated ");
                break;
            case "Fear":
                emoji = new String(Character.toChars(0x1F628)); //fearful face
                System.out.println("fear is generated ");
                break;
            case "Disgust":
                emoji = new String(Character.toChars(0x1F612)); //unamused face
                System.out.println("disgust is generated ");
                break;
            case "Surprise":
                emoji = new String(Character.toChars(0x1F62E)); //face with open mouth
                System.out.println("surprise is generated ");
                break;
            default:
                emoji = new String(Character.toChars(0x1F603)); //smiling with open mouth face
                System.out.println("nothing is generated ");
                break;
        }

        ClipData clip = ClipData.newPlainText("testLabel", emoji);
        System.out.println("emoji is generated " + emotion);

        clipboard.setPrimaryClip(clip);
    }

    public static void log(String message) {

        String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();

        Log.d(className + "." + methodName + "():" + lineNumber, message);

    }

    /*public Bitmap finaltransparentbackground(){
        //Mat src=imread("0.jpg",1);
        Bitmap tmpbm = bitmapResult.copy(Bitmap.Config.ARGB_8888, true);
        Mat src = new Mat(tmpbm.getWidth(), tmpbm.getHeight(), CvType.CV_8UC4);
        Mat dst = new Mat(tmpbm.getWidth(), tmpbm.getHeight(), CvType.CV_8UC4);
        Mat tmp = new Mat(tmpbm.getWidth(), tmpbm.getHeight(), CvType.CV_8UC4);
        Mat alpha = new Mat(tmpbm.getWidth(), tmpbm.getHeight(), CvType.CV_8UC4);

        Utils.bitmapToMat(tmpbm, src);
        cvtColor(src,tmp,COLOR_BGRA2GRAY);
        threshold(tmp,alpha,100,255,THRESH_BINARY);

        List<Mat> rgb = new ArrayList<Mat>(3);
        Core.split(src,rgb);

        List<Mat> rgba = new ArrayList<Mat>(4);
        rgba.add(rgb.get(0));
        rgba.add(rgb.get(1));
        rgba.add(rgb.get(2));
        rgba.add(alpha);
        Core.merge(rgba, dst);

        Bitmap output = Bitmap.createBitmap(tmpbm.getWidth(), tmpbm.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, output);
        return output;
    }*/
}