package com.jejakteknisi.app;

import android.content.*;import android.database.sqlite.*;import android.database.Cursor;import java.util.*;

public class DatabaseHelper extends SQLiteOpenHelper {
 public static final String DB="jejak_teknisi.db"; public DatabaseHelper(Context c){super(c,DB,null,1);}
 public void onCreate(SQLiteDatabase db){
  db.execSQL("CREATE TABLE emmc(id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT NOT NULL, grade TEXT NOT NULL, capacity TEXT, note TEXT)");
  db.execSQL("CREATE TABLE schematic(id INTEGER PRIMARY KEY AUTOINCREMENT, model TEXT NOT NULL, title TEXT NOT NULL, image TEXT, note TEXT)");
  seed(db);
 }
 public void onUpgrade(SQLiteDatabase db,int o,int n){db.execSQL("DROP TABLE IF EXISTS emmc");db.execSQL("DROP TABLE IF EXISTS schematic");onCreate(db);}
 private void seed(SQLiteDatabase db){
  String[][] rows={
   {"CONTOH-EMMC-256","A+++","256 GB","Data awal; tambahkan kode asli melalui tombol Tambah"},
   {"CONTOH-EMMC-128","A+++","128 GB","Data awal"},{"CONTOH-EMMC-64","A+++","64 GB","Data awal"},
   {"CONTOH-EMMC-32","A++ 2/32","32 GB","Data awal"},{"CONTOH-EMMC-16A","A++ 16A","16 GB","Data awal"},
   {"CONTOH-EMMC-16B","A++ 16B/A+","16 GB","Data awal"},{"CONTOH-EMMC-8","Pilihan 8GB","8 GB","Data awal"},
   {"SAMSUNG-KHUSUS","A+ Samsung/A khusus","-","Periksa kecocokan sebelum penggantian"}};
  for(String[] r:rows){ContentValues v=new ContentValues();v.put("code",r[0]);v.put("grade",r[1]);v.put("capacity",r[2]);v.put("note",r[3]);db.insert("emmc",null,v);}
  ContentValues s=new ContentValues();s.put("model","Template");s.put("title","Schematic baru");s.put("image","schematic_placeholder");s.put("note","Ganti dengan gambar schematic asli");db.insert("schematic",null,s);
 }
 public List<String[]> search(String q,String grade){List<String[]> out=new ArrayList<>();SQLiteDatabase db=getReadableDatabase();String where="(code LIKE ? OR grade LIKE ? OR capacity LIKE ?)";List<String> a=new ArrayList<>();String x="%"+q+"%";a.add(x);a.add(x);a.add(x);if(!grade.equals("Semua")){where+=" AND grade=?";a.add(grade);}Cursor c=db.rawQuery("SELECT code,grade,capacity,note FROM emmc WHERE "+where+" ORDER BY grade,code",a.toArray(new String[0]));while(c.moveToNext())out.add(new String[]{c.getString(0),c.getString(1),c.getString(2),c.getString(3)});c.close();return out;}
 public long addEmmc(String code,String grade,String cap,String note){ContentValues v=new ContentValues();v.put("code",code);v.put("grade",grade);v.put("capacity",cap);v.put("note",note);return getWritableDatabase().insert("emmc",null,v);}
 public void deleteEmmc(String code){getWritableDatabase().delete("emmc","code=?",new String[]{code});}
 public List<String[]> schematics(String q){List<String[]> o=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT model,title,image,note FROM schematic WHERE model LIKE ? OR title LIKE ? ORDER BY model",new String[]{"%"+q+"%","%"+q+"%"});while(c.moveToNext())o.add(new String[]{c.getString(0),c.getString(1),c.getString(2),c.getString(3)});c.close();return o;}
}
