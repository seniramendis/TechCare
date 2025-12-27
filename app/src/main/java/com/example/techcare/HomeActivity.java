package com.example.techcare;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ViewPager2 trustViewPager;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private final Handler trustHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        try {
            HeaderUtils.setupHeader(this);
            setupSearchBar();
            setupActiveRepairTracker(); // Initialize the tracker
            setupAdSlider();
            setupGrid();
            setupPopularServices();
            setupTrustSection();
            setupBottomNav();
        } catch (Exception e) {
            Log.e("HomeActivity", "Error in setup", e);
            Toast.makeText(this, "Error loading home screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Logic for the Active Repair Status Tracker
    private void setupActiveRepairTracker() {
        CardView repairCard = findViewById(R.id.card_active_repair);
        TextView deviceName = findViewById(R.id.tv_device_name);
        TextView status = findViewById(R.id.tv_repair_status);
        ProgressBar progressBar = findViewById(R.id.progress_repair);

        if (repairCard != null) {
            // In a real app, you would fetch this status from your database.
            // For now, we simulate an active repair.
            boolean hasActiveRepair = true;

            if (hasActiveRepair) {
                repairCard.setVisibility(View.VISIBLE);
                deviceName.setText("iPhone 13 Pro Max");

                // Set text to Green
                status.setText("Diagnosing (70%)");
                status.setTextColor(Color.parseColor("#4CAF50"));

                // Set Progress to 70
                progressBar.setProgress(70);

                // Set Filled part to Green (#4CAF50)
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                // Set Unfilled background part to Grey (#E0E0E0)
                progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));

                repairCard.setOnClickListener(v -> {
                    Toast.makeText(this, "Opening Repair Details...", Toast.LENGTH_SHORT).show();
                    // Intent intent = new Intent(this, RepairDetailsActivity.class);
                    // startActivity(intent);
                });
            } else {
                repairCard.setVisibility(View.GONE);
            }
        }
    }

    private void setupTrustSection() {
        trustViewPager = findViewById(R.id.pager_trust);
        if (trustViewPager != null) {
            List<TrustItem> trustItems = new ArrayList<>();

            // Expert Technicians
            trustItems.add(new TrustItem("Expert Technicians", "Certified pros for quality repairs.",
                    R.drawable.ic_default_user,
                    "https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?auto=format&fit=crop&w=800&q=80"));

            // Express Service
            trustItems.add(new TrustItem("Express Service", "Same-day repair for most devices.",
                    R.drawable.ic_nav_bookings,
                    "https://images.unsplash.com/photo-1524592094714-0f0654e20314?auto=format&fit=crop&w=800&q=80"));

            // Service Warranty - Using the 'Signing/Contract' image
            trustItems.add(new TrustItem("Service Warranty", "Enjoy 30 days of peace of mind.",
                    R.drawable.ic_home_repair_service,
                    "https://images.unsplash.com/photo-1450101499163-c8848c66ca85?auto=format&fit=crop&w=800&q=80"));

            // Genuine Components
            trustItems.add(new TrustItem("Genuine Components", "100% original manufacturer parts.",
                    R.drawable.ic_laptop,
                    "https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?auto=format&fit=crop&w=800&q=80"));

            trustViewPager.setAdapter(new TrustAdapter(trustItems));
            trustViewPager.setPageTransformer((page, position) -> page.setAlpha(1 - Math.abs(position)));
        }
    }

    private final Runnable trustRunnable = new Runnable() {
        @Override
        public void run() {
            if (trustViewPager != null && trustViewPager.getAdapter() != null && trustViewPager.getAdapter().getItemCount() > 0) {
                int nextItem = (trustViewPager.getCurrentItem() + 1) % trustViewPager.getAdapter().getItemCount();
                trustViewPager.setCurrentItem(nextItem, true);
                trustHandler.postDelayed(this, 2000);
            }
        }
    };

    private void setupAdSlider() {
        viewPager = findViewById(R.id.pager_promo);
        if (viewPager == null) return;

        List<AdItem> ads = new ArrayList<>();
        ads.add(new AdItem("30% OFF First Repair!", "Use code: TECHNEW30", "https://images.unsplash.com/photo-1588508065123-287b28e013da?auto=format&fit=crop&w=1000&q=80", "Claim"));
        ads.add(new AdItem("Same Day Delivery", "We come to you!", "https://images.unsplash.com/photo-1616401784845-180882ba9ba8?auto=format&fit=crop&w=1000&q=80", null));
        ads.add(new AdItem("Free Diagnostics", "Visit us for a free checkup", "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?auto=format&fit=crop&w=1000&q=80", null));

        viewPager.setAdapter(new PromoAdapter(ads));
        viewPager.setOffscreenPageLimit(3);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        viewPager.setPageTransformer(transformer);

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
            if (viewPager != null && viewPager.getAdapter() != null && viewPager.getAdapter().getItemCount() > 0) {
                int nextItem = (viewPager.getCurrentItem() + 1) % viewPager.getAdapter().getItemCount();
                viewPager.setCurrentItem(nextItem);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 4000);
        trustHandler.postDelayed(trustRunnable, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
        trustHandler.removeCallbacks(trustRunnable);
    }

    private void setupSearchBar() {
        SearchView searchView = findViewById(R.id.search_view);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String query) {
                    Toast.makeText(HomeActivity.this, "Searching: " + query, Toast.LENGTH_SHORT).show();
                    return true;
                }
                @Override public boolean onQueryTextChange(String newText) { return true; }
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

    private void setupPopularServices() {
        RecyclerView recyclerPopular = findViewById(R.id.recycler_popular);
        if (recyclerPopular != null) {
            recyclerPopular.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            List<PopularService> services = new ArrayList<>();
            services.add(new PopularService("Screen Repair", "https://loremflickr.com/600/400/smartphone,broken?lock=1"));
            services.add(new PopularService("Battery Fix", "https://loremflickr.com/600/400/phone,battery?lock=2"));
            services.add(new PopularService("Virus Cleanup", "https://loremflickr.com/600/400/hacker,code?lock=3"));
            services.add(new PopularService("Data Recovery", "https://loremflickr.com/600/400/server,data?lock=4"));
            services.add(new PopularService("OS Install", "https://loremflickr.com/600/400/software,computer?lock=5"));
            recyclerPopular.setAdapter(new PopularServiceAdapter(services));
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) return true;
                if (id == R.id.nav_bookings) {
                    startActivity(new Intent(this, MyBookingsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }
    }

    // --- Inner Classes and Adapters ---
    static class TrustItem {
        String title, desc, bgUrl;
        int iconRes;
        TrustItem(String title, String desc, int iconRes, String bgUrl) {
            this.title = title; this.desc = desc; this.iconRes = iconRes; this.bgUrl = bgUrl;
        }
    }

    class TrustAdapter extends RecyclerView.Adapter<TrustAdapter.TrustViewHolder> {
        private final List<TrustItem> items;
        TrustAdapter(List<TrustItem> items) { this.items = items; }
        @NonNull @Override public TrustViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trust, parent, false);
            return new TrustViewHolder(view);
        }
        @Override public void onBindViewHolder(@NonNull TrustViewHolder holder, int position) {
            TrustItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvDesc.setText(item.desc);
            holder.imgIcon.setImageResource(item.iconRes);
            Glide.with(holder.itemView.getContext()).load(item.bgUrl).centerCrop().into(holder.imgBg);
        }
        @Override public int getItemCount() { return items.size(); }
        class TrustViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDesc; ImageView imgIcon, imgBg;
            TrustViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_trust_title);
                tvDesc = itemView.findViewById(R.id.tv_trust_desc);
                imgIcon = itemView.findViewById(R.id.img_trust_icon);
                imgBg = itemView.findViewById(R.id.img_trust_bg);
            }
        }
    }

    static class AdItem {
        String title, desc, imageUrl, buttonText;
        AdItem(String title, String desc, String imageUrl, String buttonText) { this.title = title; this.desc = desc; this.imageUrl = imageUrl; this.buttonText = buttonText; }
    }

    class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.PromoViewHolder> {
        private final List<AdItem> items;
        PromoAdapter(List<AdItem> items) { this.items = items; }
        @NonNull @Override public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
            return new PromoViewHolder(view);
        }
        @Override public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
            AdItem item = items.get(position);
            holder.textTitle.setText(item.title);
            holder.textDesc.setText(item.desc);
            if (item.buttonText == null || item.buttonText.isEmpty()) holder.btnAction.setVisibility(View.GONE);
            else {
                holder.btnAction.setVisibility(View.VISIBLE);
                holder.btnAction.setText(item.buttonText);
                holder.btnAction.setOnClickListener(v -> Toast.makeText(HomeActivity.this, "Details: " + item.title, Toast.LENGTH_SHORT).show());
            }
            Glide.with(holder.itemView.getContext()).load(item.imageUrl).centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.imgBg);
        }
        @Override public int getItemCount() { return items.size(); }
        class PromoViewHolder extends RecyclerView.ViewHolder {
            TextView textTitle, textDesc; Button btnAction; ImageView imgBg;
            PromoViewHolder(@NonNull View itemView) {
                super(itemView);
                textTitle = itemView.findViewById(R.id.tv_banner_title);
                textDesc = itemView.findViewById(R.id.tv_banner_desc);
                btnAction = itemView.findViewById(R.id.btn_banner_action);
                imgBg = itemView.findViewById(R.id.img_banner_bg);
            }
        }
    }

    public static class PopularService {
        String name; String imageUrl;
        public PopularService(String name, String imageUrl) { this.name = name; this.imageUrl = imageUrl; }
    }

    class PopularServiceAdapter extends RecyclerView.Adapter<PopularServiceAdapter.ServiceViewHolder> {
        private final List<PopularService> items;
        PopularServiceAdapter(List<PopularService> items) { this.items = items; }
        @NonNull @Override public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_popular_service, parent, false);
            return new ServiceViewHolder(view);
        }
        @Override public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
            PopularService item = items.get(position);
            holder.tvName.setText(item.name);
            Glide.with(holder.itemView.getContext()).load(item.imageUrl).centerCrop().into(holder.imgBg);
            holder.itemView.setOnClickListener(v -> openBooking(item.name));
        }
        @Override public int getItemCount() { return items.size(); }
        class ServiceViewHolder extends RecyclerView.ViewHolder {
            TextView tvName; ImageView imgBg;
            ServiceViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_service_name);
                imgBg = itemView.findViewById(R.id.img_service_bg);
            }
        }
    }
}