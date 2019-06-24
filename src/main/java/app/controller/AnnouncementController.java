package app.controller;

import app.listener.AnnouncementListener;
import app.BlackboardScraper;
import app.entity.Announcement;
import app.util.WebTools;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

    public AnnouncementController(BlackboardScraper bs, int pullInterval) {
        blackboardScraper = bs;
        interval = pullInterval;
        announcements = new ArrayList<Announcement>();
    }

    public Announcement[] retrieveAnnouncements(String jsonString) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject json = (JSONObject) jsonParser.parse(jsonString);
        JSONArray streamEntries = (JSONArray) json.get("sv_streamEntries");

        for (Object entryObj : streamEntries) {
            JSONObject entry = (JSONObject) entryObj;
            JSONObject itemSpecificData = (JSONObject) entry.get("itemSpecificData");
            JSONObject notificationDetails = (JSONObject) itemSpecificData.get("notificationDetails");

            String announcementTitle = (String) notificationDetails.get("announcementTitle");
            String announcementBody = (String) notificationDetails.get("announcementBody");
            long announcementTimestamp = (long) entry.get("se_timestamp");

            if (announcementTitle != null && announcementBody != null) {
                addAnnouncement(new Announcement(announcementTitle,
                        WebTools.cleanseTextFromHtmlTags(announcementBody),
                        announcementTimestamp));
            }
        }

        return getUnreadAnnouncements();
    }

    public Announcement[] getAnnouncements() {
        return announcements.toArray(Announcement[]::new);
    }

    private void addAnnouncement(Announcement newAnnouncement) {
        // Check if the announcement is unique
        // TODO: replace with while loop
        boolean aExists = false;
        for (Announcement a : announcements) {
            if (a.getTitle().equals(newAnnouncement.getTitle())) {
                aExists = true;
            }
        }

        if (!aExists) {
            announcements.add(newAnnouncement);
        }
    }

    public boolean isNewAnnouncements() {
        boolean foundNewAnnouncements = false;

        //TODO: replace with while loop
        for (Announcement a : announcements) {
            foundNewAnnouncements = a.hasNotBeenRead() || foundNewAnnouncements;
        }

        return foundNewAnnouncements;
    }

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

    private Announcement[] readFromStorage() {
        return (Announcement[]) null;
    }

    public void startIntervalPulling() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    notifyListeners(retrieveAnnouncements(blackboardScraper.getStreamEntries()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, interval, TimeUnit.MINUTES);
    }

    // Event methods under
    public AnnouncementController addListener(AnnouncementListener listener) {
        listeners.add(listener);
        return this;
    }

    public AnnouncementController removeListener(AnnouncementListener listener) {
        listeners.remove(listener);
        return this;
    }

    private void notifyListeners(Announcement[] unreadAnnouncements) {
        for (AnnouncementListener c : listeners) {
            c.update(unreadAnnouncements);
        }
    }
}
