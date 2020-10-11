package com.azo.sohbetuygulama.Model;

public class Kisiler {

    String ad, durum;

    public Kisiler() {
    }

    public Kisiler(String ad, String durum) {
        this.ad = ad;
        this.durum = durum;
    }

    public String getAd() {
        return ad;
    }

    public Kisiler setAd(String ad) {
        this.ad = ad;
        return this;
    }

    public String getDurum() {
        return durum;
    }

    public Kisiler setDurum(String durum) {
        this.durum = durum;
        return this;
    }
}
