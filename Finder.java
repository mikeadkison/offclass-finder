import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.LinkedHashSet;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;
import javafx.application.Application;
import javafx.scene.text.Text;

import javafx.scene.layout.VBox;
import javafx.scene.control.Hyperlink;

import javafx.application.HostServices;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;

import javafx.scene.control.TextArea;


public class Finder extends Application {
	private static final List<String> OFFCLASSES = new ArrayList<>(); //list of classes which are considered OFFCLASSES
	private static final double MAX_OFFCLASS_PERCENT = 40.;
	private static final int TIMEOUT = 20 * 1000;
	private static final int NUM_PAGES = 15; //number of pages on logs.tf to explore for logsddddd
	private List<PlayerReport> reports;
	

	public static void main(String[] args) throws IOException {
		Finder finder = new Finder();
		launch(args);
	}

	public Finder() throws IOException {
		OFFCLASSES.add("pyro");
		OFFCLASSES.add("heavy");
		OFFCLASSES.add("sniper");
		OFFCLASSES.add("spy");
		OFFCLASSES.add("engineer");
		reports = new ArrayList<>();
		List<String> visited = new ArrayList<>();
		boolean visitedBool = false;
		int j = 0;
		String baseURL = "http://logs.tf/?p=";
		for (int i = 1; i <= NUM_PAGES; i++) {
			String logsListURL = baseURL + String.valueOf(i);

			//try {
				Document mainPage = Jsoup.connect(logsListURL).timeout(TIMEOUT).get();
				Element logsTableBody = mainPage.select("tbody").first();
				
				for (Element row: logsTableBody.select("tr")) {
					//System.out.println(j);
					j++;
					Element logLink = row.select("a").first();
					String logName = logLink.text();
					String urlEnding = logLink.attr("href");
					String url = "http://logs.tf" + urlEnding;
					String gameFormat = row.select("td").get(2).text();

					if (logName.contains("TF2Center") && gameFormat.equals("6v6") && !visited.contains(url)) {
						if (url.equals("http://logs.tf/1241016")) {
							System.out.println("http://logs.tf/1241016 IS BEING ANALYZED");
							System.out.println("VISITED HAS 1241016: " + visited.contains(url));
							System.out.println("visited bool: " + visitedBool);
							visitedBool = true;
						}
						visited.add(url);
						
						if (url.equals("http://logs.tf/1241016")) {
							System.out.println("VISITED Now HAS 1241016: " + visited.contains(url));
						}
						List<PlayerReport> reports = analyzeLog(url);
						this.reports.addAll(reports);
					}
					
				}
				System.out.println("JAY: " + j);
			//} catch (IOException e) {
				//System.out.println("EXCEPTIONI!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				//e.printStackTrace();
			//}
		}

	}

	@Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello Brightly!");
        
        ScrollPane root = new ScrollPane();
        VBox vbox = new VBox();
        addReportsToVBox(vbox);
        root.setContent(vbox);
        primaryStage.setScene(new Scene(root, 700, 700));
        primaryStage.show();
    }

    private void addReportsToVBox(VBox vbox) {
    	for (PlayerReport report: reports) {
    		TextArea infoTextArea= new TextArea(report.toString());
    		infoTextArea.setPrefRowCount(infoTextArea.getText().split("\\n").length);
    		infoTextArea.setEditable(false);
    		vbox.getChildren().add(infoTextArea);

    		Hyperlink tf2centerProfileLink = new Hyperlink(report.tf2centerProfile);
    		tf2centerProfileLink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    getHostServices().showDocument(report.tf2centerProfile);
                }
            });
    		vbox.getChildren().add(tf2centerProfileLink);

    		Hyperlink lobbyLink = new Hyperlink(report.lobbyURL);
    		lobbyLink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    getHostServices().showDocument(report.lobbyURL);
                }
            });
            vbox.getChildren().add(lobbyLink);
    	}
    }


    /**
     * returns empty string if no offclassers, else returns information about the offclassers
     */
	private List<PlayerReport> analyzeLog(String url) throws IOException {
		//System.out.println("analyzing: " + url);
		List<PlayerReport> reports = new ArrayList<>();

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

			String[] minsAndSecs = null;
			for (Element currClass: classes) { //for each class that the player played
				
				String timePlayedTableStr = currClass.attr("data-content");

				Document tableDoc = Jsoup.parse(timePlayedTableStr);
				Element timePlayedTable = tableDoc.select("table").first();
				String timePlayedStr = timePlayedTable.select("tbody").first().select("tr").first().select("td").first().text();
				
				minsAndSecs = timePlayedStr.split(":");
				int timePlayed = Integer.parseInt(minsAndSecs[0]) * 60 + Integer.parseInt(minsAndSecs[1]); //time played by this player on this class in seconds
				totalSecs += timePlayed;


				int i = 0;
				for (String offclass: OFFCLASSES) {

					if (currClass.className().contains(offclass)) { //the player has played this offclass
						totalOffclassSecs += timePlayed;
						i += 1;
						
					}
				}
			}

			double offclassPercent = Math.floor(((double)totalOffclassSecs) / totalSecs * 100);

			

			if (offclassPercent > MAX_OFFCLASS_PERCENT) {

				Element linksList = nameCell.select("ul[class=\"dropdown-menu\"]").first();
				String tf2centerProfile = linksList.select("li").get(5).select("a").first().attr("href");

				PlayerReport report = new PlayerReport(nameLink.text(), tf2centerProfile, offclassPercent, url);
				reports.add(report);

			}
        }

        return reports;
	}

	private class PlayerReport {
		String name;
		String tf2centerProfile;
		double offclassPercent;
		String lobbyURL;

		public PlayerReport(String name, String tf2centerProfile, double offclassPercent, String lobbyURL) {
			this.name = name;
			this.tf2centerProfile = tf2centerProfile;
			this.offclassPercent = offclassPercent;
			this.lobbyURL = lobbyURL;
		}

		@Override
		public String toString() {
			return "name: " + this.name + "\n"
					+ "offclass percent: " + (int) offclassPercent + "%";
		}
	}
}