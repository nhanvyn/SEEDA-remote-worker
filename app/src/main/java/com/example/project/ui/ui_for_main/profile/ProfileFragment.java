package com.example.project.ui.ui_for_main.profile;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.project.R;
import com.example.project.model.Constants;
import com.example.project.model.Database;
import com.example.project.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProfileFragment extends Fragment {

    User user;
    Database db;

    EditText emailEdit, passwordEdit, firstNameEdit, lastNameEdit, birthdayEdit, phoneEdit,
            companyIdEdit;
    Button editBtn1, cancelBtn1, editBtn2, cancelBtn2, editBtn3, cancelBtn3;
    AtomicInteger btnCount1, btnCount2, btnCount3;
    List<EditText> editTextList1, editTextList2, editTextList3;

    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_profile, container, false);

        db = new Database(root.getContext());

        getUser();
        initializeVariables();

        setupEditAction(editTextList1, editBtn1, cancelBtn1, btnCount1, 1);
        setupEditAction(editTextList2, editBtn2, cancelBtn2, btnCount2, 2);

        updateUI();

        return root;
    }

    private void getUser() {
        SharedPreferences prefs = getActivity().getSharedPreferences(
                "user", Context.MODE_PRIVATE);

        String email = prefs.getString("email", "NONE");
        String password = prefs.getString("password", "NONE");

        user = db.getUser(email, password);
    }

    private void initializeVariables() {
        // First block
        btnCount1 = new AtomicInteger();

        emailEdit = root.findViewById(R.id.emailProfileEdit);
        passwordEdit = root.findViewById(R.id.passwordProfileEdit);

        editBtn1 = root.findViewById(R.id.editButtonProfile1);
        cancelBtn1 = root.findViewById(R.id.cancelButtonProfile1);

        editTextList1 = new ArrayList<>();
        editTextList1.add(emailEdit);
        editTextList1.add(passwordEdit);

        // Second Block
        btnCount2 = new AtomicInteger();

        firstNameEdit = root.findViewById(R.id.firstNameProfileEdit);
        lastNameEdit = root.findViewById(R.id.lastNameProfileEdit);
        birthdayEdit = root.findViewById(R.id.birthdayProfileEdit);
        phoneEdit = root.findViewById(R.id.phoneProfileEdit);
        companyIdEdit = root.findViewById(R.id.companyIdProfileEdit);

        editBtn2 = root.findViewById(R.id.editButtonProfile2);
        cancelBtn2 = root.findViewById(R.id.cancelButtonProfile2);

        editTextList2 = new ArrayList<>();
        editTextList2.add(firstNameEdit);
        editTextList2.add(lastNameEdit);
        editTextList2.add(birthdayEdit);
        editTextList2.add(phoneEdit);
        editTextList2.add(companyIdEdit);
    }

    private void setupEditAction(List<EditText> editTextList, Button editBtn,
                                 Button cancelBtn, AtomicInteger btnCount, int numOfBlock) {
        editBtn.setOnClickListener(v -> {
            if(btnCount.get() % 2 == 0) {
                for(EditText editText : editTextList) {
                    makeTextEditable(editText);
                }
                cancelBtn.setVisibility(View.VISIBLE);
                editBtn.setText("Save");
            } else {
                for(EditText editText : editTextList) {
                    makeTextNotEditable(editText);
                }
                cancelBtn.setVisibility(View.GONE);
                editBtn.setText("Edit");
                updateDataInDatabase1(numOfBlock);
            }
            btnCount.incrementAndGet();
        });

        cancelBtn.setOnClickListener(v -> {
            for(EditText editText : editTextList) {
                makeTextNotEditable(editText);
            }
            cancelBtn.setVisibility(View.GONE);
            editBtn.setText("Edit");
            btnCount.incrementAndGet();
            Toast.makeText(getContext(), "Canceled", Toast.LENGTH_SHORT).show();
            updateUI();
        });
    }

    private void makeTextEditable(EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setBackgroundResource(android.R.drawable.edit_text);
    }

    private void makeTextNotEditable(EditText editText) {
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setBackgroundResource(0);
        editText.setPadding(0, 0, 0 ,0);
    }

    private void updateUI() {
        emailEdit.setText(user.getEmail());
        passwordEdit.setText(user.getPassword());
        firstNameEdit.setText(user.getFirstName());
        lastNameEdit.setText(user.getLastName());
        birthdayEdit.setText(user.getBirthday());
        phoneEdit.setText(user.getPhone());
        companyIdEdit.setText(user.getCompanyID());
    }

    private void updateDataInDatabase1(int numOfBlock) {
        int result = 0;
        if(numOfBlock == 1) {
            ContentValues cv = new ContentValues();
            cv.put(Constants.EMAIL, emailEdit.getText().toString());
            cv.put(Constants.PASSWORD, passwordEdit.getText().toString());
            result = db.update(user.getId(), cv);
        } else if(numOfBlock == 2) {
            ContentValues cv = new ContentValues();
            cv.put(Constants.FIRST_NAME, firstNameEdit.getText().toString());
            cv.put(Constants.LAST_NAME, lastNameEdit.getText().toString());
            cv.put(Constants.BIRTHDAY, birthdayEdit.getText().toString());
            cv.put(Constants.PHONE, phoneEdit.getText().toString());
            cv.put(Constants.COMPANY_ID, companyIdEdit.getText().toString());
            result = db.update(user.getId(), cv);
        }
        if(result > 0) {
            Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        }
    }
}