package com.jejakteknisi.gradeemmc;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerActivity extends AppCompatActivity {
    PreviewView preview;
    ImageCapture capture;
    Camera camera;
    ExecutorService executor=Executors.newSingleThreadExecutor();
    TextView zoomText, focusState;
    float zoom=1f;
    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    Button b(String s){ Button x=new Button(this); x.setText(s); x.setTextColor(Color.WHITE); x.setTextSize(14); x.setAllCaps(false); x.setMinHeight(dp(52)); return x; }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setNavigationBarColor(Color.BLACK);
        FrameLayout root=new FrameLayout(this); root.setBackgroundColor(Color.BLACK); setContentView(root);

        preview=new PreviewView(this); preview.setScaleType(PreviewView.ScaleType.FILL_CENTER); root.addView(preview,new FrameLayout.LayoutParams(-1,-1));

        // Top bar
        LinearLayout top=new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(dp(8),dp(8),dp(8),dp(8)); top.setBackgroundColor(0xAA07111F);
        Button close=b("✕"); close.setOnClickListener(v->finish());
        top.addView(close,new LinearLayout.LayoutParams(dp(58),dp(58)));
        TextView tt=new TextView(this); tt.setText("Fokus Tulisan eMMC\nKetuk tulisan untuk fokus"); tt.setTextColor(Color.WHITE); tt.setTextSize(18); tt.setTypeface(null,1);
        top.addView(tt,new LinearLayout.LayoutParams(0,dp(64),1));
        Button flash=b("⚡"); top.addView(flash,new LinearLayout.LayoutParams(dp(64),dp(58)));
        FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(-1,dp(76),Gravity.TOP); root.addView(top,tp);
        flash.setOnClickListener(v->{ if(camera!=null&&camera.getCameraInfo().hasFlashUnit()) camera.getCameraControl().enableTorch(!Boolean.TRUE.equals(camera.getCameraInfo().getTorchState().getValue())); });

        // Focus brackets overlay
        TextView brackets=new TextView(this); brackets.setText("┌────────────┐\n│            │\n│     ⊕      │\n│            │\n└────────────┘"); brackets.setTextColor(0xFF00E58A); brackets.setTextSize(30); brackets.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(dp(310),dp(250),Gravity.CENTER); root.addView(brackets,bp);

        // Zoom vertical
        LinearLayout zoomBox=new LinearLayout(this); zoomBox.setOrientation(LinearLayout.VERTICAL); zoomBox.setGravity(Gravity.CENTER);
        Button plus=b("+"); zoomText=b("1×"); Button minus=b("−");
        plus.setTextSize(25); minus.setTextSize(25); zoomText.setTextSize(16);
        zoomBox.addView(plus,new LinearLayout.LayoutParams(dp(74),dp(62)));
        zoomBox.addView(zoomText,new LinearLayout.LayoutParams(dp(74),dp(58)));
        zoomBox.addView(minus,new LinearLayout.LayoutParams(dp(74),dp(62)));
        FrameLayout.LayoutParams zp=new FrameLayout.LayoutParams(dp(82),dp(190),Gravity.RIGHT|Gravity.CENTER_VERTICAL); zp.setMargins(0,0,dp(10),dp(20)); root.addView(zoomBox,zp);
        plus.setOnClickListener(v->setZoom(Math.min(8f,zoom+.5f)));
        minus.setOnClickListener(v->setZoom(Math.max(1f,zoom-.5f)));

        focusState=new TextView(this); focusState.setText("● Auto Fokus aktif"); focusState.setTextColor(Color.WHITE); focusState.setTextSize(15); focusState.setGravity(Gravity.CENTER); focusState.setBackgroundColor(0xAA006B47);
        FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(dp(260),dp(52),Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM); fp.setMargins(0,0,0,dp(145)); root.addView(focusState,fp);

        Button shutter=b("📷  FOTO & DETEKSI TULISAN"); shutter.setTextSize(17); shutter.setBackgroundColor(0xFF087FF5);
        FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-1,dp(64),Gravity.BOTTOM); sp.setMargins(dp(18),0,dp(18),dp(70)); root.addView(shutter,sp);
        shutter.setOnClickListener(v->takePhoto());

        preview.setOnTouchListener((v,e)->{
            if(e.getAction()==MotionEvent.ACTION_UP) focusAt(e.getX(),e.getY());
            return true;
        });
        startCamera();
    }
    void setZoom(float z){zoom=z;if(camera!=null)camera.getCameraControl().setLinearZoom((z-1f)/7f);zoomText.setText(String.format(Locale.US,"%.1f×",z));}
    void focusAt(float x,float y){
        if(camera==null)return;
        MeteringPointFactory f=preview.getMeteringPointFactory();
        FocusMeteringAction a=new FocusMeteringAction.Builder(f.createPoint(x,y),FocusMeteringAction.FLAG_AF)
            .setAutoCancelDuration(3,java.util.concurrent.TimeUnit.SECONDS).build();
        camera.getCameraControl().startFocusAndMetering(a);
        focusState.setText("✓ Fokus dikunci pada tulisan");
    }
    void startCamera(){
        ListenableFuture<ProcessCameraProvider> future=ProcessCameraProvider.getInstance(this);
        future.addListener(()->{
            try{
                ProcessCameraProvider p=future.get();
                Preview pv=new Preview.Builder().build(); pv.setSurfaceProvider(preview.getSurfaceProvider());
                capture=new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build();
                CameraSelector sel=CameraSelector.DEFAULT_BACK_CAMERA;
                p.unbindAll(); camera=p.bindToLifecycle(this,sel,pv,capture);
                setZoom(zoom);
                focusAt(preview.getWidth()/2f,preview.getHeight()/2f);
            }catch(Exception e){ Toast.makeText(this,"Kamera gagal dibuka: "+e.getMessage(),Toast.LENGTH_LONG).show(); }
        },ContextCompat.getMainExecutor(this));
    }
    void takePhoto(){
        if(capture==null)return;
        focusState.setText("● Memfokuskan tulisan...");
        focusAt(preview.getWidth()/2f,preview.getHeight()/2f);
        new Handler().postDelayed(()->{
            ImageCapture.OutputFileOptions o=new ImageCapture.OutputFileOptions.Builder(createTempFile()).build();
            capture.takePicture(o,executor,new ImageCapture.OnImageSavedCallback(){
                @Override public void onImageSaved(@NonNull ImageCapture.OutputFileResults r){ runOnUiThread(()->processOCR(o)); }
                @Override public void onError(@NonNull ImageCaptureException e){runOnUiThread(()->Toast.makeText(ScannerActivity.this,"Gagal foto: "+e.getMessage(),Toast.LENGTH_LONG).show());}
            });
        },450);
    }
    java.io.File lastFile;
    java.io.File createTempFile(){
        try{ lastFile=java.io.File.createTempFile("emmc_scan_",".jpg",getCacheDir()); return lastFile; }
        catch(Exception e){throw new RuntimeException(e);}
    }
    void processOCR(ImageCapture.OutputFileOptions o){
        if(lastFile==null || !lastFile.exists()){Toast.makeText(this,"Foto tersimpan tetapi file tidak ditemukan",Toast.LENGTH_SHORT).show();return;}
        try{
            InputImage img=InputImage.fromFilePath(this,android.net.Uri.fromFile(lastFile));
            TextRecognizer rec=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            rec.process(img).addOnSuccessListener(result->showOCR(result.getText())).addOnFailureListener(e->Toast.makeText(this,"OCR gagal: "+e.getMessage(),Toast.LENGTH_LONG).show());
        }catch(Exception e){Toast.makeText(this,"Tidak dapat membaca foto: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }
    void showOCR(String text){
        final EditText edit=new EditText(this); edit.setText(text); edit.setTextColor(Color.WHITE); edit.setTextSize(18); edit.setGravity(Gravity.TOP); edit.setMinLines(6);
        edit.setSelectAllOnFocus(false);
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(10),dp(5),dp(10),0);
        TextView hint=new TextView(this); hint.setText("Pilih/edit kode eMMC yang ingin dicari"); hint.setTextColor(0xFF9FB2C8);
        box.addView(hint); box.addView(edit,new LinearLayout.LayoutParams(-1,dp(180)));
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("Hasil Deteksi Tulisan").setView(box)
            .setNegativeButton("Tutup",null).create();
        dlg.setButton(AlertDialog.BUTTON_NEUTRAL,"COPY",(d,w)->{
            ((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(android.content.ClipData.newPlainText("eMMC",edit.getText().toString()));
            Toast.makeText(this,"Tulisan disalin",Toast.LENGTH_SHORT).show();
        });
        dlg.setButton(AlertDialog.BUTTON_POSITIVE,"CARI",(d,w)->searchLocalOrWeb(edit.getText().toString()));
        dlg.show();
    }
    void searchLocalOrWeb(String q){
        String clean=q.trim().replaceAll("\\s+"," ");
        ArrayList<EmmcRecord> list=DatabaseStore.load(this);
        EmmcRecord found=null;
        for(EmmcRecord e:list) if(clean.toUpperCase(Locale.US).contains(e.code.toUpperCase(Locale.US)) || e.code.equalsIgnoreCase(clean)){found=e;break;}
        if(found!=null){
            new AlertDialog.Builder(this).setTitle(found.code)
                .setMessage("Manufacturer: "+found.manufacturer+"\nKapasitas: "+found.capacity+"\neMMC Version: "+found.version+
                    "\nGrade: "+(found.grade.isEmpty()?"-":found.grade)+"\nPackage: "+found.pack+"\nSumber: "+found.source)
                .setPositiveButton("EDIT",null).setNegativeButton("OK",null).show();
        }else{
            String url="https://www.google.com/search?q="+android.net.Uri.encode(clean+" eMMC datasheet capacity grade");
            startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(url)));
        }
    }
    @Override protected void onDestroy(){super.onDestroy();executor.shutdown();}
}
