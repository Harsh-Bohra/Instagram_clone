package my.insta.androrealm.Post;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import my.insta.androrealm.Home;
import my.insta.androrealm.R;

public class PostActivity extends AppCompatActivity {

    ImageView postNow, backFromPost, addedImage;
    EditText addedCaption, AddedTag;
    DatabaseReference databaseReference;

    Uri imageUri;
    String RandomUId, userId;
    String caption, tags;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postNow = findViewById(R.id.post_now);
        backFromPost = findViewById(R.id.back_from_post);
        addedImage = findViewById(R.id.added_image);
        addedCaption = findViewById(R.id.added_caption);
        AddedTag = findViewById(R.id.added_tags);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Registering the ActivityResultLauncher for image picking
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            imageUri = data.getData();
                            addedImage.setImageURI(imageUri);
                        }
                    }
                });

        backFromPost.setOnClickListener(v -> {
            startActivity(new Intent(PostActivity.this, Home.class));
            finish();
        });

        postNow.setOnClickListener(v -> {
            if (imageUri != null) {
                try {
                    saveImageLocallyAndPost();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(PostActivity.this, "Error saving image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PostActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        });

        openFileChooser();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }

    private void saveImageLocallyAndPost() throws IOException {
        // Convert URI to Bitmap
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

        // Save to internal storage
        String filename = "IMG_" + System.currentTimeMillis() + ".jpg";
        File file = new File(getFilesDir(), filename);
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.close();

        // Get the file path and save post details to Firebase Database
        String filePath = file.getAbsolutePath();
        savePostToFirebase(filePath);
    }

    private void savePostToFirebase(String filePath) {
        caption = addedCaption.getText().toString().trim();
        tags = AddedTag.getText().toString().trim();
        RandomUId = UUID.randomUUID().toString();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        HashMap<String, String> postMap = new HashMap<>();
        postMap.put("caption", caption);
        postMap.put("date_Created", getTimestamp());
        postMap.put("image_Path", filePath); // Store local file path
        postMap.put("photo_id", RandomUId);
        postMap.put("tags", tags);
        postMap.put("user_id", userId);

        databaseReference.child("User_Photo").child(userId).child(RandomUId).setValue(postMap);
        databaseReference.child("Photo").child(RandomUId).setValue(postMap);

        Toast.makeText(PostActivity.this, "Posted successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(PostActivity.this, Home.class));
        finish();
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}