package com.jejakteknisi.gradeemmc;

import android.content.*;
import org.json.*;
import java.util.*;

public final class DatabaseStore {
    static final String PREF="emmc_db";
    static final String KEY="records";
    public static ArrayList<EmmcRecord> load(Context c){
        ArrayList<EmmcRecord> out=new ArrayList<>();
        String raw=c.getSharedPreferences(PREF,0).getString(KEY,"");
        if(raw.isEmpty()) return seed();
        try{
            JSONArray a=new JSONArray(raw);
            for(int i=0;i<a.length();i++){
                JSONObject o=a.getJSONObject(i);
                out.add(new EmmcRecord(o.optString("code"),o.optString("manufacturer"),o.optString("capacity"),
                    o.optString("version"),o.optString("grade"),o.optString("pack"),o.optString("source")));
            }
        }catch(Exception e){ return seed(); }
        return out;
    }
    public static void save(Context c,ArrayList<EmmcRecord> list){
        JSONArray a=new JSONArray();
        try{
            for(EmmcRecord e:list){
                JSONObject o=new JSONObject(); o.put("code",e.code);o.put("manufacturer",e.manufacturer);
                o.put("capacity",e.capacity);o.put("version",e.version);o.put("grade",e.grade);
                o.put("pack",e.pack);o.put("source",e.source);a.put(o);
            }
        }catch(Exception ignored){}
        c.getSharedPreferences(PREF,0).edit().putString(KEY,a.toString()).apply();
    }
    static ArrayList<EmmcRecord> seed(){
        ArrayList<EmmcRecord> x=new ArrayList<>();
        x.add(new EmmcRecord("KLM4G1YEMD-C031","Samsung","4 GB","5.0","","11.5x13x0.8","Samsung official"));
        x.add(new EmmcRecord("KLM8G1WEMB-B031","Samsung","8 GB","5.0","","11.5x13x0.8","Samsung official"));
        x.add(new EmmcRecord("KLMAG2WEMB-B031","Samsung","16 GB","5.0","","11.5x13x0.8","Samsung official"));
        x.add(new EmmcRecord("KLMBG4WEBC-B031","Samsung","32 GB","5.0","","11.5x13x1.0","Samsung official"));
        x.add(new EmmcRecord("KLMCG8WEBC-B031","Samsung","64 GB","5.0","","11.5x13x1.0","Samsung official"));
        x.add(new EmmcRecord("KLM4G1FEAC-B031","Samsung","4 GB","5.0","","11x10x0.8","Samsung official"));
        x.add(new EmmcRecord("KLM8G1GEAC-B031","Samsung","8 GB","5.0","","11.5x13x1.0","Samsung official"));
        x.add(new EmmcRecord("KLMAG2GEAC-B031","Samsung","16 GB","5.0","","11.5x13x1.0","Samsung official"));
        x.add(new EmmcRecord("KLMBG4GEAC-B031","Samsung","32 GB","5.0","","11.5x13x1.0","Samsung official"));
        x.add(new EmmcRecord("KLMCG8GEAC-B031","Samsung","64 GB","5.0","","11.5x13x1.2","Samsung official"));
        x.add(new EmmcRecord("KLMDGAGEAC-B001","Samsung","128 GB","4.5","","11.5x13x1.4","Samsung official"));
        x.add(new EmmcRecord("THGBMUG6C1LBAIL","KIOXIA","8 GB","5.1","","11.5x13x0.8","KIOXIA official"));
        x.add(new EmmcRecord("THGBMUG7C1LBAIL","KIOXIA","16 GB","5.1","","11.5x13x0.8","KIOXIA official"));
        x.add(new EmmcRecord("THGBMUG8C2LBAIL","KIOXIA","32 GB","5.1","","11.5x13x0.8","KIOXIA official"));
        x.add(new EmmcRecord("THGAMVG9T23BAIL","KIOXIA","64 GB","5.1","","11.5x13x0.8","KIOXIA official"));
        x.add(new EmmcRecord("THGAMVT0T43BAIR","KIOXIA","128 GB","5.1","","11.5x13x1.0","KIOXIA official"));
        x.add(new EmmcRecord("THGBMJG6C1LBAU7","KIOXIA","8 GB","5.1","","11.5x13x1.0","KIOXIA official"));
        x.add(new EmmcRecord("THGBMJG7C2LBAU8","KIOXIA","16 GB","5.1","","11.5x13x1.0","KIOXIA official"));
        x.add(new EmmcRecord("THGBMJG8C4LBAU8","KIOXIA","32 GB","5.1","","11.5x13x1.2","KIOXIA official"));
        x.add(new EmmcRecord("THGBMJG9C8LBAU8","KIOXIA","64 GB","5.1","","11.5x13x1.2","KIOXIA official"));
        x.add(new EmmcRecord("MTFC256GBCAQTC-IT","Micron","256 GB","5.1","","11.5x13x1.3","Micron official"));
        x.add(new EmmcRecord("MTFC64GAXAQEA-WT","Micron","512 Gb","5.1","","11.5x13x0.8","Micron official"));
        x.add(new EmmcRecord("MTFC8GAMALBH-AAT","Micron","64 Gb","5.1","","11.5x13x1.1","Micron official"));
        x.add(new EmmcRecord("MTFC128GAZAQJP-AAT","Micron","128 GB","","","11.5x13x1.0","Micron official"));
        x.add(new EmmcRecord("MTFC16GAPALBH-AAT","Micron","128 Gb","5.1","","11.5x13x1.1","Micron official"));
        x.add(new EmmcRecord("MTFC64GBCAQDQ-AAT","Micron","64 GB","5.1","","14x18x1.4","Micron official"));
        x.add(new EmmcRecord("MTFC32GBCAQTC-IT","Micron","32 GB","5.1","","11.5x13x1.3","Micron official"));
        x.add(new EmmcRecord("EMMC04G-MT32","Kingston","4 GB","5.1","","11.5x13x0.8","Kingston official"));
        x.add(new EmmcRecord("EMMC04G-CT32","Kingston","4 GB","5.1","","9.0x7.5x0.8","Kingston official"));
        x.add(new EmmcRecord("EMMC08G-CT32","Kingston","8 GB","5.1","","9.0x7.5x0.8","Kingston official"));
        x.add(new EmmcRecord("EMMC16G-MW28","Kingston","16 GB","5.1","","11.5x13x0.9","Kingston official"));
        x.add(new EmmcRecord("EMMC32G-TS0A","Kingston","32 GB","5.1","","11.5x13x1.0","Kingston official"));
        x.add(new EmmcRecord("EMMC64G-TB9F","Kingston","64 GB","5.1","","8.0x8.5x0.8","Kingston official"));
        x.add(new EmmcRecord("EMMC64G-TY29","Kingston","64 GB","5.1","","11.5x13x0.8","Kingston official"));
        x.add(new EmmcRecord("EMMC128-TY29","Kingston","128 GB","5.1","","11.5x13x0.8","Kingston official"));
        x.add(new EmmcRecord("EMMC256-TY29","Kingston","256 GB","5.1","","11.5x13x1.0","Kingston official"));
        x.add(new EmmcRecord("SDINBDG4-8G-XA3","SanDisk","8 GB","5.1","Automotive Grade 3","0.45x0.51x0.05 in","SanDisk official"));
        x.add(new EmmcRecord("SDINBDG4-8G-ZA3","SanDisk","8 GB","5.1","Automotive Grade 2","0.45x0.51x0.05 in","SanDisk official"));
        x.add(new EmmcRecord("SDINBDG4-16G-XA3","SanDisk","16 GB","5.1","Automotive Grade 3","0.45x0.51x0.05 in","SanDisk official"));
        x.add(new EmmcRecord("SDINBDG4-16G-ZA3","SanDisk","16 GB","5.1","Automotive Grade 2","0.45x0.51x0.05 in","SanDisk official"));
        x.add(new EmmcRecord("SDINBDA6-32G-XA1","SanDisk","32 GB","5.1","Automotive Grade 3","11.5x13x1.0","SanDisk official"));
        x.add(new EmmcRecord("SDINBDA6-32G-ZA1","SanDisk","32 GB","5.1","Automotive Grade 2","11.5x13x1.0","SanDisk official"));
        x.add(new EmmcRecord("SDINBDA6-64G-XA1","SanDisk","64 GB","5.1","Automotive Grade 3","11.5x13x1.0","SanDisk official"));
        x.add(new EmmcRecord("SDINBDA6-64G-ZA1","SanDisk","64 GB","5.1","Automotive Grade 2","11.5x13x1.0","SanDisk official"));
        x.add(new EmmcRecord("SDINBDA6-128G-XA1","SanDisk","128 GB","5.1","Automotive Grade 3","11.5x13x1.0","SanDisk official"));
        x.add(new EmmcRecord("SDINBDA6-128G-ZA1","SanDisk","128 GB","5.1","Automotive Grade 2","11.5x13x1.0","SanDisk official"));
        x.add(new EmmcRecord("SDINBDA6-256G-XA1","SanDisk","256 GB","5.1","Automotive Grade 3","11.5x13x1.0","SanDisk official"));
        x.add(new EmmcRecord("SDINBDA6-256G-ZA1","SanDisk","256 GB","5.1","Automotive Grade 2","11.5x13x1.0","SanDisk official"));
        return x;
    }
}
