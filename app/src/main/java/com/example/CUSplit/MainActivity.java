package com.example.CUSplit;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//GROUP 14
// Names: Kambar Nursultan, Munkhbileg Batdorj, Chu Tsz Chim James
// SID: 1155147668, 1155155853, 1155142348



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.CUSplit.Group.CreateNewGroupsActivity;
import com.example.CUSplit.Group.GroupListActivity;
import com.example.CUSplit.authorization.LoginActivity;
import com.example.CUSplit.utils.CurrencyExchangeCallback;
import com.example.CUSplit.utils.ExchangeRatesCallback;
import com.example.CUSplit.utils.ImageUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.JsonObject;

import android.provider.MediaStore.Images.Media;

import com.example.CUSplit.utils.LocationUtil;
import com.example.CUSplit.utils.CurrencyUtil;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationUtil.LocationResultListener {
    private DrawerLayout drawer;
    private LocationUtil locationUtil;
    public static String currencyCode;
    private double latitude, longitude;
    private double fxRate;
    private JsonObject allRates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fetch location and currency
        locationUtil = new LocationUtil(this, this);
        checkPermissionsAndLocate();

        // set toolbar
        Toolbar toolbar = findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0); // removes shadow/elevation between toolbar and status bar
        }
        setTitle("");

        // set drawer
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //handling nav bar
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        CircleImageView profilePic = headerView.findViewById(R.id.profile_image);
        TextView username = headerView.findViewById(R.id.username);
        Button logout = headerView.findViewById(R.id.logout);
        //for logout button
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);

                // Finish MainActivity to prevent it from being accessible after logout
                finish();
            }
        });
        //for profile Pic
        profilePic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    //if the storage permission is allowed, call openGallery() which opens the gallery of user
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI );
                    startActivityForResult(galleryIntent, 2);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 2);
                }
            }

        });

        //fetches profile image and username from firebase
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            //fetch username
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Assuming the username is stored under the key "username"
                        String uNname = dataSnapshot.child("username").getValue(String.class);
                        username.setText(uNname);  // Set the username in the TextView
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Failed to load username.", Toast.LENGTH_SHORT).show();
                }
            });


            //fetch profile image
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference profilePicRef = storage.getReference().child("Users/"+ userId);
            profilePicRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(MainActivity.this)
                            .load(uri)
                            .into(profilePic);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Toast.makeText(MainActivity.this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // get view references for "Groups" and "Create New group" Buttons
        View listGroups = findViewById(R.id.listGroups);
        View createNewGroup = findViewById(R.id.createNewGroup);

        // attach click listener to buttons
        listGroups.setOnClickListener(this);
        createNewGroup.setOnClickListener(this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
           Uri selectedImage = data.getData();
            ImageUtil.uploadImageToFirebase(selectedImage, this);

        }
    }


    private void checkPermissionsAndLocate() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationUtil.getLastLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //request code 1 corresponds to location Util
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationUtil.getLastLocation();
        }
        //request code 2 corresponds to storage permission (for updating image)
        else if (requestCode == 2 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI );
            startActivityForResult(galleryIntent, 2);
        } else{
                Toast.makeText(this, "Permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }

    //this method will update currency-related items when called.
    @Override
    public void onLocationResult(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

//        Toast.makeText(getApplicationContext(),"Latitude: "+latitude+", longitude: "+longitude,Toast.LENGTH_LONG).show();

        CurrencyUtil currencyUtil = new CurrencyUtil(getApplicationContext());
//        String countryCode = currencyUtil.getCountryCode(latitude, longitude);
        currencyCode = currencyUtil.getCurrencyCode(latitude, longitude);
//
//        Toast.makeText(getApplicationContext(), "Country Code: " + countryCode + ", Currency Code: " + currencyCode, Toast.LENGTH_LONG).show();

        currencyUtil.getAllExchangeRate(latitude, longitude, new CurrencyExchangeCallback() {
            @Override
            public void onResult(double rate) {
                Log.d("ExchangeRate", "Rate to HKD: " + rate);
                fxRate = rate;
                NavigationView navigationView = findViewById(R.id.nav_view);
                View headerView = navigationView.getHeaderView(0);
                Button curr = headerView.findViewById(R.id.currency);
                Button logout = headerView.findViewById(R.id.logout);

                curr.setText(currencyCode + " to HKD: " + fxRate);
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "You're using USD! Exchange rate of HKD to "+ currencyCode + ": " + fxRate, Toast.LENGTH_LONG).show());
            }

            @Override
            public void onError(Exception e) {
                Log.e("ExchangeRateError", "Error fetching exchange rate", e);
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to fetch exchange rate", Toast.LENGTH_LONG).show());
            }
        });
//        Toast.makeText(getApplicationContext(), "Exchange rate: " + fxRate, Toast.LENGTH_LONG).show();

        CurrencyUtil.getExchangeRateToHKD(new ExchangeRatesCallback() {
            @Override
            public void onCallback(JsonObject rates) {
                allRates = rates;
            }
            @Override
            public void onError(Exception e) {
                System.err.println("Error loading exchange rates: " + e.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.mainMenuShare) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Here is the share content body";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }
        return super.onOptionsItemSelected(item);
    }

    // method for handling clicks on our buttons
    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()) {
            case R.id.listGroups : intent = new Intent(this, GroupListActivity.class);startActivity(intent);break;
            case R.id.createNewGroup:
                intent = new Intent(this, CreateNewGroupsActivity.class);startActivity(intent);break;
            default:break;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            // close the drawer if user clicks on back button while drawer is open
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
