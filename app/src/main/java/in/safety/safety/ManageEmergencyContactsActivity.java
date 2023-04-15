package in.safety.safety;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class ManageEmergencyContactsActivity extends AppCompatActivity {
    private ArrayList<String> phoneNumbers;
    private RecyclerView recyclerView;
    private SharedPreferences emergencyManager;
    private AlertDialog alertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_emergency_contacts);
        phoneNumbers = new ArrayList<>();
        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Emergency Contacts");
        }
        else if(getActionBar() != null)
        {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle("Emergency Contacts");
        }
        initializeViews();
    }

    private void initializeViews() {
        emergencyManager = getSharedPreferences("emergency_details", Context.MODE_PRIVATE);
        getContactsFromPref();
        recyclerView = findViewById(R.id.emergency_contacts_recycler);
        populateRecyclerView();
    }

    private void getContactsFromPref() {
        phoneNumbers = new ArrayList<>();
        for(int i=0;i<5;i++)
        {
            if(emergencyManager.getString("emergencyContact"+(i+1), null) == null)
                continue;
            phoneNumbers.add(emergencyManager.getString("emergencyContact"+(i+1), null));
        }
    }

    private void populateRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        EmergencyContactsRecycler adapter = new EmergencyContactsRecycler(phoneNumbers.toArray(new String[phoneNumbers.size()]));
        recyclerView.setAdapter(adapter);
        adapter.setOnDeleteClickListener(new EmergencyContactsRecycler.OnDeleteClickListener() {
            @Override
            public void delete(int position) {
                deleteContact(position);
            }
        });
    }

    private void deleteContact(int position) {
        phoneNumbers.remove(position);
        updateContacts();
        populateRecyclerView();
    }

    private void updateContacts()
    {
        for(int i=0;i<5;i++)
        {
            emergencyManager.edit().remove("emergencyContact"+(i+1)).apply();
        }
        for(int i=0;i<phoneNumbers.size();i++)
        {
            if(phoneNumbers.get(i) == null)
                continue;
            emergencyManager.edit().putString("emergencyContact"+(i+1), phoneNumbers.get(i)).apply();
        }
    }

    public void addContact(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_emergency_contact, null, false);
        builder.setView(dialogView);
        EditText phone = dialogView.findViewById(R.id.emergency_contact_input);
        Button btn = dialogView.findViewById(R.id.add_contact);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Patterns.PHONE.matcher(phone.getText().toString().trim()).matches())
                {
                    phone.setError("Invalid phone");
                    phone.requestFocus();
                }
                else
                {
                    alertDialog.dismiss();
                    phoneNumbers.add(phone.getText().toString().trim());
                    updateContacts();
                    populateRecyclerView();
                }
            }
        });
        alertDialog = builder.show();
    }
}