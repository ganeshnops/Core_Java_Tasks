package com.booking.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * MOST IMPORTANT class for Concurrency rules (BookMyShow-style seat locking).
 *
 * Concept:
 *   When a user selects seats, we LOCK them for that user with a TTL (e.g., 5 min).
 *   Other users see those seats as unavailable.
 *   If payment completes within TTL  -> seats become BOOKED permanently.
 *   If payment fails OR TTL expires  -> locks are released, seats AVAILABLE again.
 *
 * Implementation:
 *   - Per (showId, seatId) lock entry with expiryEpochMillis.
 *   - putIfAbsent + CAS-style replace lets the FIRST thread win for the same seat.
 *   - A scheduled cleanup thread sweeps expired locks every second.
 *
 * Rules covered:
 *   Concurrency rule 1 - Two users cannot book the same seat simultaneously.
 *   Concurrency rule 2 - Seat locking happens before payment.
 *   Concurrency rule 3 - Locked seats unavailable to others.
 *   Concurrency rule 4 - Lock expires after a fixed time.
 *   Concurrency rule 5 - Failed payments release locked seats.
 *   Concurrency rule 6 - System prevents overbooking.
 *   Concurrency rule 7 - Seat allocation is thread-safe.
 */
public class SeatLockService {

    /** Default lock duration. Real BookMyShow uses ~5 min; we use 2 min for demo. */
    public static final long DEFAULT_LOCK_MILLIS = TimeUnit.MINUTES.toMillis(2);

    /** Key = "showId|seatId". Value = lock info (who/when expires). */
    private final Map<String, SeatLock> locks = new ConcurrentHashMap<>();

    /** Background cleaner runs every second. */
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "SeatLock-Cleaner");
        t.setDaemon(true);   // does not prevent JVM exit
        return t;
    });

    public SeatLockService() {
        cleaner.scheduleAtFixedRate(this::sweep, 1, 1, TimeUnit.SECONDS);
    }

    /** Try to lock all the given seats for one customer atomically.
     *  Returns the list of seats that were successfully locked.
     *  If ANY seat fails (already locked by someone else and not expired),
     *  ALL the already-grabbed locks in this call are released and an empty
     *  list is returned. (All-or-nothing semantics.) */
    public boolean tryLockAll(String showId, List<String> seatIds, String customerId) {
        return tryLockAll(showId, seatIds, customerId, DEFAULT_LOCK_MILLIS);
    }

    public boolean tryLockAll(String showId, List<String> seatIds, String customerId, long ttlMillis) {
        long expiry = System.currentTimeMillis() + ttlMillis;
        Map<String, SeatLock> taken = new HashMap<>();
        for (String seatId : seatIds) {
            String key = key(showId, seatId);
            SeatLock newLock = new SeatLock(customerId, expiry);
            SeatLock existing = locks.putIfAbsent(key, newLock);
            if (existing == null) {
                taken.put(key, newLock);
                continue;
            }
            // If existing is expired we try to replace it.
            if (existing.isExpired()) {
                if (locks.replace(key, existing, newLock)) {
                    taken.put(key, newLock);
                    continue;
                }
            }
            // could not take this seat -> rollback all takens
            for (Map.Entry<String, SeatLock> e : taken.entrySet()) {
                locks.remove(e.getKey(), e.getValue());
            }
            return false;
        }
        return true;
    }

    /** Release a single seat lock (only if the same customer holds it). */
    public boolean release(String showId, String seatId, String customerId) {
        String key = key(showId, seatId);
        SeatLock existing = locks.get(key);
        if (existing == null) return false;
        if (!existing.getCustomerId().equals(customerId)) return false;
        return locks.remove(key, existing);
    }

    /** Release all seats locked for this booking attempt. */
    public void releaseAll(String showId, List<String> seatIds, String customerId) {
        for (String s : seatIds) release(showId, s, customerId);
    }

    /** Is the seat locked by someone else (not expired)? */
    public boolean isLockedByOther(String showId, String seatId, String customerId) {
        SeatLock l = locks.get(key(showId, seatId));
        if (l == null) return false;
        if (l.isExpired()) return false;
        return !l.getCustomerId().equals(customerId);
    }

    /** Who holds the lock (null if none). */
    public String lockHolder(String showId, String seatId) {
        SeatLock l = locks.get(key(showId, seatId));
        if (l == null || l.isExpired()) return null;
        return l.getCustomerId();
    }

    private void sweep() {
        long now = System.currentTimeMillis();
        locks.entrySet().removeIf(e -> e.getValue().getExpiryEpoch() <= now);
    }

    /** Stop the background cleaner - call at app shutdown. */
    public void shutdown() {
        cleaner.shutdownNow();
    }

    private static String key(String showId, String seatId) {
        return showId + "|" + seatId;
    }

    /** Immutable lock record. */
    private static final class SeatLock {
        private final String customerId;
        private final long expiryEpoch;

        SeatLock(String customerId, long expiryEpoch) {
            this.customerId = customerId;
            this.expiryEpoch = expiryEpoch;
        }
        public String getCustomerId() { return customerId; }
        public long getExpiryEpoch()  { return expiryEpoch; }
        public boolean isExpired()    { return System.currentTimeMillis() > expiryEpoch; }
    }
}
