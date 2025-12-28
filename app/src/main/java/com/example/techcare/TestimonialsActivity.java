package com.example.techcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class TestimonialsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testimonials);
        HeaderUtils.setupHeader(this);

        setupBottomNav();

        RecyclerView recyclerView = findViewById(R.id.recycler_all_reviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<HomeActivity.TestimonialItem> allReviews = new ArrayList<>();

        // Item 1: Simulated URL (Has Picture)
        allReviews.add(new HomeActivity.TestimonialItem("Fixed my MacBook screen in just 2 hours! Saved my work week.", "Kavindi Perera", 5, "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80"));

        // Item 2: Null (Default Drawable)
        allReviews.add(new HomeActivity.TestimonialItem("Good service but the parking was a bit difficult.", "Nuwan Pradeep", 4, null));

        // Item 3: Simulated URL (Has Picture)
        allReviews.add(new HomeActivity.TestimonialItem("Best price in town for battery replacement. Highly recommended!", "Dilani Silva", 5, "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=150&q=80"));

        // Item 4: Null (Default Drawable)
        allReviews.add(new HomeActivity.TestimonialItem("Fast, friendly, and honest. My go-to tech repair shop now.", "Kasun Rajapakshe", 4, null));

        // Item 5: Simulated URL (Has Picture)
        allReviews.add(new HomeActivity.TestimonialItem("Excellent service! They even cleaned my laptop fans for free.", "Chathura De Silva", 5, "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80"));

        // Item 6: Null (Default Drawable)
        allReviews.add(new HomeActivity.TestimonialItem("Genuine parts, but took a day longer than promised.", "Tharindu Bandara", 4, null));

        // Item 7: Simulated URL (Has Picture)
        allReviews.add(new HomeActivity.TestimonialItem("Great customer support. They explained everything clearly.", "Ishani Weerasinghe", 5, "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=150&q=80"));

        // Item 8: Simulated URL (Has Picture)
        allReviews.add(new HomeActivity.TestimonialItem("Highly recommended for urgent repairs. Super fast!", "Malith Kumara", 5, "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80"));

        recyclerView.setAdapter(new AllReviewsAdapter(allReviews));
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_services);

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_services) {
                    startActivity(new Intent(this, ServicesActivity.class));
                    return true;
                }
                if (id == R.id.nav_home) {
                    startActivity(new Intent(this, HomeActivity.class));
                    return true;
                }
                if (id == R.id.nav_bookings) {
                    startActivity(new Intent(this, MyBookingsActivity.class));
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

    class AllReviewsAdapter extends RecyclerView.Adapter<AllReviewsAdapter.ReviewViewHolder> {
        private final List<HomeActivity.TestimonialItem> items;
        AllReviewsAdapter(List<HomeActivity.TestimonialItem> items) { this.items = items; }

        @NonNull @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_testimonial, parent, false);
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            view.setLayoutParams(lp);
            return new ReviewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            HomeActivity.TestimonialItem item = items.get(position);
            holder.tvReview.setText("\"" + item.review + "\"");
            holder.tvAuthor.setText(item.author);

            StringBuilder stars = new StringBuilder();
            for(int i = 0; i < item.rating; i++) stars.append("⭐");
            holder.tvStars.setText(stars.toString());

            // --- STRICT IMAGE LOGIC ---
            if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
                // HAS PICTURE
                holder.imgAvatar.clearColorFilter(); // No tint
                holder.imgAvatar.setPadding(0,0,0,0);
                Glide.with(holder.itemView.getContext())
                        .load(item.imageUrl)
                        .circleCrop()
                        .into(holder.imgAvatar);
            } else {
                // DEFAULT DRAWABLE (NO API IMAGE)
                holder.imgAvatar.clearColorFilter(); // Remove tint so drawable colors show
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
}