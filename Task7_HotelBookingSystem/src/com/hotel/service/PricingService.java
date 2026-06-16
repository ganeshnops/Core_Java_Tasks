package com.hotel.service;

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.hotel.model.Room;

/**
 * Pricing with weekend + seasonal surcharges + tax.
 */
public class PricingService {

    public static final double TAX_RATE = 0.12;
    public static final double WEEKEND_MULTIPLIER = 1.30;   // 30% more on Sat/Sun

    /** Calculate room charges for [checkIn, checkOut) night-by-night. */
    public double calculateSubtotal(Room room, LocalDate checkIn, LocalDate checkOut) {
        double total = 0;
        for (LocalDate d = checkIn; d.isBefore(checkOut); d = d.plusDays(1)) {
            double nightly = room.getPricePerNight();
            DayOfWeek dow = d.getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                nightly *= WEEKEND_MULTIPLIER;
            }
            total += nightly;
        }
        return round(total);
    }

    public double tax(double subtotal) { return round(subtotal * TAX_RATE); }

    /** Refund tier (Cancellation Rule): >=7 days = 100%, >=2 days = 50%, <2 days = 0%. */
    public double refundAmount(double total, long daysToCheckIn) {
        if (daysToCheckIn >= 7) return total;
        if (daysToCheckIn >= 2) return round(total * 0.5);
        return 0;
    }

    /** Late checkout fee - half-day rate per hour late after 11am. */
    public double lateCheckOutFee(Room room, int hoursLate) {
        if (hoursLate <= 0) return 0;
        return round(room.getPricePerNight() * 0.5 * hoursLate / 24.0);   // small fee per hour
    }

    private static double round(double v) { return Math.round(v * 100.0) / 100.0; }
}
