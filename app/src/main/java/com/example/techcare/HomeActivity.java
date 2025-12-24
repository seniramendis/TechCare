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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
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

        // 3. Setup Ad Slider
        setupAdSlider();

        // 4. Setup Grid Click Listeners
        setupGrid();

        // 5. Setup Popular Services (NEW CAROUSEL)
        setupPopularServices();

        // 6. Setup Bottom Navigation
        setupBottomNav();
    }

    private void setupPopularServices() {
        RecyclerView recyclerPopular = findViewById(R.id.recycler_popular);
        recyclerPopular.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<PopularService> services = new ArrayList<>();

        // 1. Screen Repair (Hands fixing electronics)
        services.add(new PopularService(
                "Screen Repair",
                "https://images.unsplash.com/photo-1581092921461-39b9d08a9b2a?auto=format&fit=crop&w=600&q=80"
        ));

        // 2. Battery Fix (Detailed Circuit Board)
        services.add(new PopularService(
                "Battery Fix",
                "https://images.unsplash.com/photo-1550041473-d296a1a8ec02?auto=format&fit=crop&w=600&q=80"
        ));

        // 3. Virus Cleanup (Hacker / Matrix code style)
        services.add(new PopularService(
                "Virus Cleanup",
                "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&w=600&q=80"
        ));

        // 4. Data Recovery (Server lights)
        services.add(new PopularService(
                "Data Recovery",
                "https://images.unsplash.com/photo-1558494949-ef526b0042a0?auto=format&fit=crop&w=600&q=80"
        ));

        // 5. OS Install (Laptop Setup)
        services.add(new PopularService(
                "OS Install",
                "https://images.unsplash.com/photo-1593642702749-b7d2a804fbcf?auto=format&fit=crop&w=600&q=80"
        ));

        recyclerPopular.setAdapter(new PopularServiceAdapter(services));
    }

    private void setupAdSlider() {
        viewPager = findViewById(R.id.pager_promo);

        List<AdItem> ads = new ArrayList<>();

        // Ad 1: Claim Button
        ads.add(new AdItem(
                "30% OFF First Repair!",
                "Use code: TECHNEW30",
                "https://images.unsplash.com/photo-1588508065123-287b28e013da?auto=format&fit=crop&w=1000&q=80",
                "Claim"
        ));

        // Ad 2: No Button (null)
        ads.add(new AdItem(
                "Same Day Delivery",
                "We come to you!",
                "https://images.unsplash.com/photo-1616401784845-180882ba9ba8?auto=format&fit=crop&w=1000&q=80",
                null
        ));

        // Ad 3: No Button (null)
        ads.add(new AdItem(
                "Free Diagnostics",
                "Visit us for a free checkup",
                "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?auto=format&fit=crop&w=1000&q=80",
                null
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

    // --- Helper Methods ---
    private void setupSearchBar() {
        SearchView searchView = findViewById(R.id.search_view);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Toast.makeText(HomeActivity.this, "Searching: " + query, Toast.LENGTH_SHORT).show();
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

    // ===========================
    //       ADAPTER CLASSES
    // ===========================

    // 1. Promo Banner Adapter
    static class AdItem {
        String title, desc, imageUrl, buttonText;
        AdItem(String title, String desc, String imageUrl, String buttonText) {
            this.title = title;
            this.desc = desc;
            this.imageUrl = imageUrl;
            this.buttonText = buttonText;
        }
    }

    class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.PromoViewHolder> {
        private final List<AdItem> items;
        PromoAdapter(List<AdItem> items) { this.items = items; }

        @NonNull @Override
        public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
            return new PromoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
            AdItem item = items.get(position);
            holder.textTitle.setText(item.title);
            holder.textDesc.setText(item.desc);

            // Hide/Show Button Logic
            if (item.buttonText == null || item.buttonText.isEmpty()) {
                holder.btnAction.setVisibility(View.GONE);
            } else {
                holder.btnAction.setVisibility(View.VISIBLE);
                holder.btnAction.setText(item.buttonText);
                holder.btnAction.setOnClickListener(v -> {
                    if ("Claim".equals(item.buttonText)) {
                        Toast.makeText(HomeActivity.this, "Promo Code Copied!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HomeActivity.this, "Details: " + item.title, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            Glide.with(holder.itemView.getContext())
                    .load(item.imageUrl)
                    .centerCrop()
                    .into(holder.imgBg);
        }

        @Override public int getItemCount() { return items.size(); }

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

    // 2. Popular Services Adapter
    static class PopularService {
        String name;
        String imageUrl;
        PopularService(String name, String imageUrl) {
            this.name = name;
            this.imageUrl = imageUrl;
        }
    }

    class PopularServiceAdapter extends RecyclerView.Adapter<PopularServiceAdapter.ServiceViewHolder> {
        private final List<PopularService> items;
        PopularServiceAdapter(List<PopularService> items) { this.items = items; }

        @NonNull @Override
        public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_popular_service, parent, false);
            return new ServiceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
            PopularService item = items.get(position);
            holder.tvName.setText(item.name);

            // Load Background Image
            Glide.with(holder.itemView.getContext())
                    .load(item.imageUrl)
                    .centerCrop()
                    .into(holder.imgBg);

            holder.itemView.setOnClickListener(v -> openBooking(item.name));
        }

        @Override public int getItemCount() { return items.size(); }

        class ServiceViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            ImageView imgBg;
            ServiceViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_service_name);
                imgBg = itemView.findViewById(R.id.img_service_bg);
            }
        }
    }
}