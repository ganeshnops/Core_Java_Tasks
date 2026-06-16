package com.hospital.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OT room. Stores its booked time slots so we can detect overlaps.
 */
public class OperationTheatre {

    private final String id;
    private final String name;
    private final List<Slot> slots = new ArrayList<>();

    public OperationTheatre(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId()   { return id; }
    public String getName() { return name; }

    public List<Slot> getSlots() { return Collections.unmodifiableList(slots); }

    public void book(LocalDateTime start, LocalDateTime end, String surgeryId) {
        slots.add(new Slot(start, end, surgeryId));
    }

    public boolean overlaps(LocalDateTime start, LocalDateTime end) {
        for (Slot s : slots) {
            if (s.start.isBefore(end) && start.isBefore(s.end)) return true;
        }
        return false;
    }

    public static final class Slot {
        public final LocalDateTime start, end;
        public final String surgeryId;
        public Slot(LocalDateTime start, LocalDateTime end, String surgeryId) {
            this.start = start; this.end = end; this.surgeryId = surgeryId;
        }
        @Override public String toString() {
            return start + " -> " + end + " (" + surgeryId + ")";
        }
    }

    @Override
    public String toString() { return id + " | " + name + " | slots=" + slots.size(); }
}
