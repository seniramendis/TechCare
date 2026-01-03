package com.example.techcare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqViewHolder> {

    private final List<FaqItem> faqList;

    public FaqAdapter(List<FaqItem> faqList) {
        this.faqList = faqList;
    }

    @NonNull
    @Override
    public FaqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
        return new FaqViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FaqViewHolder holder, int position) {
        FaqItem item = faqList.get(position);
        holder.tvQuestion.setText(item.question);
        holder.tvAnswer.setText(item.answer);

        // Handle Expand/Collapse visibility
        boolean isExpanded = item.isExpanded;
        holder.tvAnswer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // Rotate icon for visual feedback (90 degrees if expanded)
        holder.imgIcon.setRotation(isExpanded ? 90f : 0f);

        holder.itemView.setOnClickListener(v -> {
            item.isExpanded = !item.isExpanded;
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    // Data Class for FAQ
    public static class FaqItem {
        String question;
        String answer;
        boolean isExpanded;

        public FaqItem(String question, String answer) {
            this.question = question;
            this.answer = answer;
            this.isExpanded = false;
        }
    }

    static class FaqViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvAnswer;
        ImageView imgIcon;

        public FaqViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvAnswer = itemView.findViewById(R.id.tv_answer);
            imgIcon = itemView.findViewById(R.id.img_expand_icon);
        }
    }
}