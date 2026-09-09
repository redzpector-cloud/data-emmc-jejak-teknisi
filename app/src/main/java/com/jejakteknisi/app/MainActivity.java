package com.jejakteknisi.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import android.text.*;
import java.util.*;

public class MainActivity extends Activity {

    DatabaseHelper db;
    LinearLayout root, content;
    EditText search;
    Spinner grade;
    TextView count;
    boolean schematic = false;

    String[] grades = {
        "Semua", "A+++", "A++ 2/32", "A++ 16A", "A++ 16B/A+",
        "A+B", "A+", "A+ Samsung/A khusus", "Pilihan 256",
        "Pilihan 128", "Pilihan 64", "Pilihan 32", "Pilihan 16", "Pilihan 8GB"
    };

    int green = Color.rgb(0, 150, 110);
    int dark = Color.rgb(25, 28, 32);
    int light = Color.rgb(246, 248, 250);
    int white = Color.WHITE;
    int gray = Color.rgb(100, 108, 116);

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DatabaseHelper(this);
        showMain();
    }

    TextView tv(String s, float size) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(dark);
        t.setPadding(18, 14, 18, 14);
        return t;
    }

    Button btn(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(14);
        b.setAllCaps(false);
        return b;
    }

    GradientDrawableBox box(int color, float radius) {
        return new GradientDrawableBox(color, radius);
    }

    void showMain() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(light);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(22, 18, 22, 14);
        header.setBackgroundColor(dark);

        TextView title = tv("JEJAK TEKNISI", 24);
        title.setTextColor(Color.rgb(0, 210, 145));
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView sub = tv("GRADE eMMC", 20);
        sub.setTextColor(white);
        sub.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        header.addView(title);
        header.addView(sub);
        root.addView(header);

        LinearLayout nav = new LinearLayout(this);
        nav.setPadding(10, 8, 10, 8);
        nav.setBackgroundColor(white);

        Button e = btn("Grade eMMC");
        Button sc = btn("Schematic");
        nav.addView(e, new LinearLayout.LayoutParams(0, 55, 1));
        nav.addView(sc, new LinearLayout.LayoutParams(0, 55, 1));
        root.addView(nav);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);

        e.setOnClickListener(v -> {
            schematic = false;
            render();
        });

        sc.setOnClickListener(v -> {
            schematic = true;
            render();
        });

        render();
    }

    EditText searchBox(String hint) {
        EditText s = new EditText(this);
        s.setHint(hint);
        s.setTextColor(dark);
        s.setHintTextColor(gray);
        s.setTextSize(16);
        s.setSingleLine(true);
        s.setPadding(18, 0, 18, 0);
        s.setBackground(box(white, 14));
        return s;
    }

    void render() {
        content.removeAllViews();

        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setPadding(14, 14, 14, 8);

        search = searchBox(
            schematic ? "Cari model / judul schematic" :
            "Cari kode eMMC, grade, kapasitas..."
        );

        Button cari = btn("🔍 CARI");
        cari.setTextColor(white);
        cari.setBackground(box(green, 14));

        searchRow.addView(search, new LinearLayout.LayoutParams(0, 55, 1));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(100, 55);
        cp.setMargins(8, 0, 0, 0);
        searchRow.addView(cari, cp);
        content.addView(searchRow);

        cari.setOnClickListener(v -> {
            if (schematic) loadSchematic();
            else loadEmmc();
        });

        if (!schematic) {
            LinearLayout filterRow = new LinearLayout(this);
            filterRow.setPadding(14, 2, 14, 8);

            grade = new Spinner(this);
            grade.setBackground(box(white, 12));
            grade.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                grades
            ));

            filterRow.addView(grade, new LinearLayout.LayoutParams(0, 55, 1));

            Button scan = btn("📷 SCAN OCR");
            scan.setTextColor(white);
            scan.setBackground(box(dark, 12));
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(125, 55);
            sp.setMargins(8, 0, 0, 0);
            filterRow.addView(scan, sp);

            Button add = btn("＋ TAMBAH");
            add.setBackground(box(Color.rgb(225, 235, 232), 12));
            LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(100, 55);
            ap.setMargins(8, 0, 0, 0);
            filterRow.addView(add, ap);

            content.addView(filterRow);

            scan.setOnClickListener(v ->
                startActivity(new Intent(this, ScannerActivity.class))
            );
            add.setOnClickListener(v -> addDialog());

            grade.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    public void onNothingSelected(android.widget.AdapterView<?> p) {}
                    public void onItemSelected(
                        android.widget.AdapterView<?> p, View v, int pos, long id
                    ) {
                        loadEmmc();
                    }
                }
            );

            search.setOnEditorActionListener((v, actionId, event) -> {
                loadEmmc();
                return false;
            });

            loadEmmc();

        } else {
            Button add = btn("＋ TAMBAH SCHEMATIC");
            add.setTextColor(white);
            add.setBackground(box(green, 12));

            LinearLayout.LayoutParams addp =
                new LinearLayout.LayoutParams(-1, 52);
            addp.setMargins(14, 0, 14, 10);
            content.addView(add, addp);

            add.setOnClickListener(v -> schematicDialog());

            search.setOnEditorActionListener((v, actionId, event) -> {
                loadSchematic();
                return false;
            });

            loadSchematic();
        }
    }

    void loadEmmc() {
        if (content.getChildCount() > 2) {
            content.removeViews(2, content.getChildCount() - 2);
        }

        String g = grade == null ? "Semua" :
            grade.getSelectedItem().toString();

        String q = search == null ? "" : search.getText().toString();
        List<String[]> rows = db.search(q, g);

        count = tv(rows.size() + " data ditemukan", 15);
        count.setTextColor(gray);
        count.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        content.addView(count);

        for (String[] r : rows) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(16, 12, 16, 12);
            card.setBackground(box(white, 12));

            LinearLayout.LayoutParams cardp =
                new LinearLayout.LayoutParams(-1, -2);
            cardp.setMargins(10, 3, 10, 5);

            TextView code = tv(r[0], 18);
            code.setTextColor(dark);
            code.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            TextView info = tv(
                "Grade  " + r[1] + "   •   Kapasitas " + r[2],
                14
            );
            info.setTextColor(green);

            TextView note = tv(r[3], 13);
            note.setTextColor(gray);

            card.addView(code);
            card.addView(info);
            card.addView(note);
            content.addView(card, cardp);

            card.setOnClickListener(v -> detail(r));
        }
    }

    void loadSchematic() {
        if (content.getChildCount() > 2) {
            content.removeViews(2, content.getChildCount() - 2);
        }

        String q = search == null ? "" : search.getText().toString();
        List<String[]> rows = db.schematics(q);

        TextView c = tv("Schematic: " + rows.size() + " data", 15);
        c.setTextColor(gray);
        content.addView(c);

        for (String[] r : rows) {
            TextView t = tv(
                "📐  " + r[0] + "\n" + r[1] + "\n" + r[3],
                16
            );
            t.setTextColor(dark);
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            t.setBackground(box(white, 12));

            LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, -2);
            p.setMargins(10, 3, 10, 5);

            content.addView(t, p);
            t.setOnClickListener(v -> schematicDetail(r));
        }
    }

    void detail(String[] r) {
        new AlertDialog.Builder(this)
            .setTitle(r[0])
            .setMessage(
                "Grade: " + r[1] +
                "\nKapasitas: " + r[2] +
                "\n\n" + r[3]
            )
            .setPositiveButton("Tutup", null)
            .setNegativeButton("Hapus", (d, w) -> {
                db.deleteEmmc(r[0]);
                loadEmmc();
            })
            .show();
    }

    void addDialog() {
        LinearLayout l = new LinearLayout(this);
        l.setPadding(30, 0, 30, 0);
        l.setOrientation(LinearLayout.VERTICAL);

        EditText a = new EditText(this);
        a.setHint("Kode EMMC");

        EditText b = new EditText(this);
        b.setHint("Grade");

        EditText c = new EditText(this);
        c.setHint("Kapasitas");

        EditText d = new EditText(this);
        d.setHint("Catatan");

        for (EditText x : new EditText[]{a, b, c, d}) l.addView(x);

        new AlertDialog.Builder(this)
            .setTitle("Tambah EMMC")
            .setView(l)
            .setPositiveButton("Simpan", (x, w) -> {
                db.addEmmc(
                    a.getText().toString(),
                    b.getText().toString(),
                    c.getText().toString(),
                    d.getText().toString()
                );
                loadEmmc();
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    void schematicDialog() {
        LinearLayout l = new LinearLayout(this);
        l.setPadding(30, 0, 30, 0);
        l.setOrientation(LinearLayout.VERTICAL);

        EditText a = new EditText(this);
        a.setHint("Model HP");

        EditText b = new EditText(this);
        b.setHint("Judul schematic");

        l.addView(a);
        l.addView(b);

        new AlertDialog.Builder(this)
            .setTitle("Tambah Schematic")
            .setView(l)
            .setMessage(
                "Versi awal menyimpan model/judul. " +
                "Gambar schematic dapat ditambahkan pada tahap berikutnya."
            )
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .show();
    }

    void schematicDetail(String[] r) {
        new AlertDialog.Builder(this)
            .setTitle(r[0])
            .setMessage(r[1] + "\n\n" + r[3])
            .setPositiveButton("Buka", (d, w) -> {})
            .setNegativeButton("Tutup", null)
            .show();
    }

    static class GradientDrawableBox extends android.graphics.drawable.GradientDrawable {
        GradientDrawableBox(int color, float radius) {
            setColor(color);
            setCornerRadius(radius);
        }
    }
}
