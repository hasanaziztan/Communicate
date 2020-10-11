package com.azo.sohbetuygulama;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class GrupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton mesajGonderButonu;
    private EditText kullaniciMesajiGirdisi;
    private ScrollView mScrollView;
    private TextView metinMesajlariniGoster;

    //Firebase
    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference kullaniciYolu, grupAdiYolu, grupMesajAnahtarYolu;

    // intent değişkeni
    private String mevcutGrupAdi, aktifKullaniciId, aktifKullaniciAdi, aktifTarih, aktifZaman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grup_chat);
        //intent al
        mevcutGrupAdi = getIntent().getExtras().get("grupAdı").toString();
        Toast.makeText(this, mevcutGrupAdi, Toast.LENGTH_LONG).show();

        // Firebase Tanımlamalari
        //firebase tanımlamaları
        mAuth = FirebaseAuth.getInstance();
        aktifKullaniciId = mAuth.getCurrentUser().getUid();
        kullaniciYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        grupAdiYolu = FirebaseDatabase.getInstance().getReference().child("Gruplar").child(mevcutGrupAdi);

        // Layout tanımlamaları
        mToolbar = findViewById(R.id.grup_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(mevcutGrupAdi);

        mScrollView = findViewById(R.id.my_scroll_view);
        mesajGonderButonu = findViewById(R.id.mesaj_gonderme_butonu);
        kullaniciMesajiGirdisi = findViewById(R.id.grup_mesaj_girdisi);
        metinMesajlariniGoster = findViewById(R.id.grup_chat_metni_gösterme);

        //Kullanıcının bilgisini alma
        KullaniciBilgisiAl();

        //mesajı veri tabanına kayıt
        mesajGonderButonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mesajiVeritabaninaKaydet();

                kullaniciMesajiGirdisi.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        grupAdiYolu.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    mesajlariGoster(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    mesajlariGoster(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {


            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void mesajlariGoster(DataSnapshot dataSnapshot) {

        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String sohbetTarihi = (String) ((DataSnapshot)iterator.next()).getValue();
            String sohbetMesaji = (String) ((DataSnapshot)iterator.next()).getValue();
            String sohbetAdi = (String) ((DataSnapshot)iterator.next()).getValue();
            String sohbetZamani = (String) ((DataSnapshot)iterator.next()).getValue();

            metinMesajlariniGoster.append(sohbetAdi +" :\n"+ sohbetMesaji +"\n"+sohbetZamani+"  "+sohbetTarihi+"\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void mesajiVeritabaninaKaydet() {
        String mesaj = kullaniciMesajiGirdisi.getText().toString();
        String mesajAnahtari = grupAdiYolu.push().getKey();


        if (TextUtils.isEmpty(mesaj)) {
            Toast.makeText(this, "Mesaj alanı Boş olamaz", Toast.LENGTH_SHORT).show();
        } else {

            Calendar tarih = Calendar.getInstance();
            SimpleDateFormat aktifTarihFormati = new SimpleDateFormat("MMM dd,yyyy");
            aktifTarih = aktifTarihFormati.format(tarih.getTime());

            Calendar zaman = Calendar.getInstance();
            SimpleDateFormat aktifZamanFormati = new SimpleDateFormat("hh:mm:ss a");
            aktifZaman = aktifZamanFormati.format(zaman.getTime());

            HashMap<String, Object> grupMesajAnahtari = new HashMap<>();
            grupAdiYolu.updateChildren(grupMesajAnahtari);

            grupMesajAnahtarYolu = grupAdiYolu.child(mesajAnahtari);

            HashMap<String, Object> mesajBilgisiMap = new HashMap<>();

            mesajBilgisiMap.put("ad", aktifKullaniciAdi);
            mesajBilgisiMap.put("mesaj", mesaj);
            mesajBilgisiMap.put("tarih", aktifTarih);
            mesajBilgisiMap.put("zaman", aktifZaman);

            grupMesajAnahtarYolu.updateChildren(mesajBilgisiMap);
        }


    }

    private void KullaniciBilgisiAl() {
        kullaniciYolu.child(aktifKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    aktifKullaniciAdi = dataSnapshot.child("ad").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
