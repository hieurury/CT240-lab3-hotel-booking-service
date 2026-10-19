package com.ctu.service;

public class DiscountCalculator {
    public double calculateDiscountPrice(double originalPrice, double discountPercent) {
        if (originalPrice < 0 || discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Gia hoac phan tram chiet khau khong hop le!");
        }
        return originalPrice * (1 - discountPercent / 100.0);
    }
}
