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
            "A+ Samsung/A khusus","Pilihan 256","Pilihan 128","Pilihan 64",
            "Pilihan 32","Pilihan 16","Pilihan 8GB"};

    int green = Color.rgb(0,190,130); cardDark=Color.rgb(31,35,39);
    int dark = Color.rgb(18,20,22); green=Color.rgb(0,190,130);
    int light = Color.rgb(18,20,22); white=Color.WHITE;
    int gray = Color.rgb(105,112,120); line=Color.rgb(70,76,82);

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        db=new DatabaseHelper(this);
        showMain();
    }

    GradientDrawable box(int c,float r) {
        GradientDrawable g=new GradientDrawable();
        g.setColor(c); g.setCornerRadius(r); return g;
    }

    TextView tv(String s,float size,int c) {
        TextView t=new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(c);
        t.setGravity(Gravity.CENTER_VERTICAL); return t;
    }

    Button btn(String s) {
        Button b=new Button(this);
        b.setText(s); b.setTextSize(14); b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        b.setTextColor(white); b.setBackground(box(greenDark,24));
        return b;
    }

    void showMain() {
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bgDark);

        LinearLayout header=new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(22,18,22,16);
        header.setBackgroundColor(bgDark);

        TextView brand=tv("JEJAK TEKNISI",13,green);
        brand.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        TextView title=tv("GRADE eMMC",27,white);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        TextView sub=tv("Database eMMC untuk teknisi HP",13,gray);

        header.addView(brand,new LinearLayout.LayoutParams(-1,28));
        header.addView(title,new LinearLayout.LayoutParams(-1,43));
        header.addView(sub,new LinearLayout.LayoutParams(-1,24));
        root.addView(header);

        LinearLayout sr=new LinearLayout(this);
        sr.setPadding(18,8,18,10);

        search=new EditText(this);
        search.setHint("🔍  Cari kode eMMC...");
        search.setTextSize(16); search.setSingleLine(true);
        search.setPadding(18,0,18,0);
        search.setTextColor(white);
        search.setHintTextColor(Color.rgb(125,132,139));
        search.setBackground(box(card2,22));

        Button cari=btn("CARI");
        sr.addView(search,new LinearLayout.LayoutParams(0,52,1));
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(105,52);
        bp.leftMargin=9; sr.addView(cari,bp);
        root.addView(sr);

        LinearLayout filter=new LinearLayout(this);
        filter.setPadding(18,0,18,10);
        filter.setGravity(Gravity.CENTER_VERTICAL);

        TextView label=tv("GRADE",12,gray);
        label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);

        grade=new Spinner(this);
        grade.setPadding(8,0,8,0);
        grade.setBackground(box(card2,18));
        grade.setAdapter(new ArrayAdapter<String>(
                this,android.R.layout.simple_spinner_dropdown_item,grades));

        filter.addView(label,new LinearLayout.LayoutParams(70,48));
        filter.addView(grade,new LinearLayout.LayoutParams(0,48,1));
        root.addView(filter);

        View divider=new View(this);
        divider.setBackgroundColor(line);
        root.addView(divider,new LinearLayout.LayoutParams(-1,1));

        ScrollView scroll=new ScrollView(this);
        content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(16,10,16,90);
        scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout bottom=new LinearLayout(this);
        bottom.setPadding(16,8,16,12);
        bottom.setBackgroundColor(Color.rgb(15,17,19));
        Button add=btn("＋  TAMBAH DATA eMMC");
        bottom.addView(add,new LinearLayout.LayoutParams(-1,52));
        root.addView(bottom);

        setContentView(root);

        cari.setOnClickListener(v->loadEmmc());
        search.setOnEditorActionListener((v,id,event)->{loadEmmc();return true;});
        grade.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> p){}
            public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){loadEmmc();}
        });
        add.setOnClickListener(v->addDialog());
        loadEmmc();
    }

    void loadEmmc() {
        content.removeAllViews();
        String q=search==null?"":search.getText().toString();
        String g=grade==null?"Semua":grade.getSelectedItem().toString();
        List<String[]> rows=db.search(q,g);

        TextView count = tv(rows.size()+" DATA eMMC",14,dark);
        count.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        count.setPadding(3,4,3,10);
        content.addView(count);

        if(rows.size()==0) {
            LinearLayout empty=new LinearLayout(this);
            empty.setOrientation(LinearLayout.VERTICAL);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(20,60,20,60);
            empty.setBackground(box(cardDark,18));
            TextView icon=tv("🔎",40,gray); icon.setGravity(Gravity.CENTER);
            TextView a=tv("Data tidak ditemukan",17,white);
            a.setGravity(Gravity.CENTER); a.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            TextView b=tv("Coba kode atau grade lain.",13,gray); b.setGravity(Gravity.CENTER);
            empty.addView(icon,new LinearLayout.LayoutParams(-1,58));
            empty.addView(a,new LinearLayout.LayoutParams(-1,34));
            empty.addView(b,new LinearLayout.LayoutParams(-1,28));
            content.addView(empty); return;
        }

        for(String[] r:rows) addCard(r);
    }

    void addCard(String[] r) {
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18,14,18,14);
        card.setBackground(box(Color.rgb(31,35,39),20));

        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);
        cp.bottomMargin=9;

        LinearLayout top=new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView code=tv(r[0],18,Color.WHITE);
        code.setTypeface(Typeface.DEFAULT,Typeface.BOLD);

        TextView gr=tv(r[1],12,white);
        gr.setGravity(Gravity.CENTER);
        gr.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        gr.setPadding(11,3,11,3);
        gr.setBackground(box(greenDark,20));

        top.addView(code,new LinearLayout.LayoutParams(0,40,1));
        top.addView(gr,new LinearLayout.LayoutParams(-2,32));

        View d=new View(this);
        d.setBackgroundColor(line);

        TextView cap=tv("💾  Kapasitas : "+r[2],14,gray);
        cap.setPadding(0,7,0,4);
        TextView note=tv("📝  "+r[3],13,gray);

        card.addView(top);
        card.addView(d,new LinearLayout.LayoutParams(-1,1));
        card.addView(cap); card.addView(note);
        card.setOnClickListener(v->detail(r));
        content.addView(card,cp);
    }

    void detail(String[] r) {
        new AlertDialog.Builder(this).setTitle(r[0])
            .setMessage("Grade : "+r[1]+"\nKapasitas : "+r[2]+"\n\nCatatan :\n"+r[3])
            .setPositiveButton("Tutup",null)
            .setNegativeButton("Hapus",(d,w)->{db.deleteEmmc(r[0]);loadEmmc();}).show();
    }

    void addDialog() {
        LinearLayout l=new LinearLayout(this);
        l.setPadding(30,0,30,0); l.setOrientation(LinearLayout.VERTICAL);
        EditText a=new EditText(this); a.setHint("Kode EMMC");
        EditText b=new EditText(this); b.setHint("Grade");
        EditText c=new EditText(this); c.setHint("Kapasitas");
        EditText d=new EditText(this); d.setHint("Catatan");
        l.addView(a);l.addView(b);l.addView(c);l.addView(d);

        new AlertDialog.Builder(this).setTitle("Tambah Data eMMC").setView(l)
            .setPositiveButton("Simpan",(x,w)->{
                db.addEmmc(a.getText().toString(),b.getText().toString(),
                    c.getText().toString(),d.getText().toString());
                loadEmmc();
            }).setNegativeButton("Batal",null).show();
    }
}
