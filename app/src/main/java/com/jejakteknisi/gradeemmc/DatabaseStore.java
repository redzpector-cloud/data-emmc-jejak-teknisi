package com.jejakteknisi.gradeemmc;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class DatabaseStore {
    private static final String PREF="jejak_teknisi_emmc_v3";
    public static ArrayList<EmmcRecord> load(Context c){
        ArrayList<EmmcRecord> out=new ArrayList<>();
        String raw=c.getSharedPreferences(PREF,0).getString("data","");
        try{
            JSONArray a=new JSONArray(raw);
            for(int i=0;i<a.length();i++){
                JSONObject o=a.getJSONObject(i);
                out.add(new EmmcRecord(o.optString("code"),o.optString("manufacturer"),o.optString("capacity"),o.optString("version"),o.optString("grade"),o.optString("pack"),o.optString("source")));
            }
            return out;
        }catch(Exception ignored){}
        out.add(new EmmcRecord("KH3V6001CH","Samsung","16 GB","eMMC 5.0","Belum ditentukan","BGA","Data awal"));
        out.add(new EmmcRecord("KLM8G1WEMB-B031","Samsung","8 GB","eMMC 4.x","Belum ditentukan","BGA","Data awal"));
        out.add(new EmmcRecord("KLMAG2WEMB-B031","Samsung","16 GB","eMMC 5.0","Belum ditentukan","BGA","Data awal"));
        out.add(new EmmcRecord("THGBMFG7C1LBAIL","KIOXIA/Toshiba","16 GB","eMMC 4.5","Belum ditentukan","BGA","Data awal"));
        out.add(new EmmcRecord("H26M52103FMR","SK hynix","16 GB","eMMC 4.x","Belum ditentukan","BGA","Data awal"));
        save(c,out); return out;
    }
    public static void save(Context c,ArrayList<EmmcRecord> list){
        JSONArray a=new JSONArray();
        try{ for(EmmcRecord e:list){ JSONObject o=new JSONObject();o.put("code",e.code);o.put("manufacturer",e.manufacturer);o.put("capacity",e.capacity);o.put("version",e.version);o.put("grade",e.grade);o.put("pack",e.pack);o.put("source",e.source);a.put(o);} }catch(Exception ignored){}
        c.getSharedPreferences(PREF,0).edit().putString("data",a.toString()).apply();
    }
    private static String norm(String s){return s==null?"":s.replaceAll("[^A-Za-z0-9]","").toUpperCase(Locale.US);}
    public static EmmcRecord find(Context c,String code){String n=norm(code);if(n.length()<4)return null;for(EmmcRecord e:load(c))if(norm(e.code).equals(n))return e;return null;}
    public static ArrayList<EmmcRecord> similar(Context c,String code){
        ArrayList<EmmcRecord> r=new ArrayList<>();String n=norm(code);if(n.length()<4)return r;
        for(EmmcRecord e:load(c)){String x=norm(e.code);int d=distance(n,x);int max=Math.max(n.length(),x.length());if(d<=Math.max(2,max/5)||x.contains(n)||n.contains(x))r.add(e);}
        Collections.sort(r,(a,b)->Integer.compare(distance(n,norm(a.code)),distance(n,norm(b.code))));return r;
    }
    static int distance(String a,String b){int[] prev=new int[b.length()+1];for(int j=0;j<=b.length();j++)prev[j]=j;for(int i=1;i<=a.length();i++){int[] cur=new int[b.length()+1];cur[0]=i;for(int j=1;j<=b.length();j++)cur[j]=Math.min(Math.min(cur[j-1]+1,prev[j]+1),prev[j-1]+(a.charAt(i-1)==b.charAt(j-1)?0:1));prev=cur;}return prev[b.length()];}
}
