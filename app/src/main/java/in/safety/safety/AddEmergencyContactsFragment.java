package in.safety.safety;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class AddEmergencyContactsFragment extends Fragment {

    private int counter=0;
    private ArrayList<String> phoneNumbers;
    private SharedPreferences loginStatusPref;

    private View view;
    private Button nextBtn;
    private Button addContact;
    private RecyclerView recyclerView;
    private SharedPreferences emergencyManager;

    public AddEmergencyContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        phoneNumbers = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_emergency_contacts, container, false);
        loginStatusPref = getActivity().getSharedPreferences("login_status", Context.MODE_PRIVATE);
        recyclerView = view.findViewById(R.id.recycler_view);
        nextBtn = view.findViewById(R.id.next_btn);
        nextBtn.setVisibility(View.GONE);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginStatusPref.edit().putBoolean("isLoggedIn", true).putBoolean("isAnonymous", true).apply();
                addContacts();
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
        });
        emergencyManager = getActivity().getSharedPreferences("emergency_details", Context.MODE_PRIVATE);
        addContact = view.findViewById(R.id.add_contact);
        final AlertDialog[] alertDialog = new AlertDialog[1];
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context;
                alertDialog[0] = null;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View dialogView = inflater.inflate(R.layout.dialog_add_emergency_contact, container, false);
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
                            alertDialog[0].dismiss();
                            counter++;
                            if(counter >= 2)
                                nextBtn.setVisibility(View.VISIBLE);
                            else
                                nextBtn.setVisibility(View.GONE);
                            if(counter >= 5)
                                addContact.setVisibility(View.GONE);
                            else
                                addContact.setVisibility(View.VISIBLE);
                            phoneNumbers.add(phone.getText().toString().trim());
                            updateRecycler();
                        }
                    }
                });
                alertDialog[0] = builder.show();
            }
        });
        return view;
    }

    private void addContacts() {
        for(int i=0; i<phoneNumbers.size();i++)
        {
            if(phoneNumbers.get(i) == null)
                continue;
            emergencyManager.edit().putString("emergencyContact"+(i+1), phoneNumbers.get(i)).apply();
        }
    }

    private void updateRecycler() {
        EmergencyContactsRecycler recycler = new EmergencyContactsRecycler(phoneNumbers.toArray(new String[phoneNumbers.size()]));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(recycler);
        recycler.setOnDeleteClickListener(new EmergencyContactsRecycler.OnDeleteClickListener() {
            @Override
            public void delete(int position) {
                phoneNumbers.remove(position);
                counter--;
                if(phoneNumbers.size()<2) {
                    nextBtn.setVisibility(View.GONE);
                    addContact.setVisibility(View.VISIBLE);
                }
                updateRecycler();
            }
        });
    }
}