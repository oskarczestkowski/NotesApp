package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.notesapp.databinding.ActivityUpdateBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UpdateAct extends AppCompatActivity {
    private String id, title, description;
    private NotesAdapter notesAdapter;
    ActivityUpdateBinding binding;
    private List<NotesModel> notesModelList;
    private List<NotesModel> OrgNotesModelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        Intent intent = getIntent();
        int position = intent.getIntExtra("position", -1);
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");

        notesModelList = (List<NotesModel>) intent.getSerializableExtra("notesList");
        OrgNotesModelList = (List<NotesModel>) intent.getSerializableExtra("orgNotesList");
        notesAdapter = new NotesAdapter(this);

        if (position != -1) {
            NotesModel notesModel = notesModelList.get(position);

            binding.title.setText(notesModel.getTitle());
            binding.description.setText(notesModel.getDescription());
        }


        binding.title.setText(title);
        binding.description.setText(description);
        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialog progressDialog = new ProgressDialog(view.getContext());
                progressDialog.setTitle("Deleting");
                FirebaseFirestore.getInstance()
                        .collection("notes")
                        .document(id)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(UpdateAct.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();


                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("position", position);
                                setResult(Activity.RESULT_OK, resultIntent);

                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UpdateAct.this, "Failed to delete note", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                e.printStackTrace();
                            }
                        });
            }
        });

        binding.saveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = binding.title.getText().toString();
                description = binding.description.getText().toString();
                updateNote();
            }
        });
    }

    private void updateNote() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating");
        progressDialog.setMessage("your note");
        progressDialog.show();
        NotesModel notesModel = new NotesModel(id, title, description, firebaseAuth.getUid());
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("notes")
                .document(id)
                .set(notesModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(UpdateAct.this, "Note Saved", Toast.LENGTH_SHORT).show();
                        progressDialog.cancel();
                        finish();

                        int noteIndex = -1;
                        for (int i = 0; i < notesModelList.size(); i++) {
                            if (notesModelList.get(i).getId().equals(id)) {
                                noteIndex = i;
                                break;
                            }
                        }

                        if (noteIndex != -1) {
                            notesModelList.set(noteIndex, notesModel);
                            OrgNotesModelList.set(noteIndex, notesModel);
                        }

                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UpdateAct.this, "Failed to update note", Toast.LENGTH_SHORT).show();
                        progressDialog.cancel();
                        e.printStackTrace();
                    }
                });
    }
}
