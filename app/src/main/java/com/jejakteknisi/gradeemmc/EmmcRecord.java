package com.jejakteknisi.gradeemmc;

public class EmmcRecord {
    public String code, manufacturer, capacity, version, grade, pack, source;
    public EmmcRecord(String code,String manufacturer,String capacity,String version,String grade,String pack,String source){
        this.code=code; this.manufacturer=manufacturer; this.capacity=capacity; this.version=version; this.grade=grade; this.pack=pack; this.source=source;
    }
}
