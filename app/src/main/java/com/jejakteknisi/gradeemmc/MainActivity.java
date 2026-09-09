package com.jejakteknisi.gradeemmc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {
    static final int VOICE_REQ = 80;
    LinearLayout root;
    int dp(float v){ return (int)(v*getResources().getDisplayMetrics().density+0.5f); }

    TextView title(String s, float size){
        TextView t=new TextView(this); t.setText(s); t.setTextColor(0xFFFFFFFF); t.setTextSize(size);
        t.setTypeface(null,1); t.setPadding(dp(8),dp(6),dp(8),dp(6)); return t;
    }
    Button button(String text){
        Button b=new Button(this); b.setText(text); b.setTextColor(0xFFFFFFFF);
        b.setTextSize(14); b.setAllCaps(false); b.setMinHeight(dp(52));
        b.setBackgroundColor(0xFF087FF5); return b;
    }
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},91);
        buildHome();
    }
    void buildHome(){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(16),dp(16),dp(16),dp(12));
        root.setBackgroundColor(0xFF07111F);
        setContentView(root);

        LinearLayout head=new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL);
        TextView gear=title("⚙",30); head.addView(gear,new LinearLayout.LayoutParams(dp(52),dp(60)));
        head.addView(title("JEJAK TEKNISI\nSolusi Lengkap eMMC",21),new LinearLayout.LayoutParams(0,dp(70),1));
        root.addView(head);
        gear.setOnClickListener(v->showSettings());

        LinearLayout searchRow=new LinearLayout(this); searchRow.setGravity(Gravity.CENTER_VERTICAL);
        EditText search=new EditText(this); search.setHint("Cari kode eMMC..."); search.setHintTextColor(0xFF9FB2C8); search.setTextColor(0xFFFFFFFF);
        search.setSingleLine(true); search.setPadding(dp(12),0,dp(8),0); search.setBackgroundColor(0xFF102B45);
        searchRow.addView(search,new LinearLayout.LayoutParams(0,dp(52),1));
        Button voice=button("🎙"); voice.setMinWidth(dp(58)); searchRow.addView(voice);
        root.addView(searchRow,new LinearLayout.LayoutParams(-1,dp(58)));
        voice.setOnClickListener(v->voiceSearch(search));

        Button scan=button("📷  SCAN eMMC\nFokus tulisan • OCR • Deteksi");
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(78)); sp.setMargins(0,dp(14),0,dp(12));
        root.addView(scan,sp);
        scan.setOnClickListener(v->{
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},90);
            } else startActivity(new Intent(this,ScannerActivity.class));
        });

        LinearLayout grid=new LinearLayout(this); grid.setOrientation(LinearLayout.VERTICAL);
        String[][] labels={{"🗃️ Data eMMC","🕘 Riwayat"},{"🌐 Web Search","⚙️ Pengaturan"}};
        for(int r=0;r<2;r++){
            LinearLayout row=new LinearLayout(this);
            for(int c=0;c<2;c++){
                Button x=button(labels[r][c]); x.setBackgroundColor(0xFF0D1C2E);
                row.addView(x,new LinearLayout.LayoutParams(0,dp(70),1));
                if(r==0&&c==0) x.setOnClickListener(v->showDatabase());
                if(r==0&&c==1) x.setOnClickListener(v->showHistory());
                if(r==1&&c==0) x.setOnClickListener(v->webSearch(""));
                if(r==1&&c==1) x.setOnClickListener(v->showSettings());
            }
            grid.addView(row);
        }
        root.addView(grid);
        TextView tip=title("\nKenali eMMC • Tentukan kapasitas & grade • Simpan hasil",14);
        tip.setTextColor(0xFF9FB2C8); root.addView(tip);
    }

    void voiceSearch(EditText target){
        try{
            Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"id-ID");
            startActivityForResult(i,VOICE_REQ);
        }catch(Exception e){ Toast.makeText(this,"Voice search tidak tersedia",Toast.LENGTH_SHORT).show(); }
    }
    @Override protected void onActivityResult(int r,int c,Intent d){
        super.onActivityResult(r,c,d);
        if(r==VOICE_REQ&&c==RESULT_OK&&d!=null){
            ArrayList<String> a=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(a!=null&&!a.isEmpty()) webSearch(a.get(0));
        }
    }
    void webSearch(String q){
        String url="https://www.google.com/search?q="+android.net.Uri.encode((q==null?"":q)+" eMMC datasheet capacity");
        startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(url)));
    }
    void showDatabase(){
        ArrayList<EmmcRecord> list=DatabaseStore.load(this);
        StringBuilder s=new StringBuilder();
        for(EmmcRecord e:list) s.append(e.code).append(" • ").append(e.manufacturer).append(" • ").append(e.capacity).append(" • Grade ").append(e.grade.isEmpty()?"-":e.grade).append("\n\n");
        new AlertDialog.Builder(this).setTitle("Data eMMC ("+list.size()+")").setMessage(s.length()==0?"Belum ada data.":s.toString()).setPositiveButton("OK",null).show();
    }
    void showHistory(){
        new AlertDialog.Builder(this).setTitle("Riwayat").setMessage("Riwayat pencarian akan tersimpan pada versi lanjutan.").setPositiveButton("OK",null).show();
    }
    void showSettings(){
        new AlertDialog.Builder(this).setTitle("Pengaturan")
            .setMessage("Kamera: Fokus tulisan + Tap-to-focus\nOCR: Foto manual\nPencarian: Database lokal → web\nGrade: klasifikasi eMMC, bukan kerusakan")
            .setPositiveButton("OK",null).show();
    }
}
