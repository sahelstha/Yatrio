package org.example.Yatrio.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages data refresh notifications across different controllers
 * to ensure real-time updates when data changes
 */
public class DataRefreshManager {

    // List of listeners for attraction data changes
    private static final List<DataRefreshListener> attractionListeners = new ArrayList<>();

    // List of listeners for booking data changes
    private static final List<DataRefreshListener> bookingListeners = new ArrayList<>();

    // List of listeners for user data changes
    private static final List<DataRefreshListener> userListeners = new ArrayList<>();

    // List of listeners for guide data changes
    private static final List<DataRefreshListener> guideListeners = new ArrayList<>();

    /**
     * Interface for controllers that want to be notified of data changes
     */
    public interface DataRefreshListener {
        void onDataRefresh();
    }

    // Attraction data management
    public static void addAttractionListener(DataRefreshListener listener) {
        if (!attractionListeners.contains(listener)) {
            attractionListeners.add(listener);
            System.out.println("Added attraction listener: " + listener.getClass().getSimpleName());
        }
    }

    public static void removeAttractionListener(DataRefreshListener listener) {
        attractionListeners.remove(listener);
        System.out.println("Removed attraction listener: " + listener.getClass().getSimpleName());
    }

    public static void notifyAttractionDataChanged() {
        System.out.println("Notifying " + attractionListeners.size() + " attraction listeners of data change");
        for (DataRefreshListener listener : new ArrayList<>(attractionListeners)) {
            try {
                listener.onDataRefresh();
            } catch (Exception e) {
                System.err.println("Error notifying attraction listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Booking data management
    public static void addBookingListener(DataRefreshListener listener) {
        if (!bookingListeners.contains(listener)) {
            bookingListeners.add(listener);
            System.out.println("Added booking listener: " + listener.getClass().getSimpleName());
        }
    }

    public static void removeBookingListener(DataRefreshListener listener) {
        bookingListeners.remove(listener);
        System.out.println("Removed booking listener: " + listener.getClass().getSimpleName());
    }

    public static void notifyBookingDataChanged() {
        System.out.println("Notifying " + bookingListeners.size() + " booking listeners of data change");
        for (DataRefreshListener listener : new ArrayList<>(bookingListeners)) {
            try {
                listener.onDataRefresh();
            } catch (Exception e) {
                System.err.println("Error notifying booking listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // User data management
    public static void addUserListener(DataRefreshListener listener) {
        if (!userListeners.contains(listener)) {
            userListeners.add(listener);
            System.out.println("Added user listener: " + listener.getClass().getSimpleName());
        }
    }

    public static void removeUserListener(DataRefreshListener listener) {
        userListeners.remove(listener);
        System.out.println("Removed user listener: " + listener.getClass().getSimpleName());
    }

    public static void notifyUserDataChanged() {
        System.out.println("Notifying " + userListeners.size() + " user listeners of data change");
        for (DataRefreshListener listener : new ArrayList<>(userListeners)) {
            try {
                listener.onDataRefresh();
            } catch (Exception e) {
                System.err.println("Error notifying user listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Guide data management
    public static void addGuideListener(DataRefreshListener listener) {
        if (!guideListeners.contains(listener)) {
            guideListeners.add(listener);
            System.out.println("Added guide listener: " + listener.getClass().getSimpleName());
        }
    }

    public static void removeGuideListener(DataRefreshListener listener) {
        guideListeners.remove(listener);
        System.out.println("Removed guide listener: " + listener.getClass().getSimpleName());
    }

    public static void notifyGuideDataChanged() {
        System.out.println("Notifying " + guideListeners.size() + " guide listeners of data change");
        for (DataRefreshListener listener : new ArrayList<>(guideListeners)) {
            try {
                listener.onDataRefresh();
            } catch (Exception e) {
                System.err.println("Error notifying guide listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Utility methods
    public static void clearAllListeners() {
        attractionListeners.clear();
        bookingListeners.clear();
        userListeners.clear();
        guideListeners.clear();
        System.out.println("Cleared all data refresh listeners");
    }

    public static void notifyAllDataChanged() {
        notifyAttractionDataChanged();
        notifyBookingDataChanged();
        notifyUserDataChanged();
        notifyGuideDataChanged();
    }

    // Debug methods
    public static void printListenerCounts() {
        System.out.println("=== Data Refresh Manager Status ===");
        System.out.println("Attraction listeners: " + attractionListeners.size());
        System.out.println("Booking listeners: " + bookingListeners.size());
        System.out.println("User listeners: " + userListeners.size());
        System.out.println("Guide listeners: " + guideListeners.size());
        System.out.println("===================================");
    }
}
