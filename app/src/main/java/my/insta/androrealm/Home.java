package my.insta.androrealm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import my.insta.androrealm.Like.LikeFragment;
import my.insta.androrealm.Post.PostActivity;
import my.insta.androrealm.Profile.ProfileFragment;
import my.insta.androrealm.Search.SearchFragment;
import my.insta.androrealm.home.HomeFragment;

public class Home extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView navigationView = findViewById(R.id.insta_bottom_navigation);
        navigationView.setOnNavigationItemSelectedListener(this);

        // Load HomeFragment by default
        loadFragment(new HomeFragment());
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        // Replacing switch-case with if-else statements
        if (item.getItemId() == R.id.Home) {
            fragment = new HomeFragment();
        } else if (item.getItemId() == R.id.search) {
            fragment = new SearchFragment();
        } else if (item.getItemId() == R.id.post) {
            // Directly navigate to PostActivity, no fragment to replace
            startActivity(new Intent(Home.this, PostActivity.class));
            return true;
        } else if (item.getItemId() == R.id.likes) {
            fragment = new LikeFragment();
        } else if (item.getItemId() == R.id.profile) {
            fragment = new ProfileFragment();
        }

        // Ensure the fragment is loaded properly
        return loadFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> finish())
                .setNegativeButton("No", null)
                .show();
    }
}
