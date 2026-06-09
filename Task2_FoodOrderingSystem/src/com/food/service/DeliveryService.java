package com.food.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.food.enums.DeliveryPartnerStatus;
import com.food.exception.DeliveryPartnerNotAvailableException;
import com.food.model.DeliveryPartner;

/**
 * Delivery partner assignment.
 *  - Rule 31: assigned automatically when order is ready.
 *  - Rule 32: only available partners.
 *  - Rule 33: nearest first.
 *  - Rule 34: cap per partner.
 *  - Rule 56: inactive partners never receive assignments.
 *
 * Threading: the partner selection + reservation must be atomic, so we
 * synchronize the whole assign() method on the partners map.
 */
public class DeliveryService {

    private final Map<String, DeliveryPartner> partners = new ConcurrentHashMap<>();
    private final Object assignLock = new Object();

    public DeliveryPartner add(DeliveryPartner p) { partners.put(p.getId(), p); return p; }
    public DeliveryPartner get(String id)          { return partners.get(id); }
    public Collection<DeliveryPartner> getAll()    { return Collections.unmodifiableCollection(partners.values()); }

    /** Assign the closest available partner. */
    public DeliveryPartner assignNearest() {
        synchronized (assignLock) {
            List<DeliveryPartner> candidates = new ArrayList<>();
            for (DeliveryPartner p : partners.values()) {
                if (p.getStatus() == DeliveryPartnerStatus.AVAILABLE
                        && p.getActiveOrders() < p.getMaxActiveOrders()) {
                    candidates.add(p);
                }
            }
            if (candidates.isEmpty()) {
                throw new DeliveryPartnerNotAvailableException("No delivery partner available right now.");
            }
            // Nearest first
            candidates.sort((a, b) -> Double.compare(a.getDistanceKm(), b.getDistanceKm()));
            for (DeliveryPartner candidate : candidates) {
                if (candidate.tryTakeOrder()) return candidate;
            }
            throw new DeliveryPartnerNotAvailableException("All nearby partners are full.");
        }
    }

    public void release(String partnerId) {
        DeliveryPartner p = partners.get(partnerId);
        if (p != null) p.releaseOrder();
    }
}
