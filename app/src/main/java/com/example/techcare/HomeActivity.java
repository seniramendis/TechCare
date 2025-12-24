package com.example.techcare;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide; // Import Glide
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Setup Header
        HeaderUtils.setupHeader(this);

        // 2. Setup Search Bar
        setupSearchBar();

        // 3. Setup Ad Slider (With API Images)
        setupAdSlider();

        // 4. Setup Grid Click Listeners
        setupGrid();

        // 5. Setup Bottom Navigation
        setupBottomNav();
    }

    private void setupAdSlider() {
        viewPager = findViewById(R.id.pager_promo);

        List<AdItem> ads = new ArrayList<>();

        // Add items with Image URLs
        ads.add(new AdItem(
                "30% OFF First Repair!",
                "Use code: TECHNEW30",
                "https://images.unsplash.com/photo-1581092921461-eab62e97a780?q=80&w=1000&auto=format&fit=crop",
                "Claim"
        ));

        ads.add(new AdItem(
                "Same Day Delivery",
                "Fast repairs for mobiles",
                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?q=80&w=1000&auto=format&fit=crop",
                "View"
        ));

        ads.add(new AdItem(
                "Free Diagnostics",
                "Visit us for a free checkup",
                "https://images.unsplash.com/photo-1597424214711-41315e06259c?q=80&w=1000&auto=format&fit=crop",
                "View"
        ));

        viewPager.setAdapter(new PromoAdapter(ads));

        // Slider Transitions
        viewPager.setOffscreenPageLimit(3);
        viewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        viewPager.setPageTransformer(transformer);

        // Auto Scroll Logic
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 4000);
            }
        });
    }

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPager != null && viewPager.getAdapter() != null) {
                int currentItem = viewPager.getCurrentItem();
                int totalItems = viewPager.getAdapter().getItemCount();
                int nextItem = (currentItem + 1) % totalItems;
                viewPager.setCurrentItem(nextItem);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 4000);
    }

    // --- Data Model ---
    static class AdItem {
        String title, desc, imageUrl, buttonText;

        AdItem(String title, String desc, String imageUrl, String buttonText) {
            this.title = title;
            this.desc = desc;
            this.imageUrl = imageUrl;
            this.buttonText = buttonText;
        }
    }

    // --- Adapter ---
    class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.PromoViewHolder> {
        private final List<AdItem> items;

        PromoAdapter(List<AdItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
            return new PromoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
            AdItem item = items.get(position);
            holder.textTitle.setText(item.title);
            holder.textDesc.setText(item.desc);
            holder.btnAction.setText(item.buttonText);

            // Load Image with Glide
            Glide.with(holder.itemView.getContext())
                    .load(item.imageUrl)
                    .centerCrop()
                    .into(holder.imgBg);

            holder.btnAction.setOnClickListener(v -> {
                if ("Claim".equals(item.buttonText)) {
                    Toast.makeText(getApplicationContext(), "Promo Code Copied!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Opening details for: " + item.title, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class PromoViewHolder extends RecyclerView.ViewHolder {
            TextView textTitle, textDesc;
            Button btnAction;
            ImageView imgBg;

            PromoViewHolder(@NonNull View itemView) {
                super(itemView);
                textTitle = itemView.findViewById(R.id.tv_banner_title);
                textDesc = itemView.findViewById(R.id.tv_banner_desc);
                btnAction = itemView.findViewById(R.id.btn_banner_action);
                imgBg = itemView.findViewById(R.id.img_banner_bg);
            }
        }
    }

    // --- Existing Helper Methods ---

    private void setupSearchBar() {
        SearchView searchView = findViewById(R.id.search_view);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Toast.makeText(getApplicationContext(), "Searching: " + query, Toast.LENGTH_SHORT).show();
                    return true;
                }
                @Override
                public boolean onQueryTextChange(String newText) { return true; }
            });
        }
    }

    private void setupGrid() {
        CardView cardPhone = findViewById(R.id.card_smartphone);
        CardView cardLaptop = findViewById(R.id.card_laptop);
        CardView cardAppliance = findViewById(R.id.card_appliance);
        CardView cardOther = findViewById(R.id.card_other);

        if (cardPhone != null) cardPhone.setOnClickListener(v -> openBooking("Smartphone"));
        if (cardLaptop != null) cardLaptop.setOnClickListener(v -> openBooking("Laptop/PC"));
        if (cardAppliance != null) cardAppliance.setOnClickListener(v -> openBooking("Home Appliance"));
        if (cardOther != null) cardOther.setOnClickListener(v -> openBooking("Other Device"));
    }

    private void openBooking(String deviceType) {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("DEVICE_TYPE", deviceType);
        startActivity(intent);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_bookings) {
                Toast.makeText(this, "My Bookings", Toast.LENGTH_SHORT).show();
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }
}