package app.util;

import com.gargoylesoftware.htmlunit.util.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.util.Iterator;
import java.util.Set;

public class WebTools {

    /**
     * Removes all HTML tags from texts and removes extra whitespaces at the start and the end of text.
     * This is the only method using Jsoup package.
     *
     * @param htmlString    a HTML string to remove tags from
     * @return              text extracted from HTML string without tags and unnecessary whitespaces
     */
    public static String cleanseTextFromHtmlTags(String htmlString) {
        htmlString = htmlString.replaceAll("<br>", "");
        Document doc = Jsoup.parse(htmlString);
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
        doc.select("p").prepend("\\n");
        String s = doc.html().replaceAll("\\\\n", "\n");
        return Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false))
                .replaceAll("^\\s*|\\s*$", "")
                .replaceAll("&nbsp;", "");
    }

    /**
     * Converts Cookies to a single string value, so it can be used to send with web requests.
     * Requires htmlunit package.
     *
     * @param cookies   a set of cookies to convert to string
     * @return          a string of chained cookie name and values with semi colons separating them
     */
    public static String cookiesAsRequestHeader(Set<Cookie> cookies) {
        Iterator<Cookie> it = cookies.iterator();
        StringBuilder sb = new StringBuilder();

        while (it.hasNext()) {
            Cookie c = it.next();
            sb.append(c.getName()).append("=").append(c.getValue()).append("; ");
        }
        sb.setLength(sb.length() - 1);

        return sb.toString();
    }
}
