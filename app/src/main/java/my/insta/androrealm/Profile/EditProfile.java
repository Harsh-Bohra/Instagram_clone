package my.insta.androrealm.Profile;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import my.insta.androrealm.R;
import my.insta.androrealm.models.Users;
import my.insta.androrealm.models.privatedetails;

public class EditProfile extends AppCompatActivity {

    ImageView mProfilePhoto, submit;
    TextInputEditText name, username, bio, website;
    TextView Email, Phonenumber, Gender, Birth;
    String Name, Username, Bio, Website;
    Uri imageUri;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize UI components
        mProfilePhoto = findViewById(R.id.user_img);
        name = findViewById(R.id.Namee);
        username = findViewById(R.id.Usernamee);
        bio = findViewById(R.id.Bioo);
        website = findViewById(R.id.Websitee);
        submit = findViewById(R.id.rightt);
        Email = findViewById(R.id.email);
        Phonenumber = findViewById(R.id.phonenumber);
        Gender = findViewById(R.id.gender);
        Birth = findViewById(R.id.birth);

        storageReference = FirebaseStorage.getInstance().getReference();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Retrieve data from Firebase
        loadUserProfileData(userId);

        // Register image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        mProfilePhoto.setImageURI(imageUri);
                    }
                }
        );

        mProfilePhoto.setOnClickListener(v -> openFileChooser());
        submit.setOnClickListener(v -> updateProfile());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }

    private void loadUserProfileData(String userId) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                if (user != null) {
                    name.setText(user.getFullName());
                    username.setText(user.getUsername());
                    bio.setText(user.getDiscription());
                    website.setText(user.getWebsite());
                    if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
                        Glide.with(EditProfile.this).load(user.getProfilePhoto()).into(mProfilePhoto);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfile.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference privateDetailsRef = FirebaseDatabase.getInstance().getReference("Privatedetails").child(userId);
        privateDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                privatedetails privatedetail = snapshot.getValue(privatedetails.class);
                if (privatedetail != null) {
                    Email.setText(privatedetail.getEmail());
                    Phonenumber.setText(privatedetail.getPhoneNumber());
                    Gender.setText(privatedetail.getGender());
                    Birth.setText(privatedetail.getBirthdate());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfile.this, "Failed to load private details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        Name = name.getText().toString().trim();
        Username = username.getText().toString().trim();
        Bio = bio.getText().toString().trim();
        Website = website.getText().toString().trim();

        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setCancelable(false);
        mDialog.setMessage("Updating, please wait...");
        mDialog.show();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("Username").equalTo(Username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(EditProfile.this, "Username already exists. Please try another username.", Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();
                } else {
                    updateUserDataInFirebase(mDialog);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mDialog.dismiss();
                Toast.makeText(EditProfile.this, "Error checking username.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserDataInFirebase(ProgressDialog mDialog) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = databaseReference.child(userId);

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("fullName", Name);
        updates.put("username", Username);
        updates.put("discription", Bio);
        updates.put("website", Website);

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (imageUri != null) {
                    uploadProfilePhoto(mDialog, userId, userRef);
                } else {
                    mDialog.dismiss();
                    Toast.makeText(EditProfile.this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditProfile.this, Account_Settings.class));
                    finish();
                }
            } else {
                mDialog.dismiss();
                Toast.makeText(EditProfile.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadProfilePhoto(ProgressDialog mDialog, String userId, DatabaseReference userRef) {
        StorageReference photoRef = storageReference.child("photos/users/" + userId + "/profilephoto");
        photoRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    userRef.child("profilePhoto").setValue(uri.toString());
                    mDialog.dismiss();
                    Toast.makeText(EditProfile.this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditProfile.this, Account_Settings.class));
                    finish();
                });
            }
        });
    }
}
