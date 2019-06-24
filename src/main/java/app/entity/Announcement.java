package app.entity;

public class Announcement {
    private String title;
    private String body;
    private long timestamp;
    private boolean isRead;

    public Announcement(String title, String body, long timestamp) {
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Announcement read() {
        isRead = true;
        return this;
    }

    public boolean hasNotBeenRead() {
        return !isRead;
    }
}
