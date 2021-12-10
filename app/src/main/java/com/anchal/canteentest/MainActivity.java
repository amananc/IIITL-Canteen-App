package com.anchal.canteentest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{
    Button SignIn;
    Button SignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SignIn = (Button)findViewById(R.id.signIn_btn);
        SignUp = (Button)findViewById(R.id.signUp_btn);

        SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signIn_Intent = new Intent(MainActivity.this, com.anchal.canteentest.SignIn.class);
                startActivity(signIn_Intent);
            }
        });

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent SignUp_Intent = new Intent(MainActivity.this, com.anchal.canteentest.OTPAuth.class);
                startActivity(SignUp_Intent);
            }
        });
    }
}