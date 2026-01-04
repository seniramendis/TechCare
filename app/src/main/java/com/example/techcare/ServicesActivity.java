package com.example.techcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class ServicesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    // UI Components for the Review Section
    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmit;

    // Adapter for the service list
    private ServiceAdapter serviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        dbHelper = new DatabaseHelper(this);

        HeaderUtils.setupHeader(this);
        setupBottomNav();

        // 1. Initialize the Service List with Search Capability
        setupServicesList();

        // 2. Setup Search Listener
        setupSearch();

        // 3. Setup other static interactions
        setupStaticClickListeners();
        setupFeedbackSection();
    }

    private void setupServicesList() {
        RecyclerView recyclerServices = findViewById(R.id.recycler_services);
        // Use GridLayoutManager with 2 columns to look like the original design
        recyclerServices.setLayoutManager(new GridLayoutManager(this, 2));

        List<ServiceItem> items = new ArrayList<>();
        // Add service items. You can add more here easily.
        items.add(new ServiceItem("Smartphone", "Screen, Battery & Cam", R.drawable.ic_smartphone, "https://images.unsplash.com/photo-1512054502232-10a0a035d672?w=500&q=80"));
        items.add(new ServiceItem("Laptop/PC", "OS, Keyboard & Logic", R.drawable.ic_laptop, "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=500&q=80"));
        items.add(new ServiceItem("Home Appliance", "AC, Fridge & Wash", R.drawable.ic_home_repair_service, "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=500&q=80"));
        items.add(new ServiceItem("Other Device", "TV, Audio & IoT", R.drawable.ic_more_horiz, "https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=500&q=80"));

        serviceAdapter = new ServiceAdapter(items);
        recyclerServices.setAdapter(serviceAdapter);
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.search_view);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Filter immediately on submit
                    serviceAdapter.getFilter().filter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Filter in real-time as user types
                    serviceAdapter.getFilter().filter(newText);
                    return true;
                }
            });
        }
    }

    private void setupFeedbackSection() {
        ratingBar = findViewById(R.id.rating_bar_inline);
        etComment = findViewById(R.id.et_review_inline);
        btnSubmit = findViewById(R.id.btn_submit_review);

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> submitReview());
        }
    }

    private void submitReview() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", null);

        if (email == null) {
            Toast.makeText(this, "Please login to write a review", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        float rating = ratingBar.getRating();
        String comment = etComment.getText().toString().trim();

        if (rating > 0 && !TextUtils.isEmpty(comment)) {
            boolean success = dbHelper.addReview(email, (int) rating, comment);
            if (success) {
                Toast.makeText(ServicesActivity.this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show();
                etComment.setText("");
                ratingBar.setRating(0);
            } else {
                Toast.makeText(ServicesActivity.this, "Error submitting review", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ServicesActivity.this, "Please provide a rating and comment", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupStaticClickListeners() {
        // Listener for the Support Card (located below the list)
        View cardSupport = findViewById(R.id.card_support);
        if (cardSupport != null) {
            cardSupport.setOnClickListener(v -> {
                startActivity(new Intent(ServicesActivity.this, SupportActivity.class));
            });
        }
    }

    private void openBooking(String deviceType) {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("DEVICE_TYPE", deviceType);
        startActivity(intent);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_services);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_services) return true;

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_bookings) {
                startActivity(new Intent(this, MyBookingsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    // --- Inner Classes for Data Model & Adapter ---

    static class ServiceItem {
        String name, desc, bgUrl;
        int iconRes;
        ServiceItem(String name, String desc, int iconRes, String bgUrl) {
            this.name = name; this.desc = desc; this.iconRes = iconRes; this.bgUrl = bgUrl;
        }
    }

    class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> implements Filterable {
        private List<ServiceItem> originalList;
        private List<ServiceItem> filteredList;

        ServiceAdapter(List<ServiceItem> list) {
            this.originalList = list;
            this.filteredList = new ArrayList<>(list);
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ServiceItem item = filteredList.get(position);
            holder.tvTitle.setText(item.name);
            holder.tvDesc.setText(item.desc);
            holder.imgIcon.setImageResource(item.iconRes);

            // Load background image
            Glide.with(holder.itemView.getContext())
                    .load(item.bgUrl)
                    .centerCrop()
                    .into(holder.imgBg);

            // Handle click
            holder.itemView.setOnClickListener(v -> openBooking(item.name));
        }

        @Override public int getItemCount() { return filteredList.size(); }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    String query = constraint.toString().toLowerCase().trim();
                    FilterResults results = new FilterResults();

                    if (query.isEmpty()) {
                        results.values = originalList;
                    } else {
                        List<ServiceItem> temp = new ArrayList<>();
                        for (ServiceItem item : originalList) {
                            // Check both title and description for the search term
                            if (item.name.toLowerCase().contains(query) || item.desc.toLowerCase().contains(query)) {
                                temp.add(item);
                            }
                        }
                        results.values = temp;
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredList = (List<ServiceItem>) results.values;
                    notifyDataSetChanged();
                }
            };
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDesc;
            ImageView imgBg, imgIcon;
            ViewHolder(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_service_title);
                tvDesc = v.findViewById(R.id.tv_service_desc);
                imgBg = v.findViewById(R.id.img_service_bg);
                imgIcon = v.findViewById(R.id.img_service_icon);
            }
        }
    }
}