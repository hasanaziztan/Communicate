package com.azo.sohbetuygulama;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.azo.sohbetuygulama.Model.Kisiler;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ArkadasBulActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView arkadasBulListe;

    //firebase
    private DatabaseReference kullaniciYolu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arkadas_bul);

        //Recycler Liste
        arkadasBulListe = findViewById(R.id.arkadas_bul_liste);
        arkadasBulListe.setLayoutManager(new LinearLayoutManager(this));

        //Toolbar
        mToolbar = findViewById(R.id.arkadas_bul_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Arkadaş Bul");

        //firebase
        kullaniciYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Basladıgında

        FirebaseRecyclerOptions<Kisiler> secenekler =
                new FirebaseRecyclerOptions.Builder<Kisiler>()
                        .setQuery(kullaniciYolu,Kisiler.class)
                        .build();
        // herbir item ne iş yapacağını
        FirebaseRecyclerAdapter<Kisiler,ArkadasBulViewHolder> adapter = new FirebaseRecyclerAdapter<Kisiler, ArkadasBulViewHolder>(secenekler) {
            @Override
            protected void onBindViewHolder(@NonNull ArkadasBulViewHolder holder, final int position, @NonNull Kisiler model) {
                holder.kullaniciAdi.setText(model.getAd());
                holder.kullaniciDurumu.setText(model.getDurum());

                //listeye tıkladığında
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String tiklananKullaniciIdGoster = getRef(position).getKey();

                        Intent profilAktivity = new Intent(ArkadasBulActivity.this,ProfilActivity.class);
                        profilAktivity.putExtra("tiklananKullaniciIdGoster",tiklananKullaniciIdGoster);
                        startActivity(profilAktivity);
                    }
                });

            }

            @NonNull
            @Override
            public ArkadasBulViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View  view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.kullanici_gosterme_layout,viewGroup,false);
                ArkadasBulViewHolder viewHolder = new ArkadasBulViewHolder(view);

                return viewHolder;
            }
        };
        arkadasBulListe.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();


    }

    //Recycler içerisinde kullanılan textviewler tanımlama işlemi
    public static class ArkadasBulViewHolder extends RecyclerView.ViewHolder{
        TextView kullaniciAdi,kullaniciDurumu;

        public ArkadasBulViewHolder(@NonNull View itemView) {
            super(itemView);
            //Tanımlamalar
            kullaniciAdi = itemView.findViewById(R.id.kullanici_profil_adi);
            kullaniciDurumu = itemView.findViewById(R.id.kullanici_durumu);
        }
    }
}
