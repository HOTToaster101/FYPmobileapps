package com.example.fyptest;


import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;


import androidx.annotation.RequiresApi;

import com.arthenica.mobileffmpeg.FFmpeg;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


//import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;



public class VideoSegmentation extends Activity{
    //private static final String MODEL_PATH2 = "person_segmentation_backup.tflite";    // model to use
    private static final String MODEL_PATH ="person_segmentation.tflite";
    // image buffers shape
    private static final int batchsize = 1;
    private static final int pixelsize = 1;
    //private static final int height = 513;
    private static final int height = 256;
    //private static final int width = 513;
    private static final int width = 256;
    private static final int inchannels = 3;
    private static final int outchannels = 1;

    private static final boolean useGpu = false;
    private static final String DIRECTORY_PICTURES = "Pictures";
    private static final String TAG = "videosegmentation";

    private GpuDelegate gpudelegate;
    private Interpreter tflite;
    private FileInputStream inputstream;
    private int videolength;
    public double fps;


    private ByteBuffer inpImg;
    //private int[][][][] outImg;
    private long[][] outImg;

    public Mat[] mattmp = new Mat[200];
    //Mat overlap = new Mat(513,513,CvType.CV_8UC4);
    public Mat overlap = new Mat();

    public Mat resized = new Mat(513,513,CvType.CV_8UC3);

    public VideoWriter videoWriter;

    // classes to be displayed/colors and their respective names
    private int[][] colors = new int[21][3];
    private boolean[] displayClass = new boolean[21];
    private String[] classNames = new String[21];

    //static
    // {
    // System.loadLibrary("libc++_shared.so");
    //}

    static {

        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
        System.loadLibrary("opencv_java4");
    }

    public VideoSegmentation(Activity activity) {
        try {
            gpudelegate = new GpuDelegate();
            Interpreter.Options options = (new Interpreter.Options()).addDelegate(gpudelegate);
            if (!useGpu) {
                options = (new Interpreter.Options()).setNumThreads(4);

            }
            tflite = new Interpreter(loadModelFile(activity, MODEL_PATH), options);
        }
        catch (IOException e) {e.printStackTrace();}




        //inpImg = ByteBuffer.allocateDirect(batchsize * height * width * pixelsize * inchannels);
        inpImg = ByteBuffer.allocate(batchsize * height * width * pixelsize * inchannels);
        //inpImg.order(ByteOrder.nativeOrder());

        //outImg = new int[batchsize][height] [ width] [ outchannels];
        outImg = new long[batchsize][batchsize * height * width * pixelsize ];

        colors[0]  = new int[]{255,  255,   255,  0};  displayClass[0]  = true; classNames[0]  = "bg";  ;
        colors[15] = new int[]{0, 0, 0, 255};  displayClass[15] = true;  classNames[15] = "person";
    }




    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void segmentImage(Uri uri) {

        // segment Mat inplace
        if(tflite!=null) {
            //openvideofile(uri);
            String path = uri.getPath();
            VideoCapture cap = new VideoCapture();
            int numofframe = 0;
            Mat[] matarray = new Mat[10000];
            Mat inputFrame = new Mat();
            String mpegfilename = "/storage/emulated/0/Android/data/com.example.fyptest/files/Pictures/video_1.mjpeg";
            String avifilename = "/storage/emulated/0/Android/data/com.example.fyptest/files/Pictures/video_2.avi";

            String state = Environment.getExternalStorageState();
            if(state.equals(Environment.MEDIA_MOUNTED)) {
                //mpegfilename = getActivity().getExternalFilesDir(null)+ File.separator  + "video_1.mjpeg";
                //avifilename = getActivity().getExternalFilesDir(null)+ File.separator  + "video_1.avi";
            }


               /* do {
                    externalFileRootDir = Objects.requireNonNull(externalFileRootDir).getParentFile();
                } while (Objects.requireNonNull(externalFileRootDir).getAbsolutePath().contains("/Android"));

                String saveDir = Objects.requireNonNull(externalFileRootDir).getAbsolutePath();

                String mpegfilename = saveDir + "/" + Environment.DIRECTORY_DCIM + "/" + "video_1.mjpeg";
                String avifilename = saveDir + "/" + Environment.DIRECTORY_DCIM + "/"  + "video_1.avi";*/
            try {
                Log.d("vc", "opened" + path);
                inputstream = new FileInputStream(path);
                //String ffmpegCmdString = "-i" + path + "-vcodec mjpeg" + mpegfilename;
                //String[] splitCmd = ffmpegCmdString.split(" ");
                //System.loadLibrary("opencv_ffmpeg300_64");

                //int grabber = FFmpeg.execute("-y " + "-i " + path + " -vcodec mjpeg " + mpegfilename);
                //Log.d("avi", "convert successfully");
                //int toAvi = FFmpeg.execute("-y " + "-i " + mpegfilename + " -vcodec " + avifilename);
                // int toAvi2 = FFmpeg.execute("-i " + path + " -f avi -vcodec mjpeg " +  avifilename);
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputstream);
                OpenCVFrameConverter.ToMat Matconvert = new OpenCVFrameConverter.ToMat();

                grabber.start();
                Log.d("vc", "videolength" + grabber.getLengthInVideoFrames());
                //Toast.makeText(VideoSegmentation.this, "Processing. Please wait", Toast.LENGTH_LONG).show();
                Mat img = new Mat();
                int cameraHeight = 513;
                int cameraWidth = 513;

                Mat resizeimg = new Mat();
                Mat inputtmp = new Mat();


                //Mat img = new Mat(cameraHeight, cameraWidth, CvType.CV_8UC4);

                for(int frameCount = 0; frameCount < grabber.getLengthInVideoFrames(); frameCount++){
                    Frame nthframe = grabber.grabImage();
                    img = Matconvert.convertToOrgOpenCvCoreMat(nthframe);
                    //cap.read(img);
                    //videolength = (int)cap.get(Videoio.CAP_PROP_FRAME_COUNT);
                    fps=grabber.getFrameRate();
                    //fps = (int)cap.get(Videoio.CAP_PROP_FPS);
                    //Mat img = inputFrame;

                    Size size = new Size(513, 513);
                    Size sizetmp = new Size(256, 256);
                    //Log.d("vc", "times:"+frameCount);
                    if(img != null){
                        Log.d("vc", "img is not null"+frameCount);
                    }
                    Mat tmp = new Mat(513,513,CvType.CV_8UC4);
                    Mat output = new Mat();
                    Mat resizeinput = new Mat();
                    Mat resizedrawinput = new Mat();
                    Mat rawinput = new Mat(513,513,CvType.CV_8UC4);

                    Imgproc.cvtColor(img, inputtmp, Imgproc.COLOR_RGBA2RGB);
                    //Imgproc.cvtColor(img, resizeinput, Imgproc.COLOR_RGBA2RGB);
                    //Imgproc.resize(inputtmp,resizeinput, size, 0, 0, Imgproc.INTER_LANCZOS4);

                    Imgproc.resize(inputtmp,resizeinput, sizetmp);
                    Imgproc.cvtColor(img, rawinput, Imgproc.COLOR_RGB2BGRA);
                    Imgproc.resize(rawinput,resizedrawinput, size);
                    loadMatToBuffer(resizeinput);
                    //Log.d("vc", "resize");
                    tflite.run(inpImg, outImg);
                    Mat result = loadBufferToMat(resizeinput, frameCount);
                    //Core.copyMakeBorder(tmp, resized,
                    //0, 0, (cameraWidth-cameraHeight)/2+(cameraHeight/23), (cameraWidth-cameraHeight)/2-(cameraHeight/23),
                    //Core.BORDER_CONSTANT, new Scalar(0));
                    //Core.addWeighted(resizeinput, 0.8, resized, 0.2, 0.0, outputimg);
                    Mat resizetmp = new Mat();
                    Imgproc.resize(result, resizetmp, size);
                    Point start = new Point(0, 0);

                    Core.add(resizedrawinput, resizetmp, output);

                    //overlayImage(resizeinput, resizetmp, start);
                    mattmp[frameCount] = output;

                    numofframe = frameCount;


                }
                grabber.close();

            } catch (Exception e){
                e.printStackTrace();
            }

            outputvideo(mattmp, numofframe);

                /*loadMatToBuffer(modelMat);
                tflite.run(inpImg, outImg);
                loadBufferToMat(modelMat, classesDetected);*/
        }
    }

        
        
        private void overlayImage(Mat bg, Mat fg, Point location){

            Imgproc.cvtColor(bg, bg, Imgproc.COLOR_RGB2RGBA);
            bg.copyTo(overlap);
            for(int y = (int) Math.max(location.y , 0); y < bg.rows(); ++y){
                int fY = (int) (y + location.y);
                if(fY >= fg.rows())
                    break;
                for(int x = (int) Math.max(location.x, 0); x < bg.cols(); ++x){
                    int fX = (int) (x + location.x);
                    if(fX >= fg.cols()){
                        break;
                    }

                    double opacity;
                    double[] finalPixelValue = new double[4];
                    opacity = fg.get(fY , fX)[3];

                    finalPixelValue[0] = bg.get(fY, fX)[0];
                    finalPixelValue[1] = bg.get(fY, fX)[1];
                    finalPixelValue[2] = bg.get(fY, fX)[2];
                    finalPixelValue[3] = bg.get(fY, fX)[3];

                    for(int c = 0;  c < overlap.channels(); ++c){
                        if(opacity > 0){
                            double fgPx =  fg.get(fY, fX)[c];

                            double bgPx =  bg.get(y, x)[c];

                            float fOpacity = (float) (opacity / 255);
                            finalPixelValue[c] = ( (fgPx * fOpacity));
                            if(c==3){
                                finalPixelValue[c] = fg.get(fY,fX)[3];
                            }
                        }
                    }
                    overlap.put(fY, fX,finalPixelValue);
                }
            }

        }


    private void loadMatToBuffer(Mat inMat) {
        //convert opencv mat to tensorflowlite input
        inpImg.rewind();
        byte[] data = new byte[width * height * inchannels];
        inMat.get(0, 0, data);
        inpImg = ByteBuffer.wrap(data);
    }

    private Mat loadBufferToMat(Mat modelMat, int count) {
        //tflite output to opencv mat
        //boolean[] classesFound = new boolean[2];
        Mat temp_outSegment = new Mat(height, width, CvType.CV_32SC4);
        Mat tmp = new Mat(513,513,CvType.CV_8UC4);
        // major bottleneck(remove loop - load buffer directly to Mat somehow)
        //Arrays.fill(classesFound, false);
        temp_outSegment.setTo(new Scalar(colors[0][0],colors[0][1],colors[0][2], colors[0][3]));
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                //int cl = (int)outImg[0][y][x][0];
                int cl = (int)outImg[0][y*height+x];
                if (displayClass[cl]){
                    temp_outSegment.put(y, x, colors[cl]);

                    //classesFound[cl]=true;
                }
            }
        }
        //Log.d("vc", "count= "+count);

        temp_outSegment.convertTo(tmp, CvType.CV_8UC4);
        return tmp;
        
    }

    public void outputvideo(Mat[] array, int count){
        //Log.d("vc", "videoinput");
        //ContextWrapper cw = new ContextWrapper(getApplicationContext());
        org.opencv.core.Size size= array[1].size();
        Log.d("vc", "videoinput" + array[1].size() + "fps: " + fps);
        int fourcc = VideoWriter.fourcc('M','J','P','G');


        //System.loadLibrary("ffmpeg_64");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date now = new Date();
        //String path = getExternalFilesDir("Pictures") + formatter.format(now)+"test1.avi";

        String path = "/storage/emulated/0/Android/data/com.example.fyptest/files/Pictures/" + formatter.format(now) +"test2.avi";
        String path2 = "/storage/emulated/0/Android/data/com.example.fyptest/files/Pictures/" + formatter.format(now) +"test2.webp";
        //VideoWriter videoWriter = new VideoWriter();




        // videoWriter;

        fps = 29;


        //videoWriter = new VideoWriter("/storage/emulated/0/Android/data/com.example.fyptest/files/Pictures/test001.avi", fourcc, fps, size , true);
        videoWriter = new VideoWriter(path, fourcc, fps, size , true);
//We have stated that we will use x264 as codec with FourCC
//For writing, we add the following method and it will write the image we give as parameter in this call.

        //videoWriter.open("/storage/emulated/0/Android/data/com.example.fyptest/files/Pictures/video_2.avi", VideoWriter.fourcc('h', '2','6','4'), fps, size , true);
        //videoWriter.open(path, fourcc, fps, size , true);
        //FFmpeg fFmpeg;

        //}catch (Exception e) {e.printStackTrace();}
        if (videoWriter.isOpened()) {
            Log.d("vc", "videowriter opened" + count);
            for(int i = 0; i < count+1; i++) {
                videoWriter.write(array[i]);
                Log.d("vc", "array: " + i);
            }
            videoWriter.release();
                
            try{
                
                //FFmpeg.execute("-y -i " + path + " -vcodec libwebp -filter:v fps=fps20 -lossless 1  -loop 0 -preset default -an -vsync 0 -s 320:240 "  + path2);
                int result = FFmpeg.execute("-i " + path + " -vcodec libwebp  -lossless 1  -loop 0 -preset default -an -vsync 0 -s 320:240 "  + path2);
                /*if (result == 1){
                    Toast.makeText(VideoSegmentation.this, "Save successfully! path = " + path, Toast.LENGTH_LONG).show();
                }*/
                //android.util.Log.d(VideoSegmentation.TAG, String.format("FFmpeg process exited with rc %d.", result));-c:v libwebp
            }catch (Exception e){
                e.printStackTrace();
            }
            close();


        }
        

    }


    public void close() {
        if (tflite!=null) {
            tflite.close();
            tflite = null;
            gpudelegate.close();
        }
    }
}
