package com.example.incesoscialmedia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    //RECYCLER VIEW
    ArrayList<String> userEmailFromFB;
    ArrayList<String> userCommentFromFB;
    ArrayList<String> userImageFromFB;

    FeddRecyclerAdapter feddRecyclerAdapter;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // menüyü bağlamak için
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.insta_options_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // item secilince ne yapılacak

        if(item.getItemId() == R.id.add_post){

            Intent intentToUpload = new Intent(FeedActivity.this, UploadActivity.class);
            startActivity(intentToUpload);
        }
        else if(item.getItemId() == R.id.sign_out){

            firebaseAuth.signOut();
            Intent intentToSignUp = new Intent(FeedActivity.this, SignUpActivity.class);
            startActivity(intentToSignUp);
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        userCommentFromFB = new ArrayList<>();
        userEmailFromFB = new ArrayList<>();
        userImageFromFB = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        getDataFromFirestore();

        // Recycler View
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        feddRecyclerAdapter = new FeddRecyclerAdapter(userEmailFromFB,userCommentFromFB,userImageFromFB);
        recyclerView.setAdapter(feddRecyclerAdapter);
    }

    public void getDataFromFirestore(){

        CollectionReference collectionReference = firebaseFirestore.collection("Posts");
        // SnapshotListener sürekli güncellediği için güncel verileri bize göstereceek(instagram anasayfadaki güncel postlar gibi)

        /* VERİLERİ RASTEGELE OKUMA
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>()
         */
        /* VERİLERİ FİLTRELEME
        Bu kod comment'i sadece Kelebek olan collection ları gösterir
         collectionReference.whereEqualTo("comment","Kelebek").addSnapshotListener(new EventListener<QuerySnapshot>()
         */

        // Aşağıdaki kodla tarihi yeniden eskiye doğru okuyacağız (orderBy= -e göre diz)
        collectionReference.orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if(e != null){ // eğer hata değişkeni (e) boş değilse
                    Toast.makeText(FeedActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }

                if(queryDocumentSnapshots != null){
                   for(DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                       Map<String,Object> data = snapshot.getData();

                       String comment = (String) data.get("comment");
                       String userEmail = (String) data.get("useremail");
                       String downloadUrl = (String) data.get("downloadurl");

                       userCommentFromFB.add(comment);
                       userEmailFromFB.add(userEmail);
                       userImageFromFB.add(downloadUrl);

                       feddRecyclerAdapter.notifyDataSetChanged();

                   }

                }
            }
        });
    }

}
