package app.controller;

import app.listener.AnnouncementListener;
import app.BlackboardScraper;
import app.entity.Announcement;
import app.util.WebTools;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.util.security.Credential;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AnnouncementController {
    private ArrayList<Announcement> announcements;
    private BlackboardScraper blackboardScraper;
    private int interval;
    // Used for events
    private List<AnnouncementListener> listeners = new ArrayList<>();

    /**
     * Creates an announcement controller.
     *
     * @param bs            a functioning instance of blackboard scraper
     * @param pullInterval  how long to wait before getting announcement stream from the scraper
     */
    public AnnouncementController(BlackboardScraper bs, int pullInterval) {
        blackboardScraper = bs;
        interval = pullInterval;
        announcements = new ArrayList<Announcement>();
    }

    /**
     * Converts JSON string into a set of announcements and stores them as unread announcements. This method is
     * ran by the event (more specifically: it's ran by 'startPeriodicalScraping()' inside the thread. If there are any
     * new announcement, return them. JSON dummy data is provided in the resource folder for better understanding.
     *
     * @param jsonString        JSON as string containing announcements from Blackboard
     * @return                  a collection of unread announcements if there are any
     * @throws ParseException   if the JSON string is malformed
     */
    private Announcement[] convertJsonToAnnouncements(String jsonString) throws ParseException {
        // Create a JSON object from the string
        JSONParser jsonParser = new JSONParser();
        JSONObject json = (JSONObject) jsonParser.parse(jsonString);

        // Go into the key containing the announcements
        JSONArray streamEntries = (JSONArray) json.get("sv_streamEntries");

        // Iterate thought the values (in this case, the values from the individual announcements) inside and create
        // an announcement object
        for (Object entryObj : streamEntries) {
            JSONObject entry = (JSONObject) entryObj;
            JSONObject itemSpecificData = (JSONObject) entry.get("itemSpecificData");
            JSONObject notificationDetails = (JSONObject) itemSpecificData.get("notificationDetails");

            long announcementTimestamp = (long) entry.get("se_timestamp");

            String announcementTitle = (String) notificationDetails.get("announcementTitle");
            String announcementBody = (String) notificationDetails.get("announcementBody");
            String announcementFirstName = (String) notificationDetails.get("announcementFirstName");
            String announcementLastName = (String) notificationDetails.get("announcementLastName");

            // Make sure the announcement is valid by checking for null values in title and body
            if (announcementTitle != null && announcementBody != null) {
                // Add new announcement
                addAnnouncement(new Announcement(Credential.MD5.digest(announcementTitle).substring(4, 11),
                                    announcementTitle,
                                    WebTools.cleanseTextFromHtmlTags(announcementBody),
                                    announcementTimestamp,
                                    announcementFirstName.concat(" ").concat(announcementLastName))
                );
            }
        }

        return getUnreadAnnouncements();
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
     * Checks if there are any announcements that has not been seen.
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

        // Convert the announcement list to primitive array
        return unreadAnnouncements.toArray(Announcement[]::new);
    }

    private Announcement[] readFromStorage() {
        return (Announcement[]) null;
    }

    public void startPeriodicalScraping() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    // Parse the JSON string, convert them into announcement and return new announcements for the event
                    notifyListeners(convertJsonToAnnouncements(blackboardScraper.getStreamEntries()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, interval, TimeUnit.MINUTES);
    }

    //region Event subscription methods
    public void addListener(AnnouncementListener listener) {
        listeners.add(listener);
    }

    public void removeListener(AnnouncementListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(Announcement[] unreadAnnouncements) {
        for (AnnouncementListener c : listeners) {
            c.update(unreadAnnouncements);
        }
    }
    //endregion
}
