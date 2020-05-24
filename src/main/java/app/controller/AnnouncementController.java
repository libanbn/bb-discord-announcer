package app.controller;

import app.BlackboardScraper;
import app.listener.AnnouncementListener;
import app.entity.Announcement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class fetches the announcement data from the scraper, processes it and notifies all
 * objects that has subscribed to Blackboard announcements.
 */
public class AnnouncementController {
    private int interval;
    private BlackboardScraper blackboardScraper;
    private List<Announcement> announcements;
    private List<AnnouncementListener> listeners;

    /**
     * Creates an announcement controller.
     *
     * @param bs            a functioning instance of blackboard scraper
     * @param pullInterval  how long to wait before getting announcement stream from the scraper
     */
    public AnnouncementController(BlackboardScraper bs, int pullInterval) {
        blackboardScraper = bs;
        interval = pullInterval;
        announcements = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    /**
     * Return all stored announcements as an array.
     * @return      array of all announcements
     */
    public Announcement[] getAnnouncements() {
        return announcements.toArray(Announcement[]::new);
    }

    /**
     * Adds announcement given that they are unique. To validate its uniqueness, compare the title of the current
     * announcement with all of the announcements. Any announcements that are not unique are disregarded.
     *
     * @param newAnnouncement       announcement to add to the collection
     */
    private void addAnnouncement(Announcement newAnnouncement) {
        // Bool for checking if the announcement is unique.
        boolean aExists = false;

        // TODO: replace with while loop
        // Check if previously added announcement titles matches the current announcement
        for (Announcement a : announcements) {
            if (a.getTitle().equals(newAnnouncement.getTitle())) {
                aExists = true;
            }
        }

        // Add only if the announcement is unique
        if (!aExists) {
            announcements.add(newAnnouncement);
        }
    }

    /**
     * Checks if there are any announcements that has not been seen/opened.
     * @return      <code>true</code> if new announcements are found, otherwise, it returns <code>false</code>
     */
    public boolean isNewAnnouncements() {
        boolean foundNewAnnouncements = false;

        // TODO: replace with while loop
        // Checks trough all announcements
        for (Announcement a : announcements) {
            if (a.hasNotBeenRead()) {
                foundNewAnnouncements = true;
            }
        }

        return foundNewAnnouncements;
    }

    /**
     * Returns all unread announcements.
     *
     * @return      array of unread announcements
     */
    public Announcement[] getUnreadAnnouncements() {
        List<Announcement> unreadAnnouncements = new ArrayList<Announcement>();

        for (Announcement a : announcements) {
            if (a.hasNotBeenRead()) {
                // assumes the announcements has been parsed and marks them as read before being put on the list
                unreadAnnouncements.add(a.read());
            }
        }

        return unreadAnnouncements.toArray(Announcement[]::new);
    }

    /**
     * Scrape the announcement data from Blackboard periodically. The interval is gathered from
     * the local variable defined in constructor.
     */
    public void startPeriodicalScraping() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    // Parse the JSON string, convert them into announcement and return new announcements for the event
                    notifyListeners(blackboardScraper.getAnnouncements());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, interval, TimeUnit.MINUTES);
    }


    /**
     * Add an object that wants to be notified once announcements has been fetched.
     * @param listener  object that wants to receive notifications on announcements
     */
    public void addListener(AnnouncementListener listener) {
        listeners.add(listener);
    }

    /**
     * Unsubscribe an object that no longer wants to receive notifications on announcements.
     * @param listener  object that does not want to to receive notifications on announcements
     */
    public void removeListener(AnnouncementListener listener) {
        listeners.remove(listener);
    }

    /**
     * When new announcements has been fetched from the scraper, notify all the listeners and send
     * them the new announcements.
     * @param unreadAnnouncements   an array of unread/unopened announcements
     */
    private void notifyListeners(Announcement[] unreadAnnouncements) {
        for (AnnouncementListener c : listeners) {
            c.update(unreadAnnouncements);
        }
    }
}
