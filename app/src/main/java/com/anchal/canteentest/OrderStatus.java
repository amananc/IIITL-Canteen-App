package com.anchal.canteentest;

import static com.anchal.canteentest.Common.Common.convertCodeToStatus;
import static com.anchal.canteentest.Common.Common.returnStatusCode;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anchal.canteentest.Common.Common;
import com.anchal.canteentest.Interface.ItemClickListener;
import com.anchal.canteentest.Model.Request;
import com.anchal.canteentest.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class OrderStatus extends AppCompatActivity {

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
    FirebaseDatabase database;
    DatabaseReference requests;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);


        // Getting reference of database .
        // This database is associated with firebase account of email account LIT2019007@iiitl.ac.in
        database = FirebaseDatabase.getInstance("https://fir-app-36b01-default-rtdb.asia-southeast1.firebasedatabase.app");
        requests = database.getReference("Requests");

        recyclerView = findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if (getIntent().getExtras() == null)
            loadOrders(Common.currentUser.getPhoneNo());
        else
            loadOrders(Objects.requireNonNull(getIntent()).getStringExtra("userPhone"));
    }

    private void loadOrders(String phone) {
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests.orderByChild("phone").equalTo(phone)
        )
        {
            @Override
            protected void populateViewHolder(OrderViewHolder orderViewHolder, Request request, int i) {

                orderViewHolder.txtOrderId.setText(adapter.getRef(i).getKey());
                orderViewHolder.txtOrderStatus.setText(convertCodeToStatus(request.getStatus()));


                // Setting the text color of order status
                if(returnStatusCode(request.getStatus()) == 0)
                    orderViewHolder.txtOrderStatus.setTextColor(getResources().getColor(R.color.white));

                else if(returnStatusCode(request.getStatus()) == 1)
                    orderViewHolder.txtOrderStatus.setTextColor(getResources().getColor(R.color.blue_grotto));

                else
                    orderViewHolder.txtOrderStatus.setTextColor(getResources().getColor(R.color.green));


                orderViewHolder.txtOrderAddress.setText(request.getAddress());
                orderViewHolder.txtOrderPhone.setText(request.getPhone());

                double d = Double.parseDouble(request.getDistance());
                double price = 0;

                // Checking price for delivery if the distance of delivery is more than 1 km then we will charge 5 rs per km
                // Else it will be free.
                if(d > 1.0) {
                    price = (d) * 5.0;
                }

                Locale locale = new Locale("en", "IN");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                if(price > 0) {
                    orderViewHolder.txtPrice.setText(request.getTotal() + " + " + fmt.format(price));
                }
                else
                {
                    orderViewHolder.txtPrice.setText(request.getTotal());
                }



                orderViewHolder.setItemClickListener(new ItemClickListener()
                {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick)
                    {
                        //Toast.makeText(OrderStatus.this, "", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        };
        recyclerView.setAdapter(adapter);
    }

}