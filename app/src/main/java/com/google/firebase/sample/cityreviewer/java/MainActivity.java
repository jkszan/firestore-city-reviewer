package com.google.firebase.sample.cityreviewer.java;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.sample.cityreviewer.R;
import com.google.firebase.storage.FirebaseStorage;

public class  MainActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private FirebaseFirestoreSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        if (auth == null){
            auth = FirebaseAuth.getInstance();
            auth.useEmulator("10.0.2.2", 9099);
        }

        if (firestore == null){
            firestore = FirebaseFirestore.getInstance();
            firestore.useEmulator("10.0.2.2", 8080);
            FirebaseFirestore.setLoggingEnabled(true);
        }
        if (storage == null) {
            storage = FirebaseStorage.getInstance();
            storage.useEmulator("10.0.2.2", 9199);
        }
        if (settings == null) {
            settings = new FirebaseFirestoreSettings.Builder()
                    .build();
            firestore.setFirestoreSettings(settings);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(this.<Toolbar>findViewById(R.id.toolbar));

        Navigation.findNavController(this, R.id.nav_host_fragment)
                .setGraph(R.navigation.nav_graph_java);
    }
}
