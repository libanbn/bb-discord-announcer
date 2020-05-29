package app;

import app.entity.Announcement;
import app.util.WebTools;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.jetty.util.security.Credential;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
    public Announcement[] getAnnouncements() throws Exception {
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

        // For some reason, this page has to be accessed if we want to prevent having no data
        // inside the announcement stream from BB
        WebRequest request = new WebRequest(new URL("https://ntnu.blackboard.com/webapps/streamViewer/streamViewer?cmd=view&streamName=alerts&globalNavigation=false"), HttpMethod.GET);
        request.setAdditionalHeader("Cookie", WebTools.cookiesAsRequestHeader(client.getCookieManager().getCookies()));

        // This part requires JS or no announcements will be shown later
        client.getOptions().setJavaScriptEnabled(true);
        page = client.getPage(request);

        // Create a post request for BB announcements
        URL announcementUrl = new URL("https://ntnu.blackboard.com/webapps/streamViewer/streamViewer");
        request = new WebRequest(announcementUrl, HttpMethod.POST);

        // Request header
        request.setAdditionalHeader("Accept", "*/*");
        request.setAdditionalHeader("User-Agent", "Chrome/81.0.4044.138");
        request.setAdditionalHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        request.setAdditionalHeader("Cache-Control", "no-cache");
        request.setAdditionalHeader("Cookie", WebTools.cookiesAsRequestHeader(client.getCookieManager().getCookies()));

        // Request body
        List<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("cmd", "loadStream"));
        params.add(new NameValuePair("streamName", "alerts"));
        params.add(new NameValuePair("providers", "%7B%7D"));
        params.add(new NameValuePair("forOverview", "false"));

        request.setRequestParameters(params);


        // Issues the request, but it will empty and useless json. To fix this, the same request
        // must be sent again
        client.getPage(request);
        String json = client.getPage(request).getWebResponse().getContentAsString();

        return convertJsonToAnnouncements(json);
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
        JSONArray stream = (JSONArray) json.get("sv_streamEntries");
        List<Announcement> announcements = new ArrayList<>();

        // Iterate thought the values (in this case, the values from the individual announcements) inside and create
        // an announcement object
        for (Object entryObj : stream) {
            JSONObject entry = (JSONObject) entryObj;
            JSONObject itemSpecificData = (JSONObject) entry.get("itemSpecificData");
            JSONObject notificationDetails = (JSONObject) itemSpecificData.get("notificationDetails");

            // Relevant announcement data are gathered from this point
            long timestamp = (long) entry.get("se_timestamp");

            long id = (long) notificationDetails.get("actorId");
            String title = (String) notificationDetails.get("announcementTitle");
            String body = (String) notificationDetails.get("announcementBody");
            String authorFirstName = (String) notificationDetails.get("announcementFirstName");
            String authorLastName = (String) notificationDetails.get("announcementLastName");

            // Make sure the announcement is valid by checking for null values in title and body
            if (title != null && body != null) {
                // Add new announcement
                announcements.add(new Announcement(
                    id,
                    title,
                    WebTools.cleanseTextFromHtmlTags(body),
                    timestamp,
                    authorFirstName.concat(" ").concat(authorLastName),
                    // TODO fetch subject name using the announcement subject ID
                    "unknown subject")
                );
            }
        }

        return announcements.toArray(Announcement[]::new);
    }
}
