package com.anchal.canteentest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anchal.canteentest.Interface.ItemClickListener;
import com.anchal.canteentest.Model.Food;
import com.anchal.canteentest.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FoodList extends AppCompatActivity {

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodlist;
    String CategoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        // Getting database reference.
        database = FirebaseDatabase.getInstance("https://fir-app-36b01-default-rtdb.asia-southeast1.firebasedatabase.app");
        foodlist = database.getReference("Foods");


        recycler_menu = (RecyclerView) findViewById(R.id.recycler_food);
        recycler_menu.setHasFixedSize(true);


        layoutManager = new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(layoutManager);


        if(getIntent() != null) {
            CategoryId = getIntent().getStringExtra("CategoryId");
        }

        if(!CategoryId.isEmpty() && CategoryId != null) {
            loadfoodlist(CategoryId);
        }
    }

    private void loadfoodlist(String categoryId)
    {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodlist.orderByChild("menuId").equalTo(categoryId))
        {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position)
            {
                viewHolder.foodName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.foodImage);
                final Food local = model;

                viewHolder.setItemClickListener(new ItemClickListener()
                {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick)
                    {
                        Intent foodDetails = new Intent(FoodList.this, FoodDetails.class);
                        foodDetails.putExtra("FoodId", adapter.getRef(position).getKey());
                        startActivity(foodDetails);
                    }
                });
            }

        };
        recycler_menu.setAdapter(adapter);
    }
}