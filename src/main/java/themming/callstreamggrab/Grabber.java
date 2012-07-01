/**
 * @author Tim Hemming <timhemming@gmail.com>
 */
package themming.callstreamggrab;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Grabber {
    private static Logger logger = Logger.getLogger(Grabber.class);

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println("Usage: Grabber.jar url clientId username password");
            System.exit(1);
        }

        String url = args[0];
        String clientId = args[1];
        String username = args[2];
        String password = args[3];

        final WebClient webClient = new WebClient();

        logger.info("Connecting to " + url);
        final HtmlPage page = webClient.getPage(url);

        logger.info("Logging in as clientId: " + clientId + ", username: " + username);
        final HtmlForm form = page.getFormByName("aspnetForm");
        form.getInputByName("ctl00$pageContent$txtClientID").setValueAttribute(clientId);
        form.getInputByName("ctl00$pageContent$txtUsername").setValueAttribute(username);
        form.getInputByName("ctl00$pageContent$txtPassword").setValueAttribute(password);
        final HtmlPage page2 = form.getInputByName("ctl00$pageContent$btnLogin").click();

        logger.info("Locating download page");
        final HtmlPage page3 = page2.getAnchorByText("CS Insurance Choice").click();
        final HtmlPage page4 = page3.getAnchorByText("Destinations").click();

        logger.info("Proceeding with download");
        final TextPage page5 = page4.getElementById("ctl00_pageContent_ImageButton1").click();
        final WebResponse response = page5.getWebResponse();
        if (response.getStatusCode() == 200 && response.getContentType().equals("text/plain")) {
            BufferedReader in = new BufferedReader(new InputStreamReader(response.getContentAsStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
            in.close();
        } else {
            throw new Exception(String.format("Could not download csv file. Status code: %d, Status message: %s, " +
                    "Content Type: %s",
                    response.getStatusCode(), response.getStatusMessage(), response.getContentType()));
        }

        logger.info("Download complete");
        webClient.closeAllWindows();

        logger.info("Exiting");
    }
}
