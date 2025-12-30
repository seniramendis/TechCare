// File: app/src/main/java/com/example/techcare/TestimonialsActivity.java
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
import java.util.List;

public class TestimonialsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testimonials);
        HeaderUtils.setupHeader(this);

        dbHelper = new DatabaseHelper(this);
        setupBottomNav();

        RecyclerView recyclerView = findViewById(R.id.recycler_all_reviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // FETCH FROM DB
        List<Review> allReviews = dbHelper.getAllReviews();

        // Pass the list to the adapter
        recyclerView.setAdapter(new AllReviewsAdapter(allReviews));
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_services); // Highlight Services as parent
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
        private final List<Review> items;
        AllReviewsAdapter(List<Review> items) { this.items = items; }

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
}