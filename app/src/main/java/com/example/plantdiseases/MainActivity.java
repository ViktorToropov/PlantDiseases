package com.example.plantdiseases;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    public static final String LOG_TAG = "myLogs";

    CameraService[] myCameras = null;

    private CameraManager mCameraManager    = null;
    private final int CAMERA1 = 0;
    private final int CAMERA2 = 1;

    private Button mButtonOpenCamera1 = null;
    private Button mButtonOpenCamera2 = null;
    private Button mButtonToMakeShot = null;
    private Button mButtonToMakeBlackWhite = null;
    private TextureView mImageView = null;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler = null;

    int stage = 0;
    Bitmap sBW;//черно белое изображение
    Bitmap chosenGraphBitmap;//график черно белого изображения
    private SeekBar seekBar = null;
    private Button mButtonGetProgressBar = null;
    private Button mButtonNext = null;
    private Button mButtonBack = null;
    private ImageView imageView1 = null;
    private ImageView imageView2 = null;

    Bitmap chosenBitmap;//выделенное изображение
    Bitmap sColor;//цветное изображение
    private SeekBar seekBar1 = null;
    private SeekBar seekBar2 = null;
    private Button mButtonGetProgressBar1 = null;
    private Button mButtonBack1 = null;
    private ImageView imageView3 = null;
    private ImageView imageView4 = null;
    private TextView textView = null;
    int checkPixels=0;
    int corrPixels=0;

    private ImageView imageView5 = null;
    private TextView textView2 = null;
    private Button mButtonChangeDescription = null;
    private Button mButtonBack2 = null;
    private Button mButtonNextImg2 = null;
    private Button mButtonPrevImg2 = null;
    private EditText mEditText = null;
    private int currentNimberImg = 0;
    private Button mButtonDelImg = null;

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Log.d(LOG_TAG, "Запрашиваем разрешение");
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ||
                (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        )
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }


        mButtonOpenCamera1 = findViewById(R.id.button1);
        mButtonOpenCamera2 = findViewById(R.id.button2);
        mButtonToMakeShot = findViewById(R.id.button3);
        mButtonToMakeBlackWhite =findViewById(R.id.button4);
        mImageView = findViewById(R.id.textureView);

        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        seekBar = findViewById(R.id.seekBar);
        mButtonGetProgressBar = findViewById(R.id.button5);
        mButtonNext = findViewById(R.id.button6);
        mButtonBack = findViewById(R.id.button7);

        seekBar1 = findViewById(R.id.seekBar1);
        seekBar2 = findViewById(R.id.seekBar2);
        mButtonGetProgressBar1 = findViewById(R.id.button8);
        mButtonBack1 = findViewById(R.id.button9);
        imageView3 = findViewById(R.id.imageView3);
        imageView4 = findViewById(R.id.imageView4);
        textView = findViewById(R.id.textView);

        imageView5 = findViewById(R.id.imageView5);
        textView2 = findViewById(R.id.textView2);
        mButtonChangeDescription = findViewById(R.id.button17);
        mButtonBack2 = findViewById(R.id.button18);
        mButtonNextImg2 = findViewById(R.id.button19);
        mButtonPrevImg2 = findViewById(R.id.button20);
        mButtonDelImg = findViewById(R.id.button21);
        mEditText = findViewById(R.id.editTextTextMultiLine);

        mButtonOpenCamera1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageView.setVisibility(View.VISIBLE);
                if (myCameras[CAMERA2].isOpen()) {myCameras[CAMERA2].closeCamera();}
                if (myCameras[CAMERA1].isOpen()) {myCameras[CAMERA1].closeCamera();}
                if (myCameras[CAMERA1] != null) {
                    if (!myCameras[CAMERA1].isOpen()) myCameras[CAMERA1].openCamera();
                }
            }
        });
        mButtonOpenCamera2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!getCurrentPic1().equals("0")) {

                    mButtonOpenCamera1.setVisibility(View.INVISIBLE);
                    mButtonOpenCamera2.setVisibility(View.INVISIBLE);
                    mButtonToMakeShot.setVisibility(View.INVISIBLE);
                    mButtonToMakeBlackWhite.setVisibility(View.INVISIBLE);
                    mImageView.setVisibility(View.INVISIBLE);

                    imageView5.setVisibility(View.VISIBLE);
                    textView2.setVisibility(View.VISIBLE);
                    mButtonChangeDescription.setVisibility(View.VISIBLE);
                    mButtonBack2.setVisibility(View.VISIBLE);
                    mButtonNextImg2.setVisibility(View.VISIBLE);
                    mButtonPrevImg2.setVisibility(View.VISIBLE);
                    mButtonDelImg.setVisibility(View.VISIBLE);
                    mEditText.setVisibility(View.VISIBLE);

                    // Открываем изображение
                    File f2 = new File(getExternalFilesDir(null), getCurrentPic1());
                    Bitmap sBW1;//черно белое изображение
                    sBW1 = BitmapFactory.decodeFile(f2.getAbsolutePath());
                    imageView5.setImageBitmap(sBW1);

                    File f4 = new File(getExternalFilesDir(null), getCurrentPic1().substring(0,getCurrentPic1().length()-3)+"txt");
                    ArrayList<String> arrayList = new ArrayList<>();
                    String s="";

                    String line = null;
                    try(BufferedReader br = new BufferedReader(new FileReader(f4))) {
                        while((line = br.readLine()) != null){
                            arrayList.add(line);
                        }
                    } catch (FileNotFoundException e) {

                    } catch (IOException e) {

                    }
                    for (int i=0;i<arrayList.size();i++)s+=arrayList.get(i)+"\n";
                    textView2.setText(s);



                }



            }
        });
        mButtonToMakeShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (myCameras[CAMERA1].isOpen()) myCameras[CAMERA1].makePhoto();
                if (myCameras[CAMERA2].isOpen()) myCameras[CAMERA2].makePhoto();
            }
        });
        mButtonToMakeBlackWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                stage0();
            }
        });
        mButtonGetProgressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                getRedPix();
            }
        });
        mButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                backToStart();
            }
        });
        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                goNext();
            }
        });
        mButtonBack1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                goBack1();
            }
        });
        mButtonGetProgressBar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                getPicturePix();
            }
        });

        mButtonBack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView5.setVisibility(View.INVISIBLE);
                textView2.setVisibility(View.INVISIBLE);
                mButtonChangeDescription.setVisibility(View.INVISIBLE);
                mButtonBack2.setVisibility(View.INVISIBLE);
                mButtonNextImg2.setVisibility(View.INVISIBLE);
                mButtonPrevImg2.setVisibility(View.INVISIBLE);
                mButtonDelImg.setVisibility(View.INVISIBLE);
                mEditText.setVisibility(View.INVISIBLE);

                mButtonOpenCamera1.setVisibility(View.VISIBLE);
                mButtonOpenCamera2.setVisibility(View.VISIBLE);
                mButtonToMakeShot.setVisibility(View.VISIBLE);
                mButtonToMakeBlackWhite.setVisibility(View.VISIBLE);
                mImageView.setVisibility(View.VISIBLE);

                //  currentNimberImg = 0;
            }
        });
        mButtonNextImg2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getCurrentPic1().equals("0")) {
                    currentNimberImg++;
                    // Открываем изображение
                    File f3 = new File(getExternalFilesDir(null), getCurrentPic1());
                    Bitmap sBW2;//черно белое изображение
                    sBW2 = BitmapFactory.decodeFile(f3.getAbsolutePath());
                    imageView5.setImageBitmap(sBW2);

                    File f4 = new File(getExternalFilesDir(null), getCurrentPic1().substring(0,getCurrentPic1().length()-3)+"txt");
                    ArrayList<String> arrayList = new ArrayList<>();
                    String s="";

                    String line = null;
                    try(BufferedReader br = new BufferedReader(new FileReader(f4))) {
                        while((line = br.readLine()) != null){
                            arrayList.add(line);
                        }
                    } catch (FileNotFoundException e) {

                    } catch (IOException e) {

                    }
                    for (int i=0;i<arrayList.size();i++)s+=arrayList.get(i)+"\n";
                    textView2.setText(s);


                }
            }
        });
        mButtonPrevImg2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getCurrentPic1().equals("0")) {
                    currentNimberImg--;
                    // Открываем изображение
                    File f3 = new File(getExternalFilesDir(null), getCurrentPic1());
                    Bitmap sBW2;//черно белое изображение
                    sBW2 = BitmapFactory.decodeFile(f3.getAbsolutePath());
                    imageView5.setImageBitmap(sBW2);

                    File f4 = new File(getExternalFilesDir(null), getCurrentPic1().substring(0,getCurrentPic1().length()-3)+"txt");
                    ArrayList<String> arrayList = new ArrayList<>();
                    String s="";

                    String line = null;
                    try(BufferedReader br = new BufferedReader(new FileReader(f4))) {
                        while((line = br.readLine()) != null){
                            arrayList.add(line);
                        }
                    } catch (FileNotFoundException e) {

                    } catch (IOException e) {

                    }
                    for (int i=0;i<arrayList.size();i++)s+=arrayList.get(i)+"\n";
                    textView2.setText(s);

                }
            }
        });
        mButtonChangeDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getCurrentPic1().equals("0")) {


                    File f4 = new File(getExternalFilesDir(null), getCurrentPic1().substring(0,getCurrentPic1().length()-3)+"txt");
                    ArrayList<String> arrayList = new ArrayList<>();

                    String line = null;
                    try(BufferedReader br = new BufferedReader(new FileReader(f4))) {
                        while((line = br.readLine()) != null){
                            arrayList.add(line);
                        }
                        arrayList.set(3,mEditText.getText().toString());
                    } catch (FileNotFoundException e) {

                    } catch (IOException e) {

                    }

                    FileWriter writer;
                    String s="";
                    for(int i=0;i<4;i++)s+=arrayList.get(i)+"\n";

                    try {
                        writer = new FileWriter(f4);
                        writer.append(s);
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    textView2.setText(s);

                }
            }
        });
        mButtonDelImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getCurrentPic1().equals("0")) {


                    File f4 = new File(String.valueOf(getExternalFilesDir(null)), "images.txt");
                    ArrayList<String> arrayList = new ArrayList<>();

                    String line = null;
                    try(BufferedReader br = new BufferedReader(new FileReader(f4))) {
                        while((line = br.readLine()) != null){
                            arrayList.add(line);
                        }

                    } catch (FileNotFoundException e) {

                    } catch (IOException e) {

                    }
                    //удаляем изображение
                    File f1 = new File(getExternalFilesDir(null), arrayList.get(arrayList.size()-(1+currentNimberImg)));
                    f1.delete();
                    //удаляем текстовый файл изображения
                    f1 = new File(getExternalFilesDir(null), arrayList.get(arrayList.size()-(1+currentNimberImg)).substring(0,arrayList.get(arrayList.size()-(1+currentNimberImg)).length()-3)+"txt");
                    f1.delete();
                    //удаляем информацию из текстового файла изображений
                    arrayList.remove(arrayList.size()-(1+currentNimberImg));

                    FileWriter writer;
                    String s="";
                    for(int i=0;i<arrayList.size();i++)
                        if(i==0)
                            s+=arrayList.get(i);
                        else s+="\n"+arrayList.get(i);

                    try {
                        writer = new FileWriter(f4);
                        writer.append(s);
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    imageView5.setVisibility(View.INVISIBLE);
                    textView2.setVisibility(View.INVISIBLE);
                    mButtonChangeDescription.setVisibility(View.INVISIBLE);
                    mButtonBack2.setVisibility(View.INVISIBLE);
                    mButtonNextImg2.setVisibility(View.INVISIBLE);
                    mButtonPrevImg2.setVisibility(View.INVISIBLE);
                    mButtonDelImg.setVisibility(View.INVISIBLE);
                    mEditText.setVisibility(View.INVISIBLE);

                    mButtonOpenCamera1.setVisibility(View.VISIBLE);
                    mButtonOpenCamera2.setVisibility(View.VISIBLE);
                    mButtonToMakeShot.setVisibility(View.VISIBLE);
                    mButtonToMakeBlackWhite.setVisibility(View.VISIBLE);
                    mImageView.setVisibility(View.VISIBLE);

                }
            }
        });

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            // Получение списка камер с устройства
            myCameras = new CameraService[mCameraManager.getCameraIdList().length];
            for (String cameraID : mCameraManager.getCameraIdList()) {
                Log.i(LOG_TAG, "cameraID: "+cameraID);
                int id = Integer.parseInt(cameraID);
                // создаем обработчик для камеры
                myCameras[id] = new CameraService(mCameraManager,cameraID);
            }
        }
        catch(CameraAccessException e){
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public class CameraService {

        private String mCameraID;
        private CameraDevice mCameraDevice = null;
        private CameraCaptureSession mCaptureSession;
        private ImageReader mImageReader;

        public CameraService(CameraManager cameraManager, String cameraID) {
            mCameraManager = cameraManager;
            mCameraID = cameraID;
        }

        public void makePhoto (){

            try {
                // This is the CaptureRequest.Builder that we use to take a picture.
                final CaptureRequest.Builder captureBuilder =
                        mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(mImageReader.getSurface());
                CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {

                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                   @NonNull CaptureRequest request,
                                                   @NonNull TotalCaptureResult result) {
                    }
                };
                mCaptureSession.stopRepeating();
                mCaptureSession.abortCaptures();
                mCaptureSession.capture(captureBuilder.build(), CaptureCallback, mBackgroundHandler);
            }
            catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
                = new ImageReader.OnImageAvailableListener() {

            @Override
            public void onImageAvailable(ImageReader reader) {
                File mFile = new File(getExternalFilesDir(null), getRandomName()+".jpg");
                mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
                imgSaveOnTxt1(mFile);
                txtSaveOnFolder1(mFile);
                currentNimberImg = 0;
            }

        };

        private CameraDevice.StateCallback mCameraCallback = new CameraDevice.StateCallback() {

            @Override
            public void onOpened(CameraDevice camera) {
                mCameraDevice = camera;
                Log.i(LOG_TAG, "Open camera  with id:"+mCameraDevice.getId());
                createCameraPreviewSession();
            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                mCameraDevice.close();

                Log.i(LOG_TAG, "disconnect camera  with id:"+mCameraDevice.getId());
                mCameraDevice = null;
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                Log.i(LOG_TAG, "error! camera id:"+camera.getId()+" error:"+error);
            }
        };


        private void createCameraPreviewSession() {

            mImageReader = ImageReader.newInstance(480,320, ImageFormat.JPEG,1);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);

            SurfaceTexture texture = mImageView.getSurfaceTexture();

            texture.setDefaultBufferSize(480,320);
            Surface surface = new Surface(texture);

            try {
                final CaptureRequest.Builder builder =
                        mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                builder.addTarget(surface);
                mCameraDevice.createCaptureSession(Arrays.asList(surface,mImageReader.getSurface()),
                        new CameraCaptureSession.StateCallback() {

                            @Override
                            public void onConfigured(CameraCaptureSession session) {
                                mCaptureSession = session;
                                try {
                                    mCaptureSession.setRepeatingRequest(builder.build(),null,mBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(CameraCaptureSession session) { }}, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        public boolean isOpen() {
            if (mCameraDevice == null) {
                return false;
            } else {
                return true;
            }
        }

        public void openCamera() {
            try {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    mCameraManager.openCamera(mCameraID,mCameraCallback,mBackgroundHandler);
                }
            } catch (CameraAccessException e) {
                Log.i(LOG_TAG,e.getMessage());
            }
        }

        public void closeCamera() {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }
    }


    @Override
    public void onPause() {
        if(myCameras[CAMERA1].isOpen()){myCameras[CAMERA1].closeCamera();}
        if(myCameras[CAMERA2].isOpen()){myCameras[CAMERA2].closeCamera();}
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
    }


    private static class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public void stage0(){
        if ((stage==0)&&(!getCurrentPic1().equals("0"))){
            seekBar.setVisibility(View.VISIBLE);
            mButtonGetProgressBar.setVisibility(View.VISIBLE);
            mButtonNext.setVisibility(View.VISIBLE);
            mButtonBack.setVisibility(View.VISIBLE);
            imageView1.setVisibility(View.VISIBLE);
            imageView2.setVisibility(View.VISIBLE);

            // Открываем изображение
            File f = new File(getExternalFilesDir(null), getCurrentPic1());
            sColor = BitmapFactory.decodeFile(f.getAbsolutePath());
         //   mButtonOpenCamera1.setText(getExternalFilesDir(null).toString());
            mButtonOpenCamera1.setVisibility(View.INVISIBLE);
            mButtonOpenCamera2.setVisibility(View.INVISIBLE);
            mButtonToMakeShot.setVisibility(View.INVISIBLE);
            mButtonToMakeBlackWhite.setVisibility(View.INVISIBLE);
            mImageView.setVisibility(View.INVISIBLE);

            //делаем серую картинку
            sBW = BitmapFactory.decodeFile(f.getAbsolutePath());
            int[] graphCells = new int[256];
            sBW = sBW.copy(Bitmap.Config.ARGB_8888,true);
            for (int i=0;i<sBW.getWidth();i++)
                for (int j=0;j<sBW.getHeight();j++){
                    int red = Color.red(sBW.getPixel(i,j));
                    int green = Color.green(sBW.getPixel(i,j));
                    int blue = Color.blue(sBW.getPixel(i,j));
                    int grey = (int) ((red + green + blue)/3d);
                    sBW.setPixel(i,j,Color.argb(255,grey,grey,grey));
                    graphCells[grey]++;
                }
            imageView1.setImageBitmap(sBW);
             //рисуем график
            int w = 256,h = 200;
            chosenGraphBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            chosenGraphBitmap.eraseColor(Color.BLACK);
            int maxCel=0;
            for (int i =0;i<graphCells.length;i++)
                if(graphCells[i]>maxCel)maxCel=graphCells[i];

            for (int i=0;i<w;i++)
                if(maxCel>0)
                    for (int j=0;j<graphCells[i]/(maxCel/(double)h);j++)//cells[i]/(maxCel/h)
                        chosenGraphBitmap.setPixel(i,h-1-j,Color.WHITE);
            imageView2.setImageBitmap(chosenGraphBitmap);



        }
    }
    public void getRedPix(){
        checkPixels = 0;
        Bitmap chosenPixmapRed = sBW.copy(Bitmap.Config.ARGB_8888,true);
        chosenBitmap = Bitmap.createBitmap(chosenPixmapRed.getWidth(), chosenPixmapRed.getHeight(), Bitmap.Config.ARGB_8888);
        for (int i=0;i<chosenPixmapRed.getWidth();i++)
            for (int j=0;j<chosenPixmapRed.getHeight();j++)
                if(Color.red(chosenPixmapRed.getPixel(i,j))<seekBar.getProgress()){
                    chosenBitmap.setPixel(i,j,sColor.getPixel(i,j));
                    chosenPixmapRed.setPixel(i,j,Color.RED);
                    checkPixels++;
                }
        imageView3.setImageBitmap(chosenBitmap);

        imageView4.setVisibility(View.INVISIBLE);
        imageView4.setImageBitmap(chosenBitmap);

        imageView1.setImageBitmap(chosenPixmapRed);

        Bitmap chosenGraphPixmapRed = chosenGraphBitmap.copy(Bitmap.Config.ARGB_8888,true);
        for (int i=0;i<seekBar.getProgress();i++)
            for (int j=0;j<chosenGraphPixmapRed.getHeight();j++)
                if(chosenGraphPixmapRed.getPixel(i,j)==Color.WHITE)
                    chosenGraphPixmapRed.setPixel(i,j,Color.RED);
        imageView2.setImageBitmap(chosenGraphPixmapRed);
    }
    public void backToStart(){
        seekBar.setVisibility(View.INVISIBLE);
        mButtonGetProgressBar.setVisibility(View.INVISIBLE);
        mButtonNext.setVisibility(View.INVISIBLE);
        mButtonBack.setVisibility(View.INVISIBLE);
        imageView1.setVisibility(View.INVISIBLE);
        imageView2.setVisibility(View.INVISIBLE);

        mButtonOpenCamera1.setVisibility(View.VISIBLE);
        mButtonOpenCamera2.setVisibility(View.VISIBLE);
        mButtonToMakeShot.setVisibility(View.VISIBLE);
        mButtonToMakeBlackWhite.setVisibility(View.VISIBLE);
    }
    public void goNext(){
        seekBar.setVisibility(View.INVISIBLE);
        mButtonGetProgressBar.setVisibility(View.INVISIBLE);
        mButtonNext.setVisibility(View.INVISIBLE);
        mButtonBack.setVisibility(View.INVISIBLE);
        imageView1.setVisibility(View.INVISIBLE);
        imageView2.setVisibility(View.INVISIBLE);

        seekBar1.setVisibility(View.VISIBLE);
        seekBar2.setVisibility(View.VISIBLE);
        mButtonGetProgressBar1.setVisibility(View.VISIBLE);
        mButtonBack1.setVisibility(View.VISIBLE);
        imageView3.setVisibility(View.VISIBLE);
        imageView4.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
    }
    public void goBack1(){
        seekBar1.setVisibility(View.INVISIBLE);
        seekBar2.setVisibility(View.INVISIBLE);
        mButtonGetProgressBar1.setVisibility(View.INVISIBLE);
        mButtonBack1.setVisibility(View.INVISIBLE);
        imageView3.setVisibility(View.INVISIBLE);
        imageView4.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);

        seekBar.setVisibility(View.VISIBLE);
        mButtonGetProgressBar.setVisibility(View.VISIBLE);
        mButtonNext.setVisibility(View.VISIBLE);
        mButtonBack.setVisibility(View.VISIBLE);
        imageView1.setVisibility(View.VISIBLE);
        imageView2.setVisibility(View.VISIBLE);
    }
    public void getPicturePix(){
        corrPixels = 0;
        Bitmap finalPixmapRed = chosenBitmap.copy(Bitmap.Config.ARGB_8888,true);
        for (int i = 0; i < finalPixmapRed.getWidth(); i++)
            for (int j = 0; j < finalPixmapRed.getHeight(); j++){
                if(finalPixmapRed.getPixel(i,j)!=0){
                    if(Color.red(finalPixmapRed.getPixel(i,j))<seekBar1.getProgress()){
                        finalPixmapRed.setPixel(i,j,0);
                        corrPixels++;
                    }else
                    if(Color.blue(finalPixmapRed.getPixel(i,j))<seekBar2.getProgress()){
                        finalPixmapRed.setPixel(i,j,0);
                        corrPixels++;
                    }else
                    if(Color.green(finalPixmapRed.getPixel(i,j))>=Color.red(finalPixmapRed.getPixel(i,j))&&Color.green(finalPixmapRed.getPixel(i,j))>=Color.blue(finalPixmapRed.getPixel(i,j))){
                        finalPixmapRed.setPixel(i,j,0);
                        corrPixels++;
                    }
                }
            }
        imageView4.setImageBitmap(finalPixmapRed);
        textView.setText("Поражено "+(checkPixels-corrPixels)+" Всего выделено " + checkPixels + " Процент поражения "+(int)(Math.round((checkPixels-corrPixels)/(double)checkPixels*100d))+" %");

        File f4 = new File(getExternalFilesDir(null), getCurrentPic1().substring(0,getCurrentPic1().length()-3)+"txt");
        ArrayList<String> arrayList = new ArrayList<>();

        String line = null;
        try(BufferedReader br = new BufferedReader(new FileReader(f4))) {
            while((line = br.readLine()) != null){
                arrayList.add(line);
            }
            arrayList.set(2,"Процент поражения "+(int)(Math.round((checkPixels-corrPixels)/(double)checkPixels*100d))+" %");
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        FileWriter writer;
        String s="";
        for(int i=0;i<arrayList.size();i++)s+=arrayList.get(i)+"\n";

        try {
            writer = new FileWriter(f4);
            writer.append(s);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    String getCurrentPic1(){

        File gpxfile = new File(String.valueOf(getExternalFilesDir(null)), "images.txt");
        ArrayList<String> arrayList = new ArrayList<>();

        String line = null;
        try(BufferedReader br = new BufferedReader(new FileReader(gpxfile))) {
            while((line = br.readLine()) != null){
                arrayList.add(line);
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
        if(currentNimberImg<0)currentNimberImg=0;
        if(currentNimberImg>(arrayList.size()-1))currentNimberImg=arrayList.size()-1;

        if(arrayList.size()>0)
            return arrayList.get(arrayList.size()-(1+currentNimberImg));
        else return "0";
    }

    void imgSaveOnTxt1(File file){
        File gpxfile = new File(String.valueOf(getExternalFilesDir(null)), "images.txt");
        FileReader reader = null;
        FileWriter writer = null;
        String s="";

        try {
            reader = new FileReader(gpxfile);
            int i;
            while((i=reader.read())!=-1)
                s+=(char)i;
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            writer = new FileWriter(gpxfile);
            //  writer.append(s+"\n"+file.getName()+"\n"+getLastPic1());
            if(s.equals(""))
                writer.append(s+file.getName());
            else writer.append(s+"\n"+file.getName());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    String getRandomName(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime+=System.currentTimeMillis();
    }

    void txtSaveOnFolder1(File file){
        File gpxfile = new File(String.valueOf(getExternalFilesDir(null)), file.getName().substring(0,file.getName().length()-3)+"txt");
        FileWriter writer;
        String s;
        s="Дата создания фото "+ new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())+"\n";
        s+="Время создания фото "+ new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date())+"\n"+"\n"+"Введите описание";

        try {
            writer = new FileWriter(gpxfile);
            writer.append(s);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}