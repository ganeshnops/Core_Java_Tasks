package com.booking.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import com.booking.enums.SeatCategory;
import com.booking.model.Show;

/**
 * Pricing calculation.
 *  - Weekend pricing multiplier (Pricing rule 1).
 *  - Per-category multiplier (Pricing rule 2).
 *  - Dynamic factor for special shows (Pricing rule 3).
 *  - Tax + convenience fee added separately (Pricing rules 4, 5).
 *  - Discount applied before tax (Pricing rule 6) - applied in BookingService.
 */
public class PricingService {

    public static final double TAX_RATE = 0.18;           // 18%
    public static final double CONVENIENCE_FEE_PER_SEAT = 25.0;
    public static final double WEEKEND_MULTIPLIER = 1.25;

    /** Per-seat base price for the given show + seat category. */
    public double seatPrice(Show show, SeatCategory category) {
        double price = show.getBasePrice() * category.getPriceMultiplier();
        DayOfWeek day = show.getStartTime().getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            price *= WEEKEND_MULTIPLIER;
        }
        // Dynamic factor - special shows (premiere, midnight, etc.) charged more
        if (show.getStartTime().getHour() >= 21) price *= 1.1;   // late-night surcharge

        return round(price);
    }

    public double tax(double subtotal)              { return round(subtotal * TAX_RATE); }
    public double convenienceFee(int seatCount)     { return seatCount * CONVENIENCE_FEE_PER_SEAT; }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /** Refund amount given total + how many hours before show. (Cancellation rule 3) */
    public double refundAmount(double total, double hoursBeforeShow) {
        if (hoursBeforeShow >= 24) return total;                  // full refund
        if (hoursBeforeShow >= 2)  return round(total * 0.50);     // 50% refund
        return 0;                                                 // no refund inside 2 hours
    }
}
