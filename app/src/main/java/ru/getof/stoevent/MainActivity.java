package ru.getof.stoevent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.getof.stoevent.InterfaceApi.SwitchAuthFragments;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MainActivity extends AppCompatActivity implements SwitchAuthFragments {

    public static final String SAVED_EMAIL = "EmailSave";
    public static final String SAVED_PASS = "PassSave";

    private Toolbar toolbarAuth;
    private NavController navController;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<String> phoneUser = new ArrayList<>();
    private SharedPreferences sPref;
    private ProgressBar progressBarAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbarAuth = findViewById(R.id.toolbarAuth);
        setSupportActionBar(toolbarAuth);
        progressBarAuth = findViewById(R.id.progressBarAuth);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                phoneUser.add(document.getString("phone"));
                            }
                        } else {
                            Log.w(TAG, "Ошибка получения данных.", task.getException());
                        }

                    }
                });

        loadAuth();

             navController = Navigation.findNavController(this, R.id.nav_host_authfragment);
        NavigationUI.setupActionBarWithNavController(this, navController);

    }

    private void saveAuth(String email, String pass){
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SAVED_EMAIL, email);
        ed.putString(SAVED_PASS, pass);
        ed.apply();
    }

    private void loadAuth(){

        sPref = getPreferences(MODE_PRIVATE);
        if (sPref.contains(SAVED_EMAIL)){
            progressBarAuth.setVisibility(View.VISIBLE);
            String savedEmail = sPref.getString(SAVED_EMAIL, "");
            String savedPass = sPref.getString(SAVED_PASS, "");


            mAuth.signInWithEmailAndPassword(savedEmail, savedPass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                db.collection("users").document(mAuth.getUid())
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                progressBarAuth.setVisibility(View.GONE);
                                                String userProf = documentSnapshot.get("prof").toString();
                                                String idsto = documentSnapshot.get("idsto").toString();
                                                Intent intent = new Intent(getApplicationContext(), EventsActivity.class);
                                                intent.putExtra("email", savedEmail);
                                                intent.putExtra("prof", userProf);
                                                intent.putExtra("idsto", idsto);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });

                            } else {
                                alertAuth();
                            }
                        }
                    });
        }
    }

    @Override
    public void PressButtonSignIn(final String userEmail, String userPass) {
        mAuth.signInWithEmailAndPassword(userEmail, userPass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            db.collection("users").document(mAuth.getUid())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            saveAuth(userEmail, userPass);
                                            String userProf = documentSnapshot.get("prof").toString();
                                            String idsto = documentSnapshot.get("idsto").toString();
                                            Intent intent = new Intent(getApplicationContext(), EventsActivity.class);
                                            intent.putExtra("email", userEmail);
                                            intent.putExtra("prof", userProf);
                                            intent.putExtra("idsto", idsto);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });

                        } else {
                            alertAuth();
                        }
                    }
                });

    }

    @Override
    public void PressButtonGetAcc() {
        navController.navigate(R.id.fragmentSignUp);
    }

    @Override
    public void PressButtonRegAcc(final String userProf, final String userPhone, final String userEmail, final String userPass, final String idSto) {
        boolean verify = true;

        for (int i=0;i<phoneUser.size();i++){
            if (phoneUser.get(i).equals(userPhone)){
                verify = false;
            }
        }

        if (verify) {
            mAuth.createUserWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Map<String, Object> userUp = new HashMap<>();
                                userUp.put("email", userEmail);
                                userUp.put("idsto", idSto);
                                userUp.put("phone", userPhone);
                                userUp.put("prof", userProf);
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null){
                                    db.collection("users")
                                            .document(user.getUid())
                                            .set(userUp)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getApplicationContext(),"Успешная регистрация!", Toast.LENGTH_LONG).show();
                                                    mAuth.signInWithEmailAndPassword(userEmail, userPass)
                                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                                    if (task.isSuccessful()){
                                                                        saveAuth(userEmail, userPass);
                                                                        Intent intent = new Intent(getApplicationContext(), EventsActivity.class);
                                                                        intent.putExtra("email", userEmail);
                                                                        intent.putExtra("prof", userProf);
                                                                        intent.putExtra("idsto", idSto);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    } else {
                                                                        Toast.makeText(getApplicationContext(),"Ошибка сети!", Toast.LENGTH_LONG).show();
                                                                    }
                                                                }
                                                            });

                                                }
                                            });
                                }
                            }
                        }
                    });
        } else {
            alertUser();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (navController.getCurrentDestination().getId() == R.id.fragmentSignUp){
                    navController.popBackStack();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void alertUser(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Ошибка");
        alertDialog.setMessage("Пользователь с таким телефоном\nуже зарегистрирован.\n" +
                "Регистрация не возможна!");
        alertDialog.setIcon(R.drawable.ic_error_outline_black_24dp);
        alertDialog.show();
    }

    private void alertAuth(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Ошибка");
        alertDialog.setMessage("Пользователь с такими данными\nне найден.\n" +
                "Авторизация не возможна!");
        alertDialog.setIcon(R.drawable.ic_error_outline_black_24dp);
        alertDialog.show();
    }


}
