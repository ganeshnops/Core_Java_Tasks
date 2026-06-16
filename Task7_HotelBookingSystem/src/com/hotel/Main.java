package com.hotel;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.hotel.enums.RoomType;
import com.hotel.exception.HotelException;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Hotel;
import com.hotel.model.Room;
import com.hotel.service.BookingService;
import com.hotel.service.CustomerService;
import com.hotel.service.HotelService;
import com.hotel.service.NotificationService;
import com.hotel.service.PaymentService;
import com.hotel.service.PricingService;
import com.hotel.service.RoomService;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final HotelService hotelService = new HotelService();
    private static final RoomService roomService = new RoomService();
    private static final CustomerService customerService = new CustomerService();
    private static final PricingService pricingService = new PricingService();
    private static final PaymentService paymentService = new PaymentService();
    private static final NotificationService notifyService = new NotificationService();
    private static final BookingService bookingService = new BookingService(
            roomService, customerService, pricingService, paymentService, notifyService);

    public static void main(String[] args) {
        printBanner();
        bootstrap();
        runHappyPath();

        boolean run = true;
        while (run) {
            printMenu();
            int c = readInt("Choose: ");
            System.out.println();
            try {
                switch (c) {
                    case 1: listRooms();              break;
                    case 2: searchAvailableRooms();   break;
                    case 3: bookRoom();               break;
                    case 4: checkIn();                break;
                    case 5: checkOut();               break;
                    case 6: cancelBooking();          break;
                    case 7: listBookings();           break;
                    case 8: showReports();            break;
                    case 9: run = false;              break;
                    default: System.out.println("Invalid.");
                }
            } catch (HotelException ex) {
                System.out.println("BUSINESS ERROR: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("UNEXPECTED: " + ex.getMessage());
            }
            System.out.println();
        }
        System.out.println("Goodbye!");
        sc.close();
    }

    private static void bootstrap() {
        System.out.println("=========================================================");
        System.out.println(" PHASE 1 - Bootstrap demo data");
        System.out.println("=========================================================");

        Hotel h1 = hotelService.add(new Hotel("H1", "Taj Bangalore",  "Bangalore", "MG Road"));
        Hotel h2 = hotelService.add(new Hotel("H2", "ITC Hyderabad", "Hyderabad", "Banjara Hills"));

        roomService.add(new Room("R101", h1.getId(), "101", RoomType.SINGLE, RoomType.SINGLE.getBasePricePerNight(), "WiFi, AC"));
        roomService.add(new Room("R102", h1.getId(), "102", RoomType.DOUBLE, RoomType.DOUBLE.getBasePricePerNight(), "WiFi, AC, TV"));
        roomService.add(new Room("R201", h1.getId(), "201", RoomType.DELUXE, RoomType.DELUXE.getBasePricePerNight(), "WiFi, AC, TV, Mini-bar"));
        roomService.add(new Room("R301", h1.getId(), "301", RoomType.SUITE,  RoomType.SUITE.getBasePricePerNight(),  "WiFi, AC, TV, Mini-bar, Living room"));
        roomService.add(new Room("R401", h2.getId(), "401", RoomType.DOUBLE, RoomType.DOUBLE.getBasePricePerNight(), "WiFi, AC"));
        roomService.add(new Room("R402", h2.getId(), "402", RoomType.SUITE,  RoomType.SUITE.getBasePricePerNight(),  "WiFi, AC, TV"));

        customerService.register("Alice",   "alice@test.com",   "9000000001", true);
        customerService.register("Bob",     "bob@test.com",     "9000000002", true);
        customerService.register("Charlie", "charlie@test.com", "9000000003", false);   // UNVERIFIED

        System.out.println("  2 hotels, 6 rooms, 3 customers (1 unverified)");
        System.out.println();
    }

    private static void runHappyPath() {
        System.out.println("=========================================================");
        System.out.println(" PHASE 2 - Happy-path automatic booking for Alice");
        System.out.println("=========================================================");
        try {
            LocalDate checkIn  = LocalDate.now().plusDays(7);
            LocalDate checkOut = LocalDate.now().plusDays(10);
            Booking b = bookingService.book("C1001", "R102", checkIn, checkOut);
            System.out.println("  Booking: " + b);
            // Try to book SAME room SAME dates - should fail (overlap detection!)
            try {
                bookingService.book("C1002", "R102", checkIn, checkOut);
            } catch (Exception ex) {
                System.out.println("  [OK] Overlap rejected: " + ex.getMessage());
            }
        } catch (Exception ex) {
            System.out.println("  Demo flow error: " + ex.getMessage());
        }
        System.out.println();
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("##########################################################");
        System.out.println("#             JHires Hotel Booking System                #");
        System.out.println("##########################################################");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println("==================== MENU ====================");
        System.out.println(" 1. List all rooms");
        System.out.println(" 2. Search available rooms for date range");
        System.out.println(" 3. Book a room");
        System.out.println(" 4. Check in");
        System.out.println(" 5. Check out");
        System.out.println(" 6. Cancel booking");
        System.out.println(" 7. List all bookings");
        System.out.println(" 8. Show reports (revenue)");
        System.out.println(" 9. Exit");
        System.out.println("==============================================");
    }

    private static void listRooms() {
        for (Room r : roomService.getAll()) System.out.println(r);
    }

    private static void searchAvailableRooms() {
        String hotelId = readString("Hotel ID    : ");
        String inStr  = readString("Check-in YYYY-MM-DD : ");
        String outStr = readString("Check-out YYYY-MM-DD: ");
        LocalDate in = LocalDate.parse(inStr);
        LocalDate out = LocalDate.parse(outStr);
        List<Room> available = bookingService.availableRooms(hotelId, in, out);
        System.out.println("Available rooms:");
        for (Room r : available) System.out.println("  " + r);
    }

    private static void bookRoom() {
        String cust = readString("Customer ID         : ");
        String roomId = readString("Room ID             : ");
        String inStr  = readString("Check-in YYYY-MM-DD : ");
        String outStr = readString("Check-out YYYY-MM-DD: ");
        Booking b = bookingService.book(cust, roomId, LocalDate.parse(inStr), LocalDate.parse(outStr));
        System.out.println("Booking: " + b);
    }

    private static void checkIn() {
        String bookingId = readString("Booking ID : ");
        Booking b = bookingService.checkIn(bookingId);
        System.out.println("Checked in: " + b);
    }

    private static void checkOut() {
        String bookingId = readString("Booking ID : ");
        int hoursLate = readInt("Hours late (0 if on time): ");
        Booking b = bookingService.checkOut(bookingId, hoursLate);
        System.out.println("Checked out: " + b);
    }

    private static void cancelBooking() {
        String bookingId = readString("Booking ID : ");
        Booking b = bookingService.cancel(bookingId);
        System.out.println("Cancelled: " + b);
    }

    private static void listBookings() {
        for (Booking b : bookingService.getAll()) System.out.println(b);
    }

    private static void showReports() {
        System.out.println("--- Daily Revenue ---");
        for (Map.Entry<String, Double> e : bookingService.getDailyRevenue().entrySet()) {
            System.out.println("  " + e.getKey() + " : Rs." + e.getValue());
        }
        System.out.println("--- Hotel-wise Revenue ---");
        for (Map.Entry<String, Double> e : bookingService.getHotelRevenue().entrySet()) {
            System.out.println("  " + e.getKey() + " : Rs." + e.getValue());
        }
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("  Invalid number."); }
        }
    }
}
