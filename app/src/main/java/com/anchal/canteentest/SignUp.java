package com.anchal.canteentest;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.anchal.canteentest.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity
{
    EditText name_editText;
    EditText phone_editText;
    EditText email_editText;
    EditText password_editText;

    Button signUp_button;

    // This pattern will check if our password contains all the below listed digits.
    final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";

    /*
        ^                 # start-of-string
        (?=.*[0-9])       # a digit must occur at least once
        (?=.*[a-z])       # a lower case letter must occur at least once
        (?=.*[A-Z])       # an upper case letter must occur at least once
        (?=.*[@#$%^&+=])  # a special character must occur at least once you can replace with your special characters
        (?=\\S+$)         # no whitespace allowed in the entire string
        .{4,}             # anything, at least six places though
        $
                        # end-of-string
     */


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        name_editText = (EditText)findViewById(R.id.name_ediText);
        phone_editText = (EditText)findViewById(R.id.phone_ediText);
        email_editText = (EditText)findViewById(R.id.email_editText);
        password_editText = (EditText)findViewById(R.id.password_ediText);

        signUp_button = (Button)findViewById(R.id.signUp_button);




        phone_editText.setText(OTPAuth.getPhoneNo().toString());
        phone_editText.setEnabled(false);



        // Getting reference of database .
        // This database is associated with firebase account of email account LIT2019007@iiitl.ac.in
        final FirebaseDatabase database = FirebaseDatabase.getInstance("https://fir-app-36b01-default-rtdb.asia-southeast1.firebasedatabase.app/");
        final DatabaseReference customer = database.getReference("User");


        signUp_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                customer.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(phone_editText.getText().toString()).exists())
                        {
                            // If the given phone number is already present then we can't allow it to be registered again.
                            Toast.makeText(SignUp.this, "This Phone Number is registered.", Toast.LENGTH_SHORT).show();
                        }

                        else if(!isEmailValid((email_editText.getText().toString())))
                        {
                            // If the email provided is invalid then we will show a toast to the user.
                            Toast.makeText(SignUp.this, "Invalid Email Address", Toast.LENGTH_SHORT).show();
                        }

                        else if(!isPasswordValid(password_editText.getText().toString()))
                        {
                            // If the password lacks any requirements then we will show this toast to the user.
                            Toast.makeText(SignUp.this, "Invalid password.\nPassword should contain:\n1 digit, 1 lowercase letter, 1 uppercase letter, 1 special symbol\nMinimum length = 8 and Maximum length = 10", Toast.LENGTH_SHORT).show();
                        }

                        // Else everything looks good, so we will register him as a new user.
                        else
                        {
                            User user = new User(name_editText.getText().toString(), password_editText.getText().toString(), email_editText.getText().toString());

                            // Creating a new node inside EatItDB, inside the user.
                            customer.child(phone_editText.getText().toString()).setValue(user);
                            Toast.makeText(SignUp.this, "You've Successfully Signed Up!", Toast.LENGTH_SHORT).show();

                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Toast.makeText(SignUp.this, "Please Login or Signup to use the App", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Checking if the email is valid.
    private boolean isEmailValid(CharSequence email)
    {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //checking if the password contains all the above constraints.
    public boolean isPasswordValid(final String password)
    {
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);

        return matcher.matches();
    }
}