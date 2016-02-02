import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;
import javafx.application.Application;
import javafx.scene.text.Text;

public class Finder extends Application {
	private static final List<String> OFFCLASSES = new ArrayList<>(); //list of classes which are considered OFFCLASSES
	private static final double MAX_OFFCLASS_PERCENT = 40.;
	private static String displayedText;
	private static final int TIMEOUT = 10 * 1000;
	private static final int NUM_PAGES = 15; //number of pages on logs.tf to explore for logs

	public static void main(String[] args) {
		OFFCLASSES.add("pyro");
		OFFCLASSES.add("heavy");
		OFFCLASSES.add("sniper");
		OFFCLASSES.add("spy");
		OFFCLASSES.add("engineer");

		String baseURL = "http://logs.tf/?p=";
		for (int i = 1; i <= NUM_PAGES; i++) {
			String logsListURL = baseURL + String.valueOf(i);

			try {
				Document mainPage = Jsoup.connect(logsListURL).timeout(TIMEOUT).get();
				Element logsTableBody = mainPage.select("tbody").first();
				for (Element row: logsTableBody.select("tr")) {
					Element logLink = row.select("a").first();
					String logName = logLink.text();
					String urlEnding = logLink.attr("href");
					String url = "http://logs.tf" + urlEnding;
					String gameFormat = row.select("td").get(2).text();
					if (logName.contains("TF2Center") && gameFormat.equals("6v6")) {
						if (analyzeLog(url).length() > 0) {
							displayedText += url + "\n\n";
							displayedText += analyzeLog(url) + "---------------------------------------------------------\n";
						}
						
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		launch(args);
	}

	@Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello Brightly!");
        
        ScrollPane root = new ScrollPane();
        Text textNode = new Text(displayedText);
        root.setContent(textNode);
        primaryStage.setScene(new Scene(root, 700, 700));
        primaryStage.show();
    }


    /**
     * returns empty string if no offclassers, else returns information about the offclassers
     */
	private static String analyzeLog(String url) throws IOException {
		String toReturn = "";
		Document doc = Jsoup.connect(url).get();
		Element playerTable = doc.select("table[id=\"players\"]").get(0).select("tbody").first();
		//System.out.println(doc.select("i[class^=\"classicon\"]").size());
		for (Element row: playerTable.select("tr")) { //1 row per player
			Elements tds = row.select("td");

			Element nameCell = tds.get(1);
			Element nameLink = nameCell.select("a[class=\"dropdown-toggle\"]").first();
			

			Element classesCell = tds.get(2);
			Elements classes = classesCell.select("i[class^=\"classicon\"");

			int totalSecs = 0;
			int totalOffclassSecs = 0;
			for (Element currClass: classes) { //for each class that the player played
				String timePlayedTableStr = currClass.attr("data-content");

				Document tableDoc = Jsoup.parse(timePlayedTableStr);
				Element timePlayedTable = tableDoc.select("table").first();
				String timePlayedStr = timePlayedTable.select("tbody").first().select("tr").first().select("td").first().text();
				String[] minsAndSecs = timePlayedStr.split(":");
				int timePlayed = Integer.parseInt(minsAndSecs[0]) * 60 + Integer.parseInt(minsAndSecs[1]); //time played by this player on this class in seconds
				totalSecs += timePlayed;

				for (String offclass: OFFCLASSES) {
					if (currClass.className().contains(offclass)) { //the player has played this offclass
						totalOffclassSecs += timePlayed;
					}
				}
			}

			double offclassPercent = Math.floor((double)totalOffclassSecs / totalSecs * 100);

			

			if (offclassPercent > MAX_OFFCLASS_PERCENT) {
				toReturn += "name: " + nameLink.text();
				toReturn += "\n\t offclass percentage: " + offclassPercent + "%";
				Element linksList = nameCell.select("ul[class=\"dropdown-menu\"]").first();
				String tf2centerProfile = linksList.select("li").get(5).select("a").first().attr("href");
				toReturn += "\n\t" + tf2centerProfile;
				toReturn += "\n";
			}
        }

        return toReturn;
	}
}