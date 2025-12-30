
package com.example.techcare;

public class Review {
    public String review;
    public String author;
    public int rating;
    public String imageUrl;

    public Review(String review, String author, int rating, String imageUrl) {
        this.review = review;
        this.author = author;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }
}