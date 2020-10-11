package com.azo.sohbetuygulama;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AyarlarActivity extends AppCompatActivity {

    //Layout Tanımlamaları
    private Button hesapAyarlariniGuncelleme;
    private EditText kullaniciAdi, kullaniciDurumu;

    //Firebase
    private FirebaseAuth mYetki;
    private DatabaseReference veriYolu;

    private String mevcutKullaniciId;

    //Progres
    private ProgressDialog yukleniyorBar;

    Uri resimUri;
    String myUri = "";

    //Toolbar
    private Toolbar ayarlarToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayarlar);
        //Firebase
        mYetki = FirebaseAuth.getInstance();
        veriYolu = FirebaseDatabase.getInstance().getReference();
        mevcutKullaniciId = mYetki.getCurrentUser().getUid();

        //Layout tanıtma
        hesapAyarlariniGuncelleme = findViewById(R.id.ayarları_guncelleme_butonu);
        kullaniciAdi = findViewById(R.id.kullanici_adi_ayarla);
        kullaniciDurumu = findViewById(R.id.profil_durumu_ayarla);

        //Toolbar
        ayarlarToolbar = findViewById(R.id.ayarlar_toolbar);
        setSupportActionBar(ayarlarToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profil Ayarları");


        yukleniyorBar = new ProgressDialog(this);
        
        hesapAyarlariniGuncelleme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AyarlariGuncelle();
            }
        });

        KullaniciBilgisiAl();

    }

    private void KullaniciBilgisiAl() {
        veriYolu.child("Kullanicilar").child(mevcutKullaniciId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //içerisinde veri var ise
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("ad"))){
                            String kullaniciAdiAl = dataSnapshot.child("ad").getValue().toString();
                            String kullaniciDuruAl = dataSnapshot.child("durum").getValue().toString();

                            kullaniciAdi.setText(kullaniciAdiAl);
                            kullaniciDurumu.setText(kullaniciDuruAl);

                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("ad") )){
                            String kullaniciAdiAl = dataSnapshot.child("ad").getValue().toString();
                            String kullaniciDuruAl = dataSnapshot.child("durum").getValue().toString();

                            kullaniciAdi.setText(kullaniciAdiAl);
                            kullaniciDurumu.setText(kullaniciDuruAl);

                        }else {
                            Toast.makeText(AyarlarActivity.this, "Gerekli yerleri doldurunuz", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void AyarlariGuncelle() {
        String kullaniciAdiniAl = kullaniciAdi.getText().toString();
        String kullaniciDurumuAl = kullaniciDurumu.getText().toString();
        if (TextUtils.isEmpty(kullaniciAdiniAl)){
            Toast.makeText(this, "Ad boş olamaz!", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(kullaniciDurumuAl)){
            Toast.makeText(this, "Durum boş olamaz!", Toast.LENGTH_SHORT).show();
        }else {
            //firebase çoklu veri gönderimi
                HashMap<String,String> profilHaritasi = new HashMap<>();
                profilHaritasi.put("uid",mevcutKullaniciId);
                profilHaritasi.put("ad",kullaniciAdiniAl);
                profilHaritasi.put("durum",kullaniciDurumuAl);

                veriYolu.child("Kullanicilar").child(mevcutKullaniciId).setValue(profilHaritasi)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(AyarlarActivity.this, "Başarılı bir şekilde güncellendi", Toast.LENGTH_LONG).show();
                                    Intent anaSayfa = new Intent(AyarlarActivity.this,MainActivity.class);
                                    startActivity(anaSayfa);
                                    finishAffinity();
                                }else {
                                    String mesaj = task.getException().toString();
                                    Toast.makeText(AyarlarActivity.this, "Hata : " + mesaj, Toast.LENGTH_LONG).show();
                                }

                            }
                        });
        }
    }


}
