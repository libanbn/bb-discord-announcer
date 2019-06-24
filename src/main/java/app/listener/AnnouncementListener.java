package app.listener;

import app.entity.Announcement;

public interface AnnouncementListener {
    void update(Announcement[] newAnnouncements);
}
