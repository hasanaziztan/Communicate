package com.azo.sohbetuygulama;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class KayitActivity extends AppCompatActivity {
    private Button kayitOlusturmaButonu;
    private EditText kullaniciMail,kullaniciSifre;
    private TextView zatenHesabımVar;

    //firebase
    private DatabaseReference kokReference;
    FirebaseAuth mYetki;

    private ProgressDialog yükleniyorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kayit);

        //firebaseTanımlaması
        mYetki = FirebaseAuth.getInstance();
        kokReference = FirebaseDatabase.getInstance().getReference();

        //layout tanımlamaları
        kayitOlusturmaButonu = findViewById(R.id.kayit_butonu);
        kullaniciMail = findViewById(R.id.kayit_email);
        kullaniciSifre = findViewById(R.id.kayit_sifre);
        zatenHesabımVar = findViewById(R.id.zaten_hesap_var);

        //Progresbar
        yükleniyorDialog = new ProgressDialog(this);

        zatenHesabımVar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginActivityIntent = new Intent (KayitActivity.this,LoginActivity.class);
                startActivity(loginActivityIntent);
                finishAffinity();


            }
        });

        kayitOlusturmaButonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yeniHesapOlustur();
            }
        });

    }

    private void yeniHesapOlustur(){

        String email = kullaniciMail.getText().toString();
        String sifre = kullaniciSifre.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Email Boş Olamaz", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(sifre)){
            Toast.makeText(this, "Şifre Boş Olamaz", Toast.LENGTH_SHORT).show();
        }
        else{

            yükleniyorDialog.setTitle("Yeni hesap oluşturuluyor");
            yükleniyorDialog.setMessage("Lütfen Bekleyin");
            yükleniyorDialog.setCanceledOnTouchOutside(true);
            yükleniyorDialog.show();


            mYetki.createUserWithEmailAndPassword(email,sifre)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){

                                //bildirim id
                                String cihazToken = FirebaseInstanceId.getInstance().getToken();

                                String mevcutKullaniciId = mYetki.getCurrentUser().getUid();
                                kokReference.child("Kullanicilar").child(mevcutKullaniciId).setValue("");

                                kokReference.child("Kullanicilar").child(mevcutKullaniciId).child("cihaz_token")
                                        .setValue(cihazToken);


                                Intent anasayfa = new Intent(KayitActivity.this,MainActivity.class);
                                startActivity(anasayfa);
                                finishAffinity();
                                Toast.makeText(KayitActivity.this, "Yeni hesap oluşturuldu", Toast.LENGTH_SHORT).show();
                                yükleniyorDialog.dismiss();

                            }else {
                                String mesaj = task.getException().toString();
                                Toast.makeText(KayitActivity.this, "Hata"+ mesaj +"Bilgilerinizi Kontrol Edin", Toast.LENGTH_SHORT).show();
                                yükleniyorDialog.dismiss();
                            }
                        }
                    });

        }


    }

}
