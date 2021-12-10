package com.anchal.canteentest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.anchal.canteentest.Database.Database;
import com.anchal.canteentest.Model.Food;
import com.anchal.canteentest.Model.Order;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FoodDetails extends AppCompatActivity {

    TextView food_name, food_price, food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    Button btnCart;
    ElegantNumberButton elegantNumberButton;
    String foodId;
    Food currentFood;
    FirebaseDatabase database;
    DatabaseReference foods;
    Button btnGotoCart;

    RadioGroup radioFoodSize;
    RadioButton radioFoodButton;
    String price = "Select item size";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_details);

        // Getting reference of database associated with email lit2019007@iiitl.ac.in
        database = FirebaseDatabase.getInstance("https://fir-app-36b01-default-rtdb.asia-southeast1.firebasedatabase.app");
        foods = database.getReference("Foods");

        elegantNumberButton = findViewById(R.id.number_button);
        btnCart = findViewById(R.id.btnCart);
        btnGotoCart = findViewById(R.id.btnGotoCart);

        radioFoodSize = findViewById(R.id.food_size);

        // Checking what the user has selected.
        // Since our database has only one field for price
        // For small we will keep same price
        // For medium the price will be 1.5 times the price of small
        // And for large the price will be 2 times the price of small.



        radioFoodSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                radioFoodButton = findViewById(checkedId);

                if(radioFoodButton.isChecked())
                {
                    if (radioFoodButton.getText().equals("Small"))
                        price = currentFood.getPrice();
                    else if (radioFoodButton.getText().equals("Medium"))
                        price = String.valueOf(Double.parseDouble(currentFood.getPrice()) * 1.5);
                    else if (radioFoodButton.getText().equals("Large"))
                        price = String.valueOf(Double.parseDouble(currentFood.getPrice()) * 2);
                }

                else
                {
                    Toast.makeText(FoodDetails.this, "Please select the size of the food.", Toast.LENGTH_SHORT).show();
                }
            }
        });



        // This intent will open cart.
        btnGotoCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodDetails.this, Cart.class);
                startActivity(intent);
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(price.equals("Select item size"))
                {
                    price = String.valueOf(Double.parseDouble(currentFood.getPrice()));
                }
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        elegantNumberButton.getNumber(),
                        price,
                        currentFood.getDiscount()
                ));

                Toast.makeText(FoodDetails.this, "Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });

        food_description = findViewById(R.id.food_description);
        food_image = findViewById(R.id.img_food);
        food_name = findViewById(R.id.food_name);
        food_price = findViewById(R.id.food_price);

        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        if(getIntent() != null) {
            foodId = getIntent().getStringExtra("FoodId");
        }

        if(!foodId.isEmpty()) {
            getDetailFood(foodId);
        }
    }

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);
                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(food_image);
                collapsingToolbarLayout.setTitle(currentFood.getName());

                if(price.charAt(0)== 'S')
                    food_price.setText(price);
                else
                    food_price.setText('\u20B9' +  price);
                food_name.setText(currentFood.getName());
                food_description.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}