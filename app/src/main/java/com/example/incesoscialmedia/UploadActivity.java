package com.example.incesoscialmedia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {

    Bitmap selectedImage;
    ImageView imageView;
    EditText commentText;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    Uri imageData;

    // database için
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        imageView = findViewById(R.id.imageView);
        commentText = findViewById(R.id.commentText);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }
    public void post_upload(View view){

        if(imageData != null){

            // fotoların farklı isimleri olsun diye yoksa he en son seçilen resim database yüklenir
            UUID uuid = UUID.randomUUID();
            final String imageName = "images/" + uuid + ".jpg";
            storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(UploadActivity.this,"Seçilen Fotoğraf Paylaşıldı!",Toast.LENGTH_LONG).show();

                    // Yüklediğimiz resmin database deki url sini almak için:
                    StorageReference newReference = FirebaseStorage.getInstance().getReference(imageName);// yüklenen resim seçilir.
                    newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();

                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser(); // aktif olan kullanıcıyı seçtik
                            String userEmail = firebaseUser.getEmail();
                            String comment = commentText.getText().toString();

                            /*Firebase verileri collection olarak depoluyor. Bu collectionların key ve value'si oluyor.
                              key ve value'yi  HashMap kullanarak oluşturulur*/
                            HashMap<String, Object> postData = new HashMap<>();
                            postData.put("useremail",userEmail);
                            postData.put("downloadurl",downloadUrl);
                            postData.put("comment",comment);
                            postData.put("date", FieldValue.serverTimestamp()); // güncel tarihi verir

                            firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                    Intent intent = new Intent(UploadActivity.this, FeedActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // tüm açık aktiviteleri kapat
                                    startActivity(intent); // iki satır üstteki aktiviteyi açar(FeedActivity)
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                                }
                            });


                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    public void select_image(View view){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            // eğer ilk defa izin isteniyorsa
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
        else{
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGallery,2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 1){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // izin verilmişse
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == 2 && resultCode == RESULT_OK && data != null){
             imageData = data.getData();

            try {

                if(Build.VERSION.SDK_INT >= 28){
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedImage);
                }
                else {
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                    imageView.setImageBitmap(selectedImage);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
