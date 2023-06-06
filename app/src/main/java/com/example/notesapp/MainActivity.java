package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.example.notesapp.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private NotesAdapter notesAdapter;
    private List<NotesModel> notesModelList;
    private List<NotesModel> OrgNotesModelList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityMainBinding.inflate((getLayoutInflater()));
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("NotesApp");

        notesModelList = new ArrayList<>();
        OrgNotesModelList = new ArrayList<>();

        notesAdapter=new NotesAdapter(this);
        binding.notesRecycler.setAdapter(notesAdapter);
        binding.notesRecycler.setLayoutManager(new LinearLayoutManager(this));

        binding.floatingADDbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,AddActivity.class);
                startActivity(intent);
            }
        });
        binding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text=editable.toString();
                if(text.length()>0){
                    filter(text);
                }else {
                    notesAdapter.filterList(notesModelList);

                }

            }
        });
    }

    private void filter(String text) {
        List<NotesModel> adapterList = notesAdapter.getList();
        List<NotesModel> filteredList = new ArrayList<>();
        if (text.isEmpty()) {
            filteredList.addAll(OrgNotesModelList); // Przywróć oryginalną listę notatek
        } else {
            for (NotesModel notesModel : adapterList) {
                if (notesModel.getTitle().toLowerCase(Locale.getDefault()).contains(text.toLowerCase())
                        || notesModel.getDescription().toLowerCase(Locale.getDefault()).contains(text.toLowerCase())) {
                    filteredList.add(notesModel);
                }
            }
        }
        notesAdapter.filterList(filteredList);
    }



    @Override
    protected void onStart() {
        super.onStart();
        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Checking User");
        progressDialog.setMessage("in process");
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser()==null) {
            progressDialog.show();
            firebaseAuth.signInAnonymously()
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            progressDialog.cancel();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.cancel();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            int position = data.getIntExtra("position", -1);
            if (position != -1) {
                String title = data.getStringExtra("title");
                String description = data.getStringExtra("description");

                NotesModel notesModel = notesModelList.get(position);
                notesModel.setTitle(title);
                notesModel.setDescription(description);
                notesAdapter.notifyItemChanged(position);
            }
        }
    }

    private void getData() {
        FirebaseFirestore.getInstance()
                .collection("notes")
                .whereEqualTo("uid",FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {


                        notesModelList.clear();
                        OrgNotesModelList.clear();

                        List<DocumentSnapshot> dsList = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot documentSnapshot : dsList) {
                            NotesModel notesModel = documentSnapshot.toObject(NotesModel.class);
                            notesModelList.add(notesModel);
                            OrgNotesModelList.add(notesModel);
                            notesAdapter.add(notesModel);
                        }
                        List<NotesModel> newList = new ArrayList<>(notesModelList);
                        notesAdapter.updateList(newList);
                        filter(binding.searchBar.getText().toString());

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    }


