package com.azo.sohbetuygulama;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private SekmeErisimAdapter mySekmeErisimAdapter;

    //Firebase
    private FirebaseAuth mYetki;
    private DatabaseReference kullanicilarReference;

    private String aktifKullaniciId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = findViewById(R.id.ana_sayfa_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("HoşBeş");

        myViewPager = findViewById(R.id.ana_sekmeler_pager);
        mySekmeErisimAdapter = new SekmeErisimAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(mySekmeErisimAdapter);

        myTabLayout = findViewById(R.id.ana_sekmeler);
        myTabLayout.setupWithViewPager(myViewPager);

        //Firebase
        mYetki = FirebaseAuth.getInstance();
        kullanicilarReference = FirebaseDatabase.getInstance().getReference();


    }

    @Override
    protected void onStart() {
        super.onStart();

       FirebaseUser mevcutKullanici = mYetki.getCurrentUser();

        if (mevcutKullanici == null) {
            kullaniciLoginActivityGonder();
        }else {
            kullaniciDurumuGuncelle("çevrimiçi");
            kullanicininVarliginiDogrula();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser mevcutKullanici = mYetki.getCurrentUser();

        if (mevcutKullanici != null){
            kullaniciDurumuGuncelle("çevrimdışı");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser mevcutKullanici = mYetki.getCurrentUser();

        if (mevcutKullanici != null){
            kullaniciDurumuGuncelle("çevrimdışı");
        }
    }

    private void kullanicininVarliginiDogrula() {
        String mevcutKullaniciId = mYetki.getCurrentUser().getUid();
        kullanicilarReference.child("Kullanicilar").child(mevcutKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("ad").exists())){
                    Toast.makeText(MainActivity.this, "Hoşgeldiniz", Toast.LENGTH_LONG).show();
                }else {
                    Intent ayarlar = new Intent(MainActivity.this,AyarlarActivity.class);
                    startActivity(ayarlar);
                    finishAffinity();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void kullaniciLoginActivityGonder() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finishAffinity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.secenekler_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.ana_arkadas_bulma_secenegi) {
            Intent arkadasBul = new Intent(MainActivity.this, ArkadasBulActivity.class);
            startActivity(arkadasBul);

        }
        if (item.getItemId() == R.id.ana_ayarlar_secenegi) {
            Intent ayarlar = new Intent(MainActivity.this, AyarlarActivity.class);
            startActivity(ayarlar);
        }
        if (item.getItemId() == R.id.ana_cikis_secenegi) {
            kullaniciDurumuGuncelle("çevrimdışı");
            mYetki.signOut();
            Intent giris = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(giris);
            finishAffinity();
        }
        if (item.getItemId() == R.id.ana_grup_olustur_secenegi) {
            yeniGrupTalebi();

        }
        return true;
    }

    private void yeniGrupTalebi() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Grup Adı Giriniz");

        final EditText grupAdiAlani = new EditText(MainActivity.this);
        grupAdiAlani.setHint("Grup Adi : HoşBeş");
        builder.setView(grupAdiAlani);

        builder.setPositiveButton("Oluştur", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String grupAdi = grupAdiAlani.getText().toString();
                if (TextUtils.isEmpty(grupAdi)){
                    Toast.makeText(MainActivity.this, "Grup adi boş bırakılamaz", Toast.LENGTH_LONG).show();
                }else {
                    YeniGrupOlustur(grupAdi);
                }

            }
        }).setNegativeButton("İptal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void YeniGrupOlustur(final String grupAdi) {
        kullanicilarReference.child("Gruplar").child(grupAdi).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MainActivity.this, grupAdi+" adlı grup oluşturuldu", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void kullaniciDurumuGuncelle(String durum){

        String kaydedilenAktifZaman, kaydedilenAktifTarih;

        Calendar calendar = Calendar.getInstance();

        //Tarih formatı
        SimpleDateFormat akrifTarih = new SimpleDateFormat("MM dd,yyyy");
        kaydedilenAktifTarih = akrifTarih.format(calendar.getTime());


        //Saat formatı
        SimpleDateFormat akrifZaman = new SimpleDateFormat("hh:mm a");
        kaydedilenAktifZaman = akrifZaman.format(calendar.getTime());

        HashMap<String,Object> cevrimiciDurumuMap = new HashMap<>();
        cevrimiciDurumuMap.put("zaman",kaydedilenAktifZaman);
        cevrimiciDurumuMap.put("tarih",kaydedilenAktifTarih);
        cevrimiciDurumuMap.put("durum",durum);

        aktifKullaniciId = mYetki.getCurrentUser().getUid();


        kullanicilarReference.child("Kullanicilar").child(aktifKullaniciId).child("kullaniciDurumu").updateChildren(cevrimiciDurumuMap);



    }
}
