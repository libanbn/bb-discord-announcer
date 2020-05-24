package app;

import app.entity.Announcement;
import app.util.WebTools;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Interacts with NTNU Blackboard to get relevant announcement for your class subjects.
 */
public class NtnuBlackboardScraper implements BlackboardScraper {
    private WebClient client;

    private String username;
    private String password;

    /**
     * Sets up the client that will navigate though Blackboard and get the announcements.
     *
     * @param username  FEIDE username
     * @param password  FEIDE password
     */
    public NtnuBlackboardScraper(String username, String password) {
        this.username = username;
        this.password = password;

        // Turn off console logging as this spams the console
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        // Define web browser options optimized for Blackboard
        client = new WebClient(BrowserVersion.CHROME);
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setRedirectEnabled(true);

        client.getCookieManager().setCookiesEnabled(true);
    }

    /**
     * Collects announcement from Blackboard associated with the Blackboard user who is
     * currently logged in.
     *
     * @return                  Blackboard announcements
     * @throws IOException      when something goes wrong, ironically
     */
    @Override
    public Announcement[] getAnnouncements() throws IOException {
        // Navigate to NTNU Blackboard login page
        HtmlPage page = (HtmlPage) client.getPage("https://ntnu.blackboard.com");

        // If the element is not present, that means the web client most likely has been redirected
        // to the user homepage
        if (page.getElementById("altLogin") != null) {
            // Alternative login element
            ((HtmlAnchor) page.getElementById("altLogin")).click();

            HtmlForm form = (HtmlForm) page.getFormByName("login");
            form.getInputByName("user_id").setValueAttribute(username);
            form.getInputByName("password").setValueAttribute(password);
            page = ((HtmlSubmitInput) page.getElementById("entry-login")).click();
        }

        WebRequest request = new WebRequest(new URL("https://ntnu.blackboard.com/webapps/streamViewer/streamViewer?cmd=view&streamName=alerts&globalNavigation=false"), HttpMethod.GET);
        request.setAdditionalHeader("Cookie", WebTools.cookiesAsRequestHeader(client.getCookieManager().getCookies()));

        // This part requires JS
        client.getOptions().setJavaScriptEnabled(true);
        page = client.getPage(request);

        // DOM element containing the list of announcement elements
        HtmlDivision announcementContainer = (HtmlDivision) page.getElementById("left_stream_alerts");

        // Blackboard uses JS to load the alerts and therefore need to wait for it to complete fetching
        while (announcementContainer == null || !announcementContainer.hasChildNodes()) {
            client.waitForBackgroundJavaScript(1000);
        }

        List<Announcement> announcements = new ArrayList<>();

        // Announcements are inside elements associated with the HTML class
        List<DomNode> nodes = page.getByXPath("//*[@class='stream_item']");

        for (DomNode node : nodes) {
            Announcement announcement = extractAnnouncementFromDomElement(node);
            if (announcement != null) {
                announcements.add(announcement);
            }
        }

        return announcements.toArray(Announcement[]::new);
    }

    /**
     * Takes out the string values of the announcement DOM elements and creates an Announcement
     * entity from it.
     * @param node  the DOM element that contains the announcement information
     * @return      an entity
     */
    private Announcement extractAnnouncementFromDomElement(DomNode node) {
        // Event notifications are included in the announcement stream of BB and follows another
        // design structure that doesn't have the elements defined below. We ignore these.
        if (node.getFirstByXPath(".//span[@class='announcementType']") != null) {
            String id = ((HtmlDivision) node).getId().replaceAll("[-A-z]*", "");
            String title = getTextInsideHtmlSpanByXPath(node, ".//span[@class='announcementTitle']");
            String subject = getTextInsideHtmlSpanByXPath(node, ".//span[@class='stream_area_name']");
            DomNode body = node.getFirstByXPath(".//span[@class='announcementBody']");

            String author = "unknown";
            String timestamp = getTextInsideHtmlSpanByXPath(node, ".//span[@class='stream_datestamp']");
            return new Announcement(id, title, body.asText(), 2, author, subject);
        } else {
            return null;
        }

    }

    /**
     * Get the text value inside a span element.
     * @param parent    the parent of all the span elements of the text values that
     *                  will be extracted
     * @param xPath     xpath of the span element that contains the text value
     * @return          text value containing the targeted announcement text data
     */
    private String getTextInsideHtmlSpanByXPath(DomNode parent, String xPath) {
        HtmlSpan child = (HtmlSpan) parent.getFirstByXPath(xPath);
        DomText textNode = (DomText) child.getFirstChild();

        return textNode.getWholeText();
    }
}
