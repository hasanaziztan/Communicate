package com.azo.sohbetuygulama.Model;

public class Mesajlar {

    private String kimden,mesaj,tur;

    public Mesajlar() {
    }

    public Mesajlar(String kimden, String mesaj, String tur) {
        this.kimden = kimden;
        this.mesaj = mesaj;
        this.tur = tur;
    }

    public String getKimden() {
        return kimden;
    }

    public Mesajlar setKimden(String kimden) {
        this.kimden = kimden;
        return this;
    }

    public String getMesaj() {
        return mesaj;
    }

    public Mesajlar setMesaj(String mesaj) {
        this.mesaj = mesaj;
        return this;
    }

    public String getTur() {
        return tur;
    }

    public Mesajlar setTur(String tur) {
        this.tur = tur;
        return this;
    }
}
