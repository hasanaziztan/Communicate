package com.azo.sohbetuygulama;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class KisilerFragment extends Fragment {

    private View kisilerView;

    private RecyclerView kisilerListem;

    //Firebase
    private FirebaseAuth mYetki;
    private DatabaseReference sohbetlerYolu,kullanıcılarYolu;

    private String aktifKullaniciId;


    public KisilerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        kisilerView =  inflater.inflate(R.layout.fragment_kisiler, container, false);

        //rectcler
        kisilerListem = kisilerView.findViewById(R.id.kisiler_listesi);
        kisilerListem.setLayoutManager(new LinearLayoutManager(getContext()));

        //Firebase
        mYetki = FirebaseAuth.getInstance();
        aktifKullaniciId = mYetki.getCurrentUser().getUid();
        sohbetlerYolu = FirebaseDatabase.getInstance().getReference().child("Sohbetler").child(aktifKullaniciId);
        kullanıcılarYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

        return kisilerView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions secenekler = new FirebaseRecyclerOptions.Builder<Kisiler>()
                .setQuery(sohbetlerYolu, Kisiler.class)
                .build();
        //Adapter
        FirebaseRecyclerAdapter<Kisiler,kisilerViewHolder> adapter = new FirebaseRecyclerAdapter<Kisiler, kisilerViewHolder>(secenekler) {
            @Override
            protected void onBindViewHolder(@NonNull final kisilerViewHolder holder, int position, @NonNull Kisiler model) {
                String tıklananSatırKullaniciIdsi = getRef(position).getKey();

                kullanıcılarYolu.child(tıklananSatırKullaniciIdsi).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            // veritabanı kullanıcı
                            if (dataSnapshot.child("kullaniciDurumu").hasChild("durum")){
                                String durum = dataSnapshot.child("kullaniciDurumu").child("durum").getValue().toString();
                                String tarih = dataSnapshot.child("kullaniciDurumu").child("tarih").getValue().toString();
                                String zaman = dataSnapshot.child("kullaniciDurumu").child("zaman").getValue().toString();

                                if (durum.equals("çevrimiçi")){
                                    holder.cevrimIciIconu.setVisibility(View.VISIBLE);
                                }
                                else if (durum.equals("çevrimdışı")){
                                    holder.cevrimIciIconu.setVisibility(View.INVISIBLE);
                                }

                            }else {
                                holder.cevrimIciIconu.setVisibility(View.INVISIBLE);

                            }

                            //VERi çekme
                            String kullaniciAdi = dataSnapshot.child("ad").getValue().toString();
                            String kullaniciDurumu = dataSnapshot.child("durum").getValue().toString();

                            //veri gösterme
                            holder.kullaniciAdi.setText(kullaniciAdi);
                            holder.kullaniciDurum.setText(kullaniciDurumu);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public kisilerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.kullanici_gosterme_layout,viewGroup,false);

                kisilerViewHolder viewHolder = new kisilerViewHolder(view);

                return viewHolder;
            }
        };
        kisilerListem.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();
    }
    public static  class kisilerViewHolder extends RecyclerView.ViewHolder {

        TextView kullaniciAdi,kullaniciDurum;
        ImageView cevrimIciIconu;

        public kisilerViewHolder(@NonNull View itemView) {
            super(itemView);

            kullaniciAdi=itemView.findViewById(R.id.kullanici_profil_adi);
            kullaniciDurum=itemView.findViewById(R.id.kullanici_durumu);
            cevrimIciIconu=itemView.findViewById(R.id.kullanici_cevrimici);
        }
    }


}
