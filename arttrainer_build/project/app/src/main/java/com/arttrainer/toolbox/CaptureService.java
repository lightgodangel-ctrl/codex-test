package com.arttrainer.toolbox;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class CaptureService extends Service {
    public static final String ACTION_START="com.arttrainer.toolbox.capture.START", ACTION_SNAPSHOT="com.arttrainer.toolbox.capture.SNAPSHOT", ACTION_STOP="com.arttrainer.toolbox.capture.STOP";
    public static final String EXTRA_RESULT_CODE="resultCode", EXTRA_RESULT_DATA="resultData", EXTRA_PREVIEW_MODE="previewMode";
    private static final int NOTIFICATION_ID=9102; private static final String CHANNEL_ID="arttrainer_capture"; private static volatile boolean running=false;
    private MediaProjection projection; private VirtualDisplay virtualDisplay; private ImageReader imageReader; private HandlerThread workerThread; private Handler worker; private final AtomicBoolean snapshotRequested=new AtomicBoolean(false); private volatile String pendingPreviewMode="original";
    public static boolean isRunning(){return running;}
    @Override public void onCreate(){super.onCreate();createNotificationChannel();workerThread=new HandlerThread("ArtTrainerCapture");workerThread.start();worker=new Handler(workerThread.getLooper());}
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent==null)return START_NOT_STICKY; String action=intent.getAction();
        if(ACTION_STOP.equals(action)){stopProjection();stopSelf();return START_NOT_STICKY;}
        if(ACTION_SNAPSHOT.equals(action)){pendingPreviewMode=intent.getStringExtra(EXTRA_PREVIEW_MODE);if(pendingPreviewMode==null)pendingPreviewMode="original";if(running)snapshotRequested.set(true);else restoreBubble();return START_NOT_STICKY;}
        if(ACTION_START.equals(action)){pendingPreviewMode=intent.getStringExtra(EXTRA_PREVIEW_MODE);if(pendingPreviewMode==null)pendingPreviewMode="original";startAsForeground();if(running){snapshotRequested.set(true);return START_NOT_STICKY;}int resultCode=intent.getIntExtra(EXTRA_RESULT_CODE,0);Intent resultData=(Intent)intent.getParcelableExtra(EXTRA_RESULT_DATA);if(resultCode!=-1||resultData==null){restoreBubble();stopSelf();return START_NOT_STICKY;}try{startProjection(resultCode,resultData);}catch(Throwable t){restoreBubble();stopProjection();stopSelf();}}
        return START_NOT_STICKY;
    }
    private void startAsForeground(){Notification n=buildNotification();if(Build.VERSION.SDK_INT>=34)startForeground(NOTIFICATION_ID,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);else startForeground(NOTIFICATION_ID,n);}
    private void startProjection(int resultCode,Intent resultData){
        MediaProjectionManager mgr=(MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);if(mgr==null)throw new IllegalStateException();projection=mgr.getMediaProjection(resultCode,resultData);if(projection==null)throw new IllegalStateException();
        projection.registerCallback(new MediaProjection.Callback(){@Override public void onStop(){running=false;cleanupDisplayOnly();restoreBubble();stopSelf();}},new Handler(getMainLooper()));
        DisplayMetrics dm=new DisplayMetrics();WindowManager wm=(WindowManager)getSystemService(WINDOW_SERVICE);if(wm==null)throw new IllegalStateException();wm.getDefaultDisplay().getRealMetrics(dm);
        imageReader=ImageReader.newInstance(dm.widthPixels,dm.heightPixels,PixelFormat.RGBA_8888,2);imageReader.setOnImageAvailableListener(this::onImageAvailable,worker);
        virtualDisplay=projection.createVirtualDisplay("ArtTrainerCapture",dm.widthPixels,dm.heightPixels,dm.densityDpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,imageReader.getSurface(),null,worker);running=true;worker.postDelayed(()->snapshotRequested.set(true),400);
    }
    private void onImageAvailable(ImageReader reader){Image image=null;try{image=reader.acquireLatestImage();if(image==null)return;if(!snapshotRequested.getAndSet(false))return;Image.Plane plane=image.getPlanes()[0];ByteBuffer buffer=plane.getBuffer();int pixelStride=plane.getPixelStride(),rowStride=plane.getRowStride(),rowPadding=rowStride-pixelStride*image.getWidth(),paddedWidth=image.getWidth()+rowPadding/pixelStride;Bitmap padded=Bitmap.createBitmap(paddedWidth,image.getHeight(),Bitmap.Config.ARGB_8888);padded.copyPixelsFromBuffer(buffer);Bitmap cropped=Bitmap.createBitmap(padded,0,0,image.getWidth(),image.getHeight());if(cropped!=padded)padded.recycle();File out=new File(getCacheDir(),"arttrainer_capture.png");try(FileOutputStream fos=new FileOutputStream(out)){cropped.compress(Bitmap.CompressFormat.PNG,100,fos);}cropped.recycle();Intent show=new Intent(this,OverlayService.class);show.setAction(OverlayService.ACTION_SHOW_PREVIEW);show.putExtra(OverlayService.EXTRA_CAPTURE_PATH,out.getAbsolutePath());show.putExtra(OverlayService.EXTRA_PREVIEW_MODE,pendingPreviewMode);startService(show);}catch(Throwable t){restoreBubble();}finally{if(image!=null)image.close();}}
    private Notification buildNotification(){Intent stop=new Intent(this,CaptureService.class);stop.setAction(ACTION_STOP);PendingIntent stopPi=PendingIntent.getService(this,2,stop,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);Intent open=new Intent(this,MainActivity.class);PendingIntent openPi=PendingIntent.getActivity(this,3,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL_ID):new Notification.Builder(this);return b.setSmallIcon(R.drawable.ic_arttrainer).setContentTitle("ArtTrainer 화면 분석").setContentText("캡처 세션 실행 중").setContentIntent(openPi).setOngoing(true).addAction(new Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel,"캡처 종료",stopPi).build()).build();}
    private void createNotificationChannel(){if(Build.VERSION.SDK_INT<26)return;NotificationChannel ch=new NotificationChannel(CHANNEL_ID,"ArtTrainer 화면 분석",NotificationManager.IMPORTANCE_LOW);NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);if(nm!=null)nm.createNotificationChannel(ch);}
    private void restoreBubble(){try{Intent i=new Intent(this,OverlayService.class);i.setAction(OverlayService.ACTION_SHOW_BUBBLE);startService(i);}catch(Exception ignored){}}
    private void stopProjection(){running=false;cleanupDisplayOnly();if(projection!=null){try{projection.stop();}catch(Exception ignored){}projection=null;}stopForeground(true);}
    private void cleanupDisplayOnly(){if(virtualDisplay!=null){try{virtualDisplay.release();}catch(Exception ignored){}virtualDisplay=null;}if(imageReader!=null){try{imageReader.close();}catch(Exception ignored){}imageReader=null;}}
    @Override public void onDestroy(){stopProjection();if(workerThread!=null){workerThread.quitSafely();workerThread=null;}super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}
