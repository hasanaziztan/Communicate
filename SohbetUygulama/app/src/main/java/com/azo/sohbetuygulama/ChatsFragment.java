package com.azo.sohbetuygulama;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.azo.sohbetuygulama.Model.Kisiler;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View ozelSohbetlerView;
    private RecyclerView sohbetlerListesi;

    //Firebase
    private DatabaseReference sohbetlerYolu,kullaniciYolu;
    private FirebaseAuth mYetki;
    private String aktifKullaniciId;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ozelSohbetlerView = inflater.inflate(R.layout.fragment_chats, container, false);

        mYetki = FirebaseAuth.getInstance();
        aktifKullaniciId = mYetki.getCurrentUser().getUid();
        sohbetlerYolu = FirebaseDatabase.getInstance().getReference().child("Sohbetler").child(aktifKullaniciId);
        kullaniciYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

        sohbetlerListesi = ozelSohbetlerView.findViewById(R.id.sohbetler_listesi);
        sohbetlerListesi.setLayoutManager(new LinearLayoutManager(getContext()));

        return ozelSohbetlerView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Kisiler> secenekler = new FirebaseRecyclerOptions.Builder<Kisiler>()
                .setQuery(sohbetlerYolu,Kisiler.class)
                .build();

        FirebaseRecyclerAdapter<Kisiler,sohbetlerViewHolder> adapter = new FirebaseRecyclerAdapter<Kisiler, sohbetlerViewHolder>(secenekler) {
            @Override
            protected void onBindViewHolder(@NonNull final sohbetlerViewHolder holder, int position, @NonNull Kisiler model) {
                final String kullaniciIdleri = getRef(position).getKey();

                //Veritabanından veri çağırma
                kullaniciYolu.child(kullaniciIdleri).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            final String adAl = dataSnapshot.child("ad").getValue().toString();
                            final String durumAl = dataSnapshot.child("durum").getValue().toString();

                            holder.kullaniciAdi.setText(adAl);

                            // veritabanı kullanıcı
                            if (dataSnapshot.child("kullaniciDurumu").hasChild("durum")){
                                String durum = dataSnapshot.child("kullaniciDurumu").child("durum").getValue().toString();
                                String tarih = dataSnapshot.child("kullaniciDurumu").child("tarih").getValue().toString();
                                String zaman = dataSnapshot.child("kullaniciDurumu").child("zaman").getValue().toString();

                                if (durum.equals("çevrimiçi")){
                                    holder.kullaniciDurumu.setText("çevrimiçi");
                                }
                                else if (durum.equals("çevrimdışı")){
                                    holder.kullaniciDurumu.setText("Son görülme" + tarih + "" + zaman );
                                }

                            }else {
                                holder.kullaniciDurumu.setText("çevrimdışı");

                            }

                            //hersatıra tıklandığında
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //Chat sayfasına git
                                    Intent chat = new Intent(getContext(),ChatActivity.class);
                                    chat.putExtra("kullanici_id_ziyaret",kullaniciIdleri);
                                    chat.putExtra("kullanici_adi_ziyaret",adAl);
                                    startActivity(chat);
                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }

            @NonNull
            @Override
            public sohbetlerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.kullanici_gosterme_layout,viewGroup,false);

                return new sohbetlerViewHolder(view);
            }
        };

        sohbetlerListesi.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();
    }

    public static class sohbetlerViewHolder extends RecyclerView.ViewHolder {
        //kontroller
        TextView kullaniciAdi,kullaniciDurumu;

        public sohbetlerViewHolder(@NonNull View itemView) {
            super(itemView);

            kullaniciAdi = itemView.findViewById(R.id.kullanici_profil_adi);
            kullaniciDurumu = itemView.findViewById(R.id.kullanici_durumu);
        }
    }
}
