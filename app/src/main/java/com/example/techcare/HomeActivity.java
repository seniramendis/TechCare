package com.example.techcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
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
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ViewPager2 trustViewPager;
    private ViewPager2 repairsViewPager;

    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private final Handler trustHandler = new Handler(Looper.getMainLooper());
    private final Handler trackerHandler = new Handler(Looper.getMainLooper());

    private DatabaseHelper dbHelper;
    private SimpleCursorAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);

        try {
            HeaderUtils.setupHeader(this);
            setupSearchBar();
            setupActiveRepairsCarousel();
            setupAdSlider();
            setupGrid();
            setupPopularServices();
            setupTestimonials();
            setupTrustSection();
            setupTechTips();
            setupBottomNav();
        } catch (Exception e) {
            Log.e("HomeActivity", "Error in setup", e);
            Toast.makeText(this, "Error loading home screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupSearchBar() {
        SearchView searchView = findViewById(R.id.search_view);
        if (searchView == null) return;

        final String[] from = new String[] { "suggestion" };
        final int[] to = new int[] { android.R.id.text1 };

        searchAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        searchView.setSuggestionsAdapter(searchAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performGlobalSearch(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                populateSearchSuggestions(newText);
                return true;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) searchAdapter.getItem(position);
                int index = cursor.getColumnIndex("suggestion");
                if (index != -1) {
                    String selection = cursor.getString(index);
                    searchView.setQuery(selection, true);
                }
                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position) { return false; }
        });
    }

    private void populateSearchSuggestions(String query) {
        final MatrixCursor c = new MatrixCursor(new String[]{ BaseColumns._ID, "suggestion" });

        String[] allSuggestions = {
                "Profile & Settings",
                "My Bookings History",
                "Customer Support",
                "TechCare Reviews",
                "Book: Smartphone Repair",
                "Book: Laptop Service",
                "Book: Home Appliance",
                "Book: Other Device",
                "Service List & Prices",
                "Contact Us",
                "Repair Status"
        };

        query = query.toLowerCase();
        int id = 0;
        for (String suggestion : allSuggestions) {
            if (suggestion.toLowerCase().contains(query)) {
                c.addRow(new Object[]{id++, suggestion});
            }
        }

        if (query.matches("\\d+")) {
            c.addRow(new Object[]{id++, "Track Booking #" + query});
        }

        searchAdapter.changeCursor(c);
    }

    private void performGlobalSearch(String query) {
        String q = query.toLowerCase().trim();
        if (q.isEmpty()) return;

        if (q.contains("track booking #")) {
            startActivity(new Intent(HomeActivity.this, MyBookingsActivity.class));
            return;
        }

        if (q.contains("profile") || q.contains("account") || q.contains("settings")) {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            return;
        }
        if (q.contains("booking") || q.contains("order") || q.contains("history") || q.contains("status")) {
            startActivity(new Intent(HomeActivity.this, MyBookingsActivity.class));
            return;
        }
        if (q.contains("support") || q.contains("help") || q.contains("contact")) {
            startActivity(new Intent(HomeActivity.this, SupportActivity.class));
            return;
        }
        if (q.contains("review") || q.contains("testimonial")) {
            startActivity(new Intent(HomeActivity.this, TestimonialsActivity.class));
            return;
        }
        if (q.contains("service") || q.contains("price")) {
            startActivity(new Intent(HomeActivity.this, ServicesActivity.class));
            return;
        }

        if (q.contains("laptop") || q.contains("computer") || q.contains("pc")) {
            openBooking("Laptop/PC");
            return;
        }
        if (q.contains("smartphone") || q.contains("mobile") || q.contains("phone") || q.contains("screen")) {
            openBooking("Smartphone");
            return;
        }
        if (q.contains("appliance") || q.contains("washing") || q.contains("tv")) {
            openBooking("Home Appliance");
            return;
        }
        if (q.contains("other") || q.contains("console")) {
            openBooking("Other Device");
            return;
        }

        checkDatabaseForBooking(query);
    }

    private void checkDatabaseForBooking(String query) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", null);

        if (email != null) {
            Cursor cursor = dbHelper.getUserBookings(email);
            boolean found = false;
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        int devIdx = cursor.getColumnIndex("device_type");
                        int idIdx = cursor.getColumnIndex("booking_id");

                        if (devIdx != -1 && idIdx != -1) {
                            String dev = cursor.getString(devIdx);
                            String id = String.valueOf(cursor.getInt(idIdx));

                            if (dev.toLowerCase().contains(query.toLowerCase()) || id.equals(query)) {
                                found = true;
                                break;
                            }
                        }
                    } while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null) cursor.close();
            }

            if (found) {
                Toast.makeText(this, "Opening related booking...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this, MyBookingsActivity.class));
            } else {
                Toast.makeText(this, "No specific result found. Try 'Laptop' or 'Profile'.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Please login to search your history.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTestimonials() {
        RecyclerView recyclerReviews = findViewById(R.id.recycler_testimonials);
        TextView btnViewAll = findViewById(R.id.tv_reviews_view_all);

        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, TestimonialsActivity.class));
            });
        }

        if (recyclerReviews != null) {
            recyclerReviews.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            List<Review> reviews = dbHelper.getAllReviews();
            if (reviews.size() > 5) {
                reviews = reviews.subList(0, 5);
            }
            recyclerReviews.setAdapter(new TestimonialAdapter(reviews));
        }
    }

    class TestimonialAdapter extends RecyclerView.Adapter<TestimonialAdapter.ReviewViewHolder> {
        private final List<Review> items;
        TestimonialAdapter(List<Review> items) { this.items = items; }

        @NonNull @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_testimonial, parent, false);
            return new ReviewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            Review item = items.get(position);
            holder.tvReview.setText("\"" + item.review + "\"");
            holder.tvAuthor.setText(item.author);

            StringBuilder stars = new StringBuilder();
            for(int i = 0; i < item.rating; i++) stars.append("⭐");
            holder.tvStars.setText(stars.toString());

            if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
                holder.imgAvatar.clearColorFilter();
                holder.imgAvatar.setPadding(0,0,0,0);
                Glide.with(holder.itemView.getContext())
                        .load(item.imageUrl)
                        .circleCrop()
                        .into(holder.imgAvatar);
            } else {
                holder.imgAvatar.clearColorFilter();
                holder.imgAvatar.setPadding(0,0,0,0);
                holder.imgAvatar.setImageResource(R.drawable.ic_default_user);
            }
        }

        @Override public int getItemCount() { return items.size(); }

        class ReviewViewHolder extends RecyclerView.ViewHolder {
            TextView tvReview, tvAuthor, tvStars;
            ImageView imgAvatar;

            ReviewViewHolder(@NonNull View itemView) {
                super(itemView);
                tvReview = itemView.findViewById(R.id.tv_review_text);
                tvAuthor = itemView.findViewById(R.id.tv_review_author);
                tvStars = itemView.findViewById(R.id.tv_star_rating);
                imgAvatar = itemView.findViewById(R.id.img_avatar);
            }
        }
    }

    private final Runnable trackerRunnable = new Runnable() {
        @Override
        public void run() {
            setupActiveRepairsCarousel();
            trackerHandler.postDelayed(this, 1000);
        }
    };

    private void setupActiveRepairsCarousel() {
        repairsViewPager = findViewById(R.id.pager_active_repairs);
        TextView viewAll = findViewById(R.id.tv_view_all);
        LinearLayout header = findViewById(R.id.layout_tracker_header);

        if (repairsViewPager == null) return;
        if (viewAll != null) {
            viewAll.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MyBookingsActivity.class)));
        }

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", null);

        if (email == null) {
            hideRepairsSection(header, repairsViewPager);
            return;
        }

        List<RepairItem> repairList = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = dbHelper.getUserBookings(email);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex("booking_id");
                    int deviceIndex = cursor.getColumnIndex("device_type");

                    if (idIndex != -1 && deviceIndex != -1) {
                        int id = cursor.getInt(idIndex);
                        String device = cursor.getString(deviceIndex);
                        long startTime = prefs.getLong("booking_start_" + id, 0);
                        if (startTime == 0) {
                            startTime = System.currentTimeMillis();
                            prefs.edit().putLong("booking_start_" + id, startTime).apply();
                        }

                        // --- SIMULATION LOGIC ---
                        long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
                        String status;
                        int progress;

                        if (elapsedSeconds < 10) {
                            status = "Received";
                            progress = 10;
                        }
                        else if (elapsedSeconds < 20) {
                            status = "Diagnosing";
                            progress = 35;
                        }
                        else if (elapsedSeconds < 30) {
                            status = "Repairing";
                            progress = 65;
                        }
                        else if (elapsedSeconds < 40) {
                            status = "Testing";
                            progress = 85;
                        }
                        else {
                            status = "Completed";
                            progress = 100;
                        }

                        repairList.add(new RepairItem(device, status, progress, elapsedSeconds, id));
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("HomeActivity", "Error loading repairs", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        if (repairList.isEmpty()) {
            hideRepairsSection(header, repairsViewPager);
        } else {
            showRepairsSection(header, repairsViewPager);

            for (RepairItem item : repairList) {
                dbHelper.updateBookingStatus(item.id, item.status);
            }

            // --- FIX START: Better update mechanism ---
            if (repairsViewPager.getAdapter() instanceof RepairAdapter) {
                RepairAdapter adapter = (RepairAdapter) repairsViewPager.getAdapter();
                adapter.updateList(repairList);
            } else {
                RepairAdapter adapter = new RepairAdapter(repairList);
                repairsViewPager.setAdapter(adapter);
                repairsViewPager.setOffscreenPageLimit(3);
                repairsViewPager.setClipToPadding(false);
                repairsViewPager.setClipChildren(false);
                repairsViewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
                CompositePageTransformer transformer = new CompositePageTransformer();
                transformer.addTransformer(new MarginPageTransformer(24));
                repairsViewPager.setPageTransformer(transformer);
            }
            // --- FIX END ---
        }
    }

    private void hideRepairsSection(View header, View pager) {
        if(header != null) header.setVisibility(View.GONE);
        if(pager != null) pager.setVisibility(View.GONE);
    }

    private void showRepairsSection(View header, View pager) {
        if(header != null) header.setVisibility(View.VISIBLE);
        if(pager != null) pager.setVisibility(View.VISIBLE);
    }

    static class RepairItem {
        String deviceName, status;
        int progress;
        long elapsedSeconds;
        int id;
        RepairItem(String deviceName, String status, int progress, long elapsedSeconds, int id) {
            this.deviceName = deviceName;
            this.status = status;
            this.progress = progress;
            this.elapsedSeconds = elapsedSeconds;
            this.id = id;
        }
    }

    class RepairAdapter extends RecyclerView.Adapter<RepairAdapter.RepairViewHolder> {
        List<RepairItem> list;
        RepairAdapter(List<RepairItem> list) { this.list = list; }

        // --- FIX: Use notifyItemRangeChanged to prevent jitter ---
        void updateList(List<RepairItem> newList) {
            if (list == null || list.size() != newList.size()) {
                // List structure changed, we must do a full refresh
                this.list = newList;
                notifyDataSetChanged();
            } else {
                // List structure is same, only data changed.
                // Using notifyItemRangeChanged prevents ViewPager from snapping/resetting.
                this.list = newList;
                notifyItemRangeChanged(0, list.size());
            }
        }

        @NonNull @Override
        public RepairViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_repair_card, parent, false);
            return new RepairViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull RepairViewHolder holder, int position) {
            RepairItem item = list.get(position);
            holder.deviceName.setText(item.deviceName);
            String displayStatus = item.status;
            if ("Completed".equals(item.status)) displayStatus = "Ready for Pickup";
            holder.status.setText(displayStatus + " (" + item.progress + "%)");
            holder.progressBar.setProgress(item.progress);
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            holder.progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));

            if (item.progress < 100) {
                holder.eta.setText("In Progress");
            } else {
                holder.eta.setText("Service Completed");
            }
        }
        @Override public int getItemCount() { return list.size(); }
        class RepairViewHolder extends RecyclerView.ViewHolder {
            TextView deviceName, status, eta;
            ProgressBar progressBar;
            RepairViewHolder(@NonNull View v) {
                super(v);
                deviceName = v.findViewById(R.id.tv_device_name);
                status = v.findViewById(R.id.tv_repair_status);
                progressBar = v.findViewById(R.id.progress_repair);
                eta = v.findViewById(R.id.tv_eta);
            }
        }
    }

    private void setupTrustSection() {
        trustViewPager = findViewById(R.id.pager_trust);
        if (trustViewPager != null) {
            List<TrustItem> trustItems = new ArrayList<>();
            trustItems.add(new TrustItem("Expert Technicians", "Certified pros for quality repairs.", R.drawable.ic_default_user, "https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?auto=format&fit=crop&w=800&q=80"));
            trustItems.add(new TrustItem("Express Service", "Same-day repair for most devices.", R.drawable.ic_nav_bookings, "https://images.unsplash.com/photo-1524592094714-0f0654e20314?auto=format&fit=crop&w=800&q=80"));
            trustItems.add(new TrustItem("Service Warranty", "Enjoy 30 days of peace of mind.", R.drawable.ic_home_repair_service, "https://images.unsplash.com/photo-1450101499163-c8848c66ca85?auto=format&fit=crop&w=800&q=80"));
            trustItems.add(new TrustItem("Genuine Components", "100% original manufacturer parts.", R.drawable.ic_laptop, "https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?auto=format&fit=crop&w=800&q=80"));
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
        trackerHandler.post(trackerRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
        trustHandler.removeCallbacks(trustRunnable);
        trackerHandler.removeCallbacks(trackerRunnable);
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
                if (id == R.id.nav_services) {
                    startActivity(new Intent(this, ServicesActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
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

    private void setupTechTips() {
        RecyclerView recyclerTips = findViewById(R.id.recycler_tech_tips);
        if (recyclerTips != null) {
            recyclerTips.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            List<TechTip> tips = new ArrayList<>();
            tips.add(new TechTip(
                    "Save Battery Life",
                    "Turn off background app refresh and lower brightness.",
                    "Battery health is crucial for your device's longevity. \n\n1. Avoid charging to 100% constantly; keeping it between 20% and 80% is ideal.\n2. Turn off Background App Refresh for apps you don't use often.\n3. Use Dark Mode if your phone has an OLED screen to save power.\n4. Keep your phone away from extreme temperatures.",
                    "https://images.unsplash.com/photo-1592434134753-a70baf7979d5?auto=format&fit=crop&w=800&q=80",
                    R.drawable.ic_smartphone));

            tips.add(new TechTip(
                    "Water Damage?",
                    "Turn off immediately. Don't shake. Rice doesn't always work.",
                    "If your device gets wet:\n\n1. Turn it off IMMEDIATELY. Do not try to charge it.\n2. Remove the SIM card and SD card.\n3. Dry the exterior with a cloth.\n4. Do NOT use a hairdryer; the heat can damage seals.\n5. Bring it to TechCare for a professional chemical wash immediately.",
                    "https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=crop&w=800&q=80",
                    R.drawable.ic_home_repair_service));

            tips.add(new TechTip(
                    "Speed Up PC",
                    "Clear temp files, disable startups, and update drivers.",
                    "Is your computer running slow?\n\n1. Check your Task Manager for 'Startup' apps and disable ones you don't need.\n2. Run a Disk Cleanup to remove temporary files.\n3. Uninstall unused software.\n4. Consider upgrading your HDD to an SSD for a massive speed boost.",
                    "https://images.unsplash.com/photo-1587831990711-23ca6441447b?auto=format&fit=crop&w=800&q=80",
                    R.drawable.ic_laptop));

            tips.add(new TechTip(
                    "Screen Care",
                    "Use microfiber cloth only. Avoid alcohol-based cleaners.",
                    "Your screen is the most used part of your phone.\n\n1. Never use Windex or household glass cleaners; they strip the oleophobic coating.\n2. Use a dedicated microfiber cloth.\n3. Apply a high-quality tempered glass protector.\n4. Don't keep your phone in the same pocket as keys or coins.",
                    "https://images.unsplash.com/photo-1512499617640-c74ae3a79d37?auto=format&fit=crop&w=800&q=80",
                    R.drawable.ic_smartphone));

            recyclerTips.setAdapter(new TechTipAdapter(tips));
        }
    }

    static class TechTip {
        String title;
        String description;
        String fullContent;
        String imageUrl;
        int iconRes;

        TechTip(String title, String description, String fullContent, String imageUrl, int iconRes) {
            this.title = title;
            this.description = description;
            this.fullContent = fullContent;
            this.imageUrl = imageUrl;
            this.iconRes = iconRes;
        }
    }

    class TechTipAdapter extends RecyclerView.Adapter<TechTipAdapter.TipViewHolder> {
        private final List<TechTip> items;

        TechTipAdapter(List<TechTip> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tech_tip, parent, false);
            return new TipViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
            TechTip item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvDesc.setText(item.description);
            holder.imgIcon.setImageResource(item.iconRes);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), TechTipDetailActivity.class);
                intent.putExtra("TIP_TITLE", item.title);
                intent.putExtra("TIP_CONTENT", item.fullContent);
                intent.putExtra("TIP_IMAGE", item.imageUrl);
                v.getContext().startActivity(intent);
            });

            View readMore = holder.itemView.findViewById(R.id.btn_read_more);
            if (readMore != null) {
                readMore.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), TechTipDetailActivity.class);
                    intent.putExtra("TIP_TITLE", item.title);
                    intent.putExtra("TIP_CONTENT", item.fullContent);
                    intent.putExtra("TIP_IMAGE", item.imageUrl);
                    v.getContext().startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class TipViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDesc;
            ImageView imgIcon;

            TipViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_tip_title);
                tvDesc = itemView.findViewById(R.id.tv_tip_desc);
                imgIcon = itemView.findViewById(R.id.img_tip_icon);
            }
        }
    }
}