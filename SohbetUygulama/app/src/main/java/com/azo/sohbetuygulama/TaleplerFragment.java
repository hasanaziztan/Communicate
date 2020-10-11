package com.azo.sohbetuygulama;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.azo.sohbetuygulama.Model.Kisiler;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class TaleplerFragment extends Fragment {

    private View taleplerFragmentView;

    private RecyclerView taleplerListem;

    //Firebase
    private DatabaseReference sohbetTalepleriYolu,kullanicilarYolu,sohbetlerYolu;
    private FirebaseAuth mYetki;

    private String aktifKullaniciId;

    public TaleplerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        taleplerFragmentView = inflater.inflate(R.layout.fragment_talepler, container, false);

        //Firebase
        mYetki = FirebaseAuth.getInstance();
        aktifKullaniciId = mYetki.getCurrentUser().getUid();
        sohbetTalepleriYolu = FirebaseDatabase.getInstance().getReference().child("Sohbet Talebi");
        kullanicilarYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        sohbetlerYolu = FirebaseDatabase.getInstance().getReference().child("Sohbetler");

        // Recyler
        taleplerListem = taleplerFragmentView.findViewById(R.id.sohbet_talep_listesi);
        taleplerListem.setLayoutManager(new LinearLayoutManager(getContext()));

        return taleplerFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Kisiler> secenekler = new FirebaseRecyclerOptions.Builder<Kisiler>()
                .setQuery(sohbetTalepleriYolu.child(aktifKullaniciId),Kisiler.class)
                .build();

        FirebaseRecyclerAdapter<Kisiler,TaleplerViewHolder> adapter = new FirebaseRecyclerAdapter<Kisiler, TaleplerViewHolder>(secenekler) {
            @Override
            protected void onBindViewHolder(@NonNull final TaleplerViewHolder holder, int position, @NonNull Kisiler model) {
                //Butonları gösterme
                holder.itemView.findViewById(R.id.talep_kabul_butonu).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.talep_iptal_butonu).setVisibility(View.VISIBLE);

                //Taleplerin hepsini al
                final String kullanici_id_listesi = getRef(position).getKey();

                DatabaseReference talepTuruAl = getRef(position).child("talep_turu").getRef();

                talepTuruAl.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String tur = dataSnapshot.getValue().toString();

                            if (tur.equals("alındı")) {
                                kullanicilarYolu.child(kullanici_id_listesi).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            final String talepKullaniciAdi = dataSnapshot.child("ad").getValue().toString();
                                            final String talepKullaniciDurumu = dataSnapshot.child("durum").getValue().toString();

                                            holder.kullaniciAdi.setText(talepKullaniciAdi);
                                            holder.kullaniciDurumu.setText("Kullanici Seninle iletişim kurmak istiyor");


                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CharSequence secenekler[] = new CharSequence[]{
                                                        "Kabul",
                                                        "İptal"
                                                };
                                                //AlertDialog
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(talepKullaniciAdi+" Chat Talebi");

                                                builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface  dialog, int which) {
                                                        if (which == 0){
                                                            sohbetlerYolu.child(aktifKullaniciId).child(kullanici_id_listesi).child("Sohbetler")
                                                                    .setValue("Kaydedildi").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        sohbetlerYolu.child(kullanici_id_listesi).child(aktifKullaniciId)
                                                                                .child("Sohbetler").setValue("Kaydedildi")
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            sohbetTalepleriYolu.child(aktifKullaniciId).child(kullanici_id_listesi)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()){
                                                                                                                sohbetTalepleriYolu.child(kullanici_id_listesi).child(aktifKullaniciId)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                Toast.makeText(getContext(), "Sohbet kaydedildi", Toast.LENGTH_LONG).show();
                                                                                                                            }
                                                                                                                        });
                                                                                                            }
                                                                                                            
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            });

                                                        }
                                                        if (which == 1){
                                                            sohbetTalepleriYolu.child(aktifKullaniciId).child(kullanici_id_listesi)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                sohbetTalepleriYolu.child(kullanici_id_listesi).child(aktifKullaniciId)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                Toast.makeText(getContext(), "Sohbet silindi", Toast.LENGTH_LONG).show();
                                                                                            }
                                                                                        });
                                                                            }

                                                                        }
                                                                    });



                                                        }


                                                    }
                                                });
                                                builder.show();
                                            }

                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            else if (tur.equals("gönderildi")){
                                Button talep_gonderme_btn = holder.itemView.findViewById(R.id.talep_kabul_butonu);

                                talep_gonderme_btn.setText("Talep Gönderildi");

                                //iptal butonu
                                holder.itemView.findViewById(R.id.talep_iptal_butonu).setVisibility(View.INVISIBLE);

                                //Yapistir
                                kullanicilarYolu.child(kullanici_id_listesi).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        final String talepKullaniciAdi = dataSnapshot.child("ad").getValue().toString();
                                        final String talepKullaniciDurumu = dataSnapshot.child("durum").getValue().toString();

                                        holder.kullaniciAdi.setText(talepKullaniciAdi);
                                        holder.kullaniciDurumu.setText("sen " + talepKullaniciAdi+ " adlı kullanıcıya talep gönderdin");


                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CharSequence secenekler[] = new CharSequence[]{
                                                        "Chat talebini iptal et"
                                                };
                                                //AlertDialog
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(talepKullaniciAdi+" Mevcut chat talebi var");

                                                builder.setItems(secenekler, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface  dialog, int which) {
                                                        if (which == 0){
                                                            sohbetTalepleriYolu.child(aktifKullaniciId).child(kullanici_id_listesi)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                sohbetTalepleriYolu.child(kullanici_id_listesi).child(aktifKullaniciId)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                Toast.makeText(getContext(), "Chat talebini sildiniz", Toast.LENGTH_LONG).show();
                                                                                            }
                                                                                        });
                                                                            }

                                                                        }
                                                                    });



                                                        }


                                                    }
                                                });
                                                builder.show();
                                            }

                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public TaleplerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.kullanici_gosterme_layout,viewGroup,false);

                TaleplerViewHolder holder = new TaleplerViewHolder(view);

                return holder;
            }
        };

        taleplerListem.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();

    }

    public static class TaleplerViewHolder extends RecyclerView.ViewHolder{

        TextView kullaniciAdi,kullaniciDurumu;
        Button kabul,iptal;

        public TaleplerViewHolder(@NonNull View itemView) {
            super(itemView);

            kullaniciAdi = itemView.findViewById(R.id.kullanici_profil_adi);
            kullaniciDurumu = itemView.findViewById(R.id.kullanici_durumu);
            kabul = itemView.findViewById(R.id.talep_kabul_butonu);
            iptal = itemView.findViewById(R.id.talep_iptal_butonu);
        }
    }

}
