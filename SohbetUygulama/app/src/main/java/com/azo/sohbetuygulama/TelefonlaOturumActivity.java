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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class TelefonlaOturumActivity extends AppCompatActivity {

    private Button dogrulamaKodBtn,girisBtn;
    private EditText telefonNumara,kodNumara;
    private TextView telefonTw,dogrulamaTw;

    //telefon
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String myDogrulamaId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    //Firebase
    FirebaseAuth mAuth;

    private ProgressDialog yukleniyor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telefonla_oturum);
        dogrulamaKodBtn=findViewById(R.id.dogrulama_butonu_gonder_butonu);
        girisBtn = findViewById(R.id.dogrulama_butonu);
        telefonNumara = findViewById(R.id.telefon_numarasi_girdi);
        kodNumara = findViewById(R.id.dogrulama_kodu_girisi);
        telefonTw = findViewById(R.id.telefon_numarasi);
        dogrulamaTw = findViewById(R.id.dogrulama_kodu);

        //firebase
        mAuth = FirebaseAuth.getInstance();

        yukleniyor = new ProgressDialog(this);

        dogrulamaKodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String telefonNumarasi = telefonNumara.getText().toString();
                if (TextUtils.isEmpty(telefonNumarasi)){
                    Toast.makeText(TelefonlaOturumActivity.this, "BoşBırakılamaz", Toast.LENGTH_LONG).show();
                }else{

                    yukleniyor.setTitle("Telefonla Doğrulama");
                    yukleniyor.setMessage("Lütfen bekleyin");
                    yukleniyor.setCanceledOnTouchOutside(false);
                    yukleniyor.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            telefonNumarasi,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            TelefonlaOturumActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }

            }
        });

        girisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dogrulamaKodBtn.setVisibility(View.INVISIBLE);
                telefonNumara.setVisibility(View.INVISIBLE);
                telefonTw.setVisibility(View.INVISIBLE);

                String dogrulamaKodu = kodNumara.getText().toString();

                if (TextUtils.isEmpty(dogrulamaKodu)){
                    Toast.makeText(TelefonlaOturumActivity.this, "Doğrulama kodu boş olamaz", Toast.LENGTH_LONG).show();
                }else {

                    yukleniyor.setTitle("Kodla Doğrulama");
                    yukleniyor.setMessage("Lütfen bekleyin");
                    yukleniyor.setCanceledOnTouchOutside(false);
                    yukleniyor.show();


                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(myDogrulamaId, dogrulamaKodu);
                    telefonlaGirisYap(credential);
                }

            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                telefonlaGirisYap(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                yukleniyor.dismiss();



                Toast.makeText(TelefonlaOturumActivity.this, "Geçersiz Numara Girdiniz", Toast.LENGTH_LONG).show();

                dogrulamaKodBtn.setVisibility(View.VISIBLE);
                girisBtn.setVisibility(View.INVISIBLE);
                telefonTw.setVisibility(View.VISIBLE);
                dogrulamaTw.setVisibility(View.INVISIBLE);
                telefonNumara.setVisibility(View.VISIBLE);
                kodNumara.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                myDogrulamaId = verificationId;
                mResendToken = token;

                yukleniyor.dismiss();

                Toast.makeText(TelefonlaOturumActivity.this, "Kod Gönderildi", Toast.LENGTH_SHORT).show();

                dogrulamaKodBtn.setVisibility(View.INVISIBLE);
                girisBtn.setVisibility(View.VISIBLE);
                telefonTw.setVisibility(View.INVISIBLE);
                dogrulamaTw.setVisibility(View.VISIBLE);
                telefonNumara.setVisibility(View.INVISIBLE);
                kodNumara.setVisibility(View.VISIBLE);


            }
        };
    }

    private void telefonlaGirisYap(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            yukleniyor.dismiss();
                            Toast.makeText(TelefonlaOturumActivity.this, "Oturum açıldı", Toast.LENGTH_LONG).show();
                            KullaniciyiAnaSayfaGonder();

                        } else {
                            String hataMesaj = task.getException().toString();

                            Toast.makeText(TelefonlaOturumActivity.this, "Hata :"+hataMesaj, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void KullaniciyiAnaSayfaGonder() {
        Intent anaSayfa = new Intent(TelefonlaOturumActivity.this,MainActivity.class);
        startActivity(anaSayfa);
        finishAffinity();
    }

}
