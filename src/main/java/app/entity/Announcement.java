package app.entity;

/**
 * Contains the data for announcement
 */
public class Announcement {
    private long id;
    private String title;
    private String body;
    private String author;
    private String subject;

    private long timestamp;
    private boolean isRead;     // Has the announcement been read?

    public Announcement(long id, String title, String body, long timestamp, String author, String subject) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
        this.author = author;
        this.isRead = false;
        this.subject = subject;
    }

    /**
     * Return the id of the announcement.
     * @return      announcement id
     */
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * Return the title of the announcement.
     * @return      announcement title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Return the content of the announcement.
     * @return      announcement content body
     */
    public String getBody() {
        return body;
    }

    /**
     * Return the timestamp of when the announcement was published.
     * @return      announcement timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Return the author of the announcement.
     * @return      announcement author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Return the read status of the announcement. It does not alter it status.
     * @return
     */
    public boolean hasNotBeenRead() {
        return !isRead;
    }

    public String getSubject() {
        return subject;
    }

    /**
     * Sets the status of this announcement as read and returns itself for simplicity.
     * @return  returns the instance of this class
     */
    public Announcement read() {
        isRead = true;
        return this;
    }
}
