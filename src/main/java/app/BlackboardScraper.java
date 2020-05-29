package app;

import app.entity.Announcement;
import java.io.IOException;

public interface BlackboardScraper {
    Announcement[] getAnnouncements() throws Exception;
}
