package com.azo.sohbetuygulama.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azo.sohbetuygulama.Model.Mesajlar;
import com.azo.sohbetuygulama.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MesajAdapter extends RecyclerView.Adapter<MesajAdapter.MesajlarViewHolder> {

    private List<Mesajlar> kullaniciMesajlariListesi;

    //Firebase
    private FirebaseAuth mYetki;
    private DatabaseReference kullanicilarYolu;

    //Adapter
    public MesajAdapter(List<Mesajlar> kullaniciMesajlariListesi) {
        this.kullaniciMesajlariListesi = kullaniciMesajlariListesi;
    }

    //ViewHolder
    public class MesajlarViewHolder extends RecyclerView.ViewHolder {

        //Ozel mesajlar layout item
        public TextView gonderenMesajMetni, aliciMesajMetni, bos;

        public MesajlarViewHolder(@NonNull View itemView) {
            super(itemView);

            bos = itemView.findViewById(R.id.profil_resim);
            aliciMesajMetni = itemView.findViewById(R.id.alici_mesaj_metni);
            gonderenMesajMetni = itemView.findViewById(R.id.gonderen_mesaj_metni);
        }
    }

    @NonNull
    @Override
    public MesajlarViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ozel_mesajlar_layout, viewGroup, false);

        //Firebase
        mYetki = FirebaseAuth.getInstance();
        return new MesajlarViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MesajlarViewHolder mesajlarViewHolder, int i) {
        String mesajGonderenId = mYetki.getCurrentUser().getUid();

        Mesajlar mesajlar = kullaniciMesajlariListesi.get(i);

        String kimdenKullaniciId = mesajlar.getKimden();
        String kimdenMesajTuru = mesajlar.getTur();

        //Veritabanı
        kullanicilarYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(kimdenKullaniciId);

        kullanicilarYolu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (kimdenMesajTuru.equals("metin")) {

            mesajlarViewHolder.aliciMesajMetni.setVisibility(View.INVISIBLE);
            mesajlarViewHolder.gonderenMesajMetni.setVisibility(View.INVISIBLE);


            if (kimdenKullaniciId.equals(mesajGonderenId)) {
                mesajlarViewHolder.gonderenMesajMetni.setVisibility(View.VISIBLE);
                mesajlarViewHolder.gonderenMesajMetni.setBackgroundResource(R.drawable.gonderen_mesajlari_layout);
                mesajlarViewHolder.gonderenMesajMetni.setTextColor(Color.BLACK);
                mesajlarViewHolder.gonderenMesajMetni.setText(mesajlar.getMesaj());
            } else {


                mesajlarViewHolder.aliciMesajMetni.setVisibility(View.VISIBLE);

                mesajlarViewHolder.aliciMesajMetni.setBackgroundResource(R.drawable.alici_mesajlari_layout);
                mesajlarViewHolder.aliciMesajMetni.setTextColor(Color.BLACK);
                mesajlarViewHolder.aliciMesajMetni.setText(mesajlar.getMesaj());
            }

        }

    }

    @Override
    public int getItemCount() {
        return kullaniciMesajlariListesi.size();
    }


}
