package com.azo.sohbetuygulama;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilActivity extends AppCompatActivity {

    private String alinanKullaniciId, aktif_durum, aktifKullaniciId;

    //Layout tanımlamaları
    private TextView kullaniciProfilAdi, kullaniciProfilDurumu;
    private Button mesajGondermeTalebiButonu,mesajDegerlendirmeTalebiButonu;

    //Firebase entegrasyon
    private DatabaseReference kullaniciYolu, sohbetTalebiYolu,sohbetlerYolu,bildirimYolu;
    private FirebaseAuth mYetki;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        //Intent ile alma
        alinanKullaniciId = getIntent().getExtras().get("tiklananKullaniciIdGoster").toString();
        // Tanımlamalar
        kullaniciProfilAdi = findViewById(R.id.kullanici_adi_ziyaret);
        kullaniciProfilDurumu = findViewById(R.id.profil_durumu_ziyaret);
        mesajGondermeTalebiButonu = findViewById(R.id.mesaj_gonderme_talebi_butonu);
        mesajDegerlendirmeTalebiButonu = findViewById(R.id.mesaj_degerlendirme_talebi_butonu);

        aktif_durum = "yeni";

        //Firebase
        kullaniciYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        sohbetTalebiYolu = FirebaseDatabase.getInstance().getReference().child("Sohbet Talebi");
        sohbetlerYolu = FirebaseDatabase.getInstance().getReference().child("Sohbetler");
        bildirimYolu = FirebaseDatabase.getInstance().getReference().child("Bildirimler");
        mYetki = FirebaseAuth.getInstance();

        aktifKullaniciId = mYetki.getCurrentUser().getUid();

        kullaniciBilgisiAl();
    }

    private void kullaniciBilgisiAl() {

        kullaniciYolu.child(alinanKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //firebaseden veri çekme
                if (dataSnapshot.exists()) {
                    String kullaniciAdi = dataSnapshot.child("ad").getValue().toString();
                    String kullaniciDurumu = dataSnapshot.child("durum").getValue().toString();

                    //Verileri gösterme
                    kullaniciProfilAdi.setText(kullaniciAdi);
                    kullaniciProfilDurumu.setText(kullaniciDurumu);

                    //konuşma Talebi
                    chatTalepleriniYonet();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void chatTalepleriniYonet() {
        //Talep varsa buton iptali göstersin
        sohbetTalebiYolu.child(aktifKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(alinanKullaniciId)) {
                    String talep_turu = dataSnapshot.child(alinanKullaniciId).child("talep_turu").getValue().toString();
                    if (talep_turu.equals("gönderildi")){

                        aktif_durum = "talep_gönderildi";
                        mesajGondermeTalebiButonu.setText("Sohbet Talebi İptal");
                    }else {
                        aktif_durum = "talep_alindi";
                        mesajGondermeTalebiButonu.setText("Sohbet Talebi Kabul");
                        mesajDegerlendirmeTalebiButonu.setVisibility(View.VISIBLE);
                        mesajDegerlendirmeTalebiButonu.setEnabled(true);

                        mesajDegerlendirmeTalebiButonu.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                MesajTalebiIptal();
                            }
                        });
                    }
                }
                else {
                    sohbetlerYolu.child(aktifKullaniciId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(alinanKullaniciId)){
                                        aktif_durum = "arkadaşlar";
                                        mesajGondermeTalebiButonu.setText("Bu sohbeti sil");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (aktifKullaniciId.equals(alinanKullaniciId)) {

            mesajGondermeTalebiButonu.setVisibility(View.INVISIBLE);

        } else {
            // MESAJ talebi gitsin
            mesajGondermeTalebiButonu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mesajGondermeTalebiButonu.setEnabled(false);
                    if (aktif_durum.equals("yeni")) {
                        sohbetTalebiGonder();
                    }
                    if (aktif_durum.equals("talep_gönderildi")){
                        MesajTalebiIptal();
                    }
                    if (aktif_durum.equals("talep_alindi")){
                        MesajTalebiKabul();
                    }
                    if (aktif_durum.equals("arkadaşlar")){
                        OzelSohbetSil();
                    }

                }
            });
        }
    }

    private void OzelSohbetSil() {

        //Sohbeti sil
        sohbetlerYolu.child(aktifKullaniciId).child(alinanKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //Talebi alandan sil
                    sohbetlerYolu.child(alinanKullaniciId).child(aktifKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                mesajGondermeTalebiButonu.setEnabled(true);
                                aktif_durum = "yeni";
                                mesajGondermeTalebiButonu.setText("Sohbet Talebi Gönder");

                                mesajDegerlendirmeTalebiButonu.setVisibility(View.INVISIBLE);
                                mesajDegerlendirmeTalebiButonu.setEnabled(false);
                            }

                        }
                    });
                }

            }
        });


    }

    private void MesajTalebiKabul() {
        sohbetlerYolu.child(aktifKullaniciId).child(alinanKullaniciId).child("Sohbetler").setValue("Kaydedildi")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            sohbetlerYolu.child(alinanKullaniciId).child(aktifKullaniciId).child("Sohbetler").setValue("Kaydedildi")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sohbetTalebiYolu.child(aktifKullaniciId).child(alinanKullaniciId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    sohbetTalebiYolu.child(alinanKullaniciId).child(aktifKullaniciId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    mesajGondermeTalebiButonu.setEnabled(true);

                                                                                    aktif_durum = "arkadaşlar";
                                                                                    mesajGondermeTalebiButonu.setText("Bu sohbeti sil");

                                                                                    mesajDegerlendirmeTalebiButonu.setVisibility(View.INVISIBLE);
                                                                                    mesajDegerlendirmeTalebiButonu.setEnabled(false);
                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });
                                            }

                                        }
                                    });
                        }
                    }
                });
    }

    private void MesajTalebiIptal() {
        //Talebi gödnerenden sil
        sohbetTalebiYolu.child(aktifKullaniciId).child(alinanKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //Talebi alandan sil
                    sohbetTalebiYolu.child(alinanKullaniciId).child(aktifKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                mesajGondermeTalebiButonu.setEnabled(true);
                                aktif_durum = "yeni";
                                mesajGondermeTalebiButonu.setText("Sohbet Talebi Gönder");

                                mesajDegerlendirmeTalebiButonu.setVisibility(View.INVISIBLE);
                                mesajDegerlendirmeTalebiButonu.setEnabled(false);
                            }

                        }
                    });
                }

            }
        });

    }

    private void sohbetTalebiGonder() {
        sohbetTalebiYolu.child(aktifKullaniciId).child(alinanKullaniciId).child("talep_turu").setValue("gönderildi")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            sohbetTalebiYolu.child(alinanKullaniciId).child(aktifKullaniciId).child("talep_turu")
                                    .setValue("alındı").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        //Bildirim için
                                        HashMap<String,String> chatBildirimMap = new HashMap<>();
                                        chatBildirimMap.put("kimden",aktifKullaniciId);
                                        chatBildirimMap.put("tur","talep");

                                        //bildiri Veritabanı YOlu
                                        bildirimYolu.child(alinanKullaniciId).push().setValue(chatBildirimMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){
                                                    mesajGondermeTalebiButonu.setEnabled(true);
                                                    aktif_durum = "talep_gönderildi";
                                                    mesajGondermeTalebiButonu.setText("Mesaj Talebi İptal");
                                                }

                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
    }


}

