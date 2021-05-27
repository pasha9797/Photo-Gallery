package ru.vsu.csf.pchernyshov.photogallery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import ru.vsu.csf.pchernyshov.photogallery.domain.Credentials;
import ru.vsu.csf.pchernyshov.photogallery.service.SaveSharedPreference;

public class MainActivity extends AppCompatActivity {
    private final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 153;
    private Credentials credentials = null;
    private Fragment feedFragment;
    private Fragment profileFragment;
    private FragmentManager fm = getSupportFragmentManager();
    private Fragment active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        credentials = SaveSharedPreference.getCredentials(getApplicationContext());

        setContentView(R.layout.activity_main);

        requestExternalStoragePermission();
    }

    private void requestExternalStoragePermission() {
        // Here, thisActivity is the current activity
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showExplanation("Permission Needed", "Rationale", Manifest.permission.READ_PHONE_STATE, MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            } else {
                // No explanation needed; request the permission
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            initFragments();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initFragments();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void initFragments() {
        if (fm.getFragments().size() == 0) {
            feedFragment = FeedFragment.newInstance(credentials);
            profileFragment = ProfileFragment.newInstance(credentials);
            active = feedFragment;
            fm.beginTransaction().add(R.id.main_container, profileFragment, "2").hide(profileFragment).commit();
            fm.beginTransaction().add(R.id.main_container, feedFragment, "1").commit();
        } else {
            feedFragment = fm.findFragmentByTag("1");
            profileFragment = fm.findFragmentByTag("2");
            active = feedFragment.isHidden() ? profileFragment : feedFragment;
        }
        BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = item -> {
            switch (item.getItemId()) {
                case R.id.action_feed:
                    fm.beginTransaction().hide(active).show(feedFragment).commit();
                    active = feedFragment;
                    return true;

                case R.id.action_profile:
                    fm.beginTransaction().hide(active).show(profileFragment).commit();
                    active = profileFragment;
                    return true;
            }
            return false;
        };
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }


    private void showExplanation(String title, String message, final String permission, final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, id) ->
                        requestPermissions(new String[]{permission}, permissionRequestCode));
        builder.create().show();
    }
}
