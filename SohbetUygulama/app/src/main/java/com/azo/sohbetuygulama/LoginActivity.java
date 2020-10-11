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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    //Layout tanımlamaları
    private Button girisButonu,telefonlaGiris;
    private EditText kullaniciMail,kullaniciSifre;
    private TextView yeniHesapAlma,sifreUnutmaBaglantisi;

    //Firebase
    private FirebaseAuth mYetki;
    private DatabaseReference kullaniciYolu;

    //kullanıcıya bilgi vermek
    ProgressDialog girisDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //progres
        girisDialog = new ProgressDialog(this);

        //firebase bağlama metodları kullanma
        mYetki =FirebaseAuth.getInstance();
        kullaniciYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

        //Layout tanıtmaları
        girisButonu = findViewById(R.id.giris_butonu);
        telefonlaGiris = findViewById(R.id.telefonla_giris_butonu);
        kullaniciMail = findViewById(R.id.giris_email);
        kullaniciSifre = findViewById(R.id.giris_sifre);
        yeniHesapAlma = findViewById(R.id.yeni_hesap_alma);
        sifreUnutmaBaglantisi = findViewById(R.id.sifre_unutma_baglantisi);

        yeniHesapAlma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent kayitAktivityIntent = new Intent(LoginActivity.this,KayitActivity.class);
                startActivity(kayitAktivityIntent);
                finishAffinity();
            }
        });

        girisButonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kullaniciGirisYap();
            }
        });
        telefonlaGiris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                telefonlaKayit();
            }
        });
    }

    private void telefonlaKayit() {
        //Telefon kayıt aktivitisine geçiş ekranı
        Intent telefonlaGirisIntent = new Intent(LoginActivity.this,TelefonlaOturumActivity.class);
        startActivity(telefonlaGirisIntent);
        finishAffinity();
    }

    private void kullaniciGirisYap() {
        //edittextlere girilen metinleri alma
        String email = kullaniciMail.getText().toString();
        String sifre = kullaniciSifre.getText().toString();

        // email ve şifre boş olduğunda hata
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Email boş olmaz", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(sifre)){
            Toast.makeText(this, "Şifre boş olamaz", Toast.LENGTH_SHORT).show();
        }
        else{
            //Giriş yap butonuna tıklandığında kullanıcıya bilgi vermek
            girisDialog.setTitle("Giriş yapılıyor");
            girisDialog.setMessage("Lütfen Bekleyiniz");
            girisDialog.setCanceledOnTouchOutside(true);
            girisDialog.show();

            //giriş yapma işlemi e posta ve şifrenin veritabanındaki doğruluğunun kontrolü
            mYetki.signInWithEmailAndPassword(email,sifre)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                String aktifKullaniciId = mYetki.getCurrentUser().getUid();
                                String cihazToken = FirebaseInstanceId.getInstance().getToken();

                                kullaniciYolu.child(aktifKullaniciId).child("cihaz_token").setValue(cihazToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){

                                                    Intent anaSayfa = new Intent(LoginActivity.this,MainActivity.class);
                                                    startActivity(anaSayfa);
                                                    finishAffinity();
                                                    Toast.makeText(LoginActivity.this, "Giriş başarrılı", Toast.LENGTH_LONG).show();
                                                    girisDialog.dismiss();
                                                }
                                            }
                                        });
                            }

                            else {
                                String mesaj = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Hata:"+mesaj+"Bilgileri Kontrol ediniz", Toast.LENGTH_LONG).show();
                                girisDialog.dismiss();
                            }

                        }
                    });

        }

    }


    private void KullaniciyiAnaActivityeGonder() {
        Intent anaSayfa = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(anaSayfa);
    }
}
