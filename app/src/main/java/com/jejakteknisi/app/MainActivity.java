package com.jejakteknisi.app;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    DatabaseHelper db;
    LinearLayout content;
    EditText search;
    Spinner grade;

    String[] grades = {"Semua","A+++","A++ 2/32","A++ 16A","A++ 16B/A+","A+B","A+",
            "A+ Samsung/A khusus","Pilihan 256","Pilihan 128","Pilihan 64","Pilihan 32",
            "Pilihan 16","Pilihan 8GB"};

    int green = Color.rgb(0,150,110);
    int dark = Color.rgb(25,28,32);
    int light = Color.rgb(246,248,250);
    int gray = Color.rgb(105,112,120);

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DatabaseHelper(this);
        showMain();
    }

    GradientDrawable box(int color, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radius);
        return g;
    }

    TextView tv(String s, float size, int color) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    Button btn(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(Color.WHITE);
        b.setBackground(box(green, 28));
        return b;
    }

    void showMain() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(light);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(24,20,24,20);
        header.setBackgroundColor(dark);

        TextView brand = tv("JEJAK TEKNISI",14,Color.LTGRAY);
        brand.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        TextView title = tv("GRADE eMMC",26,Color.WHITE);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        TextView sub = tv("Database & klasifikasi eMMC untuk teknisi",13,Color.LTGRAY);

        header.addView(brand,new LinearLayout.LayoutParams(-1,30));
        header.addView(title,new LinearLayout.LayoutParams(-1,42));
        header.addView(sub,new LinearLayout.LayoutParams(-1,25));
        root.addView(header);

        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setPadding(18,16,18,8);
        search = new EditText(this);
        search.setHint("🔍  Cari kode eMMC...");
        search.setTextSize(16);
        search.setSingleLine(true);
        search.setPadding(20,0,20,0);
        search.setTextColor(dark);
        search.setHintTextColor(Color.rgb(145,150,156));
        search.setBackground(box(Color.WHITE,25));

        Button cari = btn("CARI");
        searchRow.addView(search,new LinearLayout.LayoutParams(0,52,1));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(100,52);
        cp.leftMargin=10;
        searchRow.addView(cari,cp);
        root.addView(searchRow);

        LinearLayout filter = new LinearLayout(this);
        filter.setPadding(18,2,18,8);
        TextView label = tv("FILTER GRADE",12,gray);
        label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        grade = new Spinner(this);
        grade.setBackground(box(Color.WHITE,18));
        grade.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,grades));
        filter.addView(label,new LinearLayout.LayoutParams(110,52));
        filter.addView(grade,new LinearLayout.LayoutParams(0,52,1));
        root.addView(filter);

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(18,4,18,90);
        scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout bottom = new LinearLayout(this);
        bottom.setPadding(18,8,18,14);
        bottom.setBackgroundColor(Color.WHITE);
        Button add = btn("＋  TAMBAH DATA eMMC");
        bottom.addView(add,new LinearLayout.LayoutParams(-1,52));
        root.addView(bottom);

        setContentView(root);

        cari.setOnClickListener(v -> loadEmmc());
        search.setOnEditorActionListener((v,id,event)->{loadEmmc();return true;});
        grade.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(android.widget.AdapterView<?> p) {}
            public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){loadEmmc();}
        });
        add.setOnClickListener(v -> addDialog());
        loadEmmc();
    }

    void loadEmmc() {
        content.removeAllViews();
        String q = search == null ? "" : search.getText().toString();
        String g = grade == null ? "Semua" : grade.getSelectedItem().toString();
        List<String[]> rows = db.search(q,g);

        TextView count = tv(rows.size()+" DATA eMMC",14,dark);
        count.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        count.setPadding(4,4,4,12);
        content.addView(count);

        if(rows.size()==0) {
            LinearLayout empty = new LinearLayout(this);
            empty.setOrientation(LinearLayout.VERTICAL);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(20,60,20,60);
            empty.setBackground(box(Color.WHITE,20));
            TextView icon=tv("🔎",42,gray); icon.setGravity(Gravity.CENTER);
            TextView a=tv("Data tidak ditemukan",17,dark); a.setGravity(Gravity.CENTER);
            a.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            TextView b=tv("Coba kode atau grade yang berbeda.",13,gray); b.setGravity(Gravity.CENTER);
            empty.addView(icon,new LinearLayout.LayoutParams(-1,60));
            empty.addView(a,new LinearLayout.LayoutParams(-1,35));
            empty.addView(b,new LinearLayout.LayoutParams(-1,30));
            content.addView(empty);
            return;
        }

        for(String[] r:rows) addCard(r);
    }

    void addCard(String[] r) {
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18,14,18,14);
        card.setBackground(box(Color.WHITE,20));

        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.bottomMargin=10;

        LinearLayout top=new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView code=tv(r[0],18,dark);
        code.setTypeface(Typeface.DEFAULT,Typeface.BOLD);

        TextView gr=tv(r[1],13,Color.WHITE);
        gr.setGravity(Gravity.CENTER);
        gr.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        gr.setPadding(12,4,12,4);
        gr.setBackground(box(green,30));

        top.addView(code,new LinearLayout.LayoutParams(0,40,1));
        top.addView(gr,new LinearLayout.LayoutParams(-2,34));

        TextView cap=tv("💾  "+r[2],14,gray);
        cap.setPadding(0,5,0,5);
        TextView note=tv(r[3],13,gray);

        card.addView(top);
        card.addView(cap);
        card.addView(note);
        card.setOnClickListener(v->detail(r));
        content.addView(card,p);
    }

    void detail(String[] r) {
        new AlertDialog.Builder(this)
                .setTitle(r[0])
                .setMessage("Grade : "+r[1]+"\nKapasitas : "+r[2]+"\n\nCatatan :\n"+r[3])
                .setPositiveButton("Tutup",null)
                .setNegativeButton("Hapus",(d,w)->{db.deleteEmmc(r[0]);loadEmmc();})
                .show();
    }

    void addDialog() {
        LinearLayout l=new LinearLayout(this);
        l.setPadding(30,0,30,0);
        l.setOrientation(LinearLayout.VERTICAL);

        EditText a=new EditText(this); a.setHint("Kode EMMC");
        EditText b=new EditText(this); b.setHint("Grade");
        EditText c=new EditText(this); c.setHint("Kapasitas");
        EditText d=new EditText(this); d.setHint("Catatan");

        l.addView(a); l.addView(b); l.addView(c); l.addView(d);

        new AlertDialog.Builder(this)
                .setTitle("Tambah Data eMMC")
                .setView(l)
                .setPositiveButton("Simpan",(x,w)->{
                    db.addEmmc(a.getText().toString(),b.getText().toString(),
                            c.getText().toString(),d.getText().toString());
                    loadEmmc();
                })
                .setNegativeButton("Batal",null)
                .show();
    }
}
