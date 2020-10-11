package com.azo.sohbetuygulama;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.azo.sohbetuygulama.Adapter.MesajAdapter;
import com.azo.sohbetuygulama.Model.Mesajlar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String idMesajiAlici, adMesajiAlici,idMesajGonderen;
    private TextView kullaniciAdi,kullaniciSonGorulmesi;
    private ImageView sohbetSayfasina;

    private ImageButton mesajGondermeButonu;
    private EditText girilenMesajMetni;

    //Toolbar
    private Toolbar sohbetToolbar;

    private FirebaseAuth mYetki;
    private DatabaseReference mesajYolu,kullaniciYolu;
    private final List<Mesajlar> mesajlarList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MesajAdapter mesajAdapter;
    private RecyclerView kullaniciMesajlariListesi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Intent al
        idMesajiAlici = getIntent().getExtras().get("kullanici_id_ziyaret").toString();
        adMesajiAlici = getIntent().getExtras().get("kullanici_adi_ziyaret").toString();



        //Tanımlamalar layout
        kullaniciAdi = findViewById(R.id.kullanici_adi_gosterme_chat);
        kullaniciSonGorulmesi = findViewById(R.id.kullanici_durumu_gösterme_chat);
        sohbetSayfasina = findViewById(R.id.sohbet_sayfasina_gonderme);
        mesajGondermeButonu = findViewById(R.id.mesaj_gonder_btn);
        girilenMesajMetni = findViewById(R.id.girilen_mesaj);

        mesajAdapter = new MesajAdapter(mesajlarList);
        kullaniciMesajlariListesi = findViewById(R.id.kullanicilarin_ozel_mesaj_listesi);
        linearLayoutManager = new LinearLayoutManager(this);
        kullaniciMesajlariListesi.setLayoutManager(linearLayoutManager);
        kullaniciMesajlariListesi.setAdapter(mesajAdapter);

        //Firebase Tanımlama
        mYetki = FirebaseAuth.getInstance();
        mesajYolu = FirebaseDatabase.getInstance().getReference();
        kullaniciYolu = FirebaseDatabase.getInstance().getReference();
        idMesajGonderen = mYetki.getCurrentUser().getUid();

        sohbetSayfasina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sohbet = new Intent(ChatActivity.this,MainActivity.class);
                startActivity(sohbet);
            }
        });

        //Kontrollere Intent
        kullaniciAdi.setText(adMesajiAlici);

        //Mesajgönderimi
        mesajGondermeButonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                mesajGonder();

            }
        });


    }

    private void sonGorulmeyiGoster(){

        kullaniciYolu.child("Kullanicilar").child(idMesajGonderen).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // veritabanı kullanıcı
                if (dataSnapshot.child("kullaniciDurumu").hasChild("durum")){
                    String durum = dataSnapshot.child("kullaniciDurumu").child("durum").getValue().toString();
                    String tarih = dataSnapshot.child("kullaniciDurumu").child("tarih").getValue().toString();
                    String zaman = dataSnapshot.child("kullaniciDurumu").child("zaman").getValue().toString();

                    if (durum.equals("çevrimiçi")){
                        kullaniciSonGorulmesi.setText("çevrimiçi");
                    }
                    else if (durum.equals("çevrimdışı")){
                        kullaniciSonGorulmesi.setText("Son görülme" + tarih + "" + zaman );
                    }

                }else {
                    kullaniciSonGorulmesi.setText("çevrimdışı");

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();


        //Firebase veri alma
        mesajYolu.child("Mesajlar").child(idMesajGonderen).child(idMesajiAlici)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        //Veri tabanından veriyi alıp model aktarma
                        Mesajlar mesajlar = dataSnapshot.getValue(Mesajlar.class);

                        //modeli listeye ekle
                        mesajlarList.add(mesajlar);

                        mesajAdapter.notifyDataSetChanged();

                        //Scrolview kayırma
                        kullaniciMesajlariListesi.smoothScrollToPosition(kullaniciMesajlariListesi.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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


    private void mesajGonder() {
        //Mesajı alma
        
        String mesajMetni = girilenMesajMetni.getText().toString();
        
        if (TextUtils.isEmpty(mesajMetni)){
            Toast.makeText(this, "Mesaj metni boş olamaz", Toast.LENGTH_SHORT).show();
        }else {
            String mesajGonderenYolu = "Mesajlar/" + idMesajGonderen+ "/" + idMesajiAlici;
            String mesajAlanYolu = "Mesajlar/" + idMesajiAlici+ "/" + idMesajGonderen;

            DatabaseReference kullaniciMesajAnahtarYolu = mesajYolu.child("Mesajlar").child(idMesajGonderen).child(idMesajiAlici).push();

            String mesajEklemeId = kullaniciMesajAnahtarYolu.getKey();

            Map mesajMetniGovdesi = new HashMap();
            mesajMetniGovdesi.put("mesaj",mesajMetni);
            mesajMetniGovdesi.put("tur","metin");
            mesajMetniGovdesi.put("kimden",idMesajGonderen);

            Map mesajGovdesiDetaylari = new HashMap();
            mesajGovdesiDetaylari.put(mesajGonderenYolu + "/" + mesajEklemeId,mesajMetniGovdesi);
            mesajGovdesiDetaylari.put(mesajAlanYolu + "/" + mesajEklemeId,mesajMetniGovdesi);

            mesajYolu.updateChildren(mesajGovdesiDetaylari).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Mesaj gonderildi", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(ChatActivity.this, "Mesaj gönderilemedi", Toast.LENGTH_LONG).show();
                    }
                    girilenMesajMetni.setText("");
                }
            });

        }
    }
}
