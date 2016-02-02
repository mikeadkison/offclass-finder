import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Finder {
	private static final List<String> offclasses = new ArrayList<>(); //list of classes which are considered offclasses

	public static void main(String[] args) throws IOException {
		offclasses.add("pyro");
		offclasses.add("heavy");
		offclasses.add("sniper");
		offclasses.add("spy");
		offclasses.add("engineer");

		Document doc = Jsoup.connect("http://logs.tf/1240238").get();
		Element playerTable = doc.select("table[id=\"players\"]").get(0).select("tbody").first();
		//System.out.println(doc.select("i[class^=\"classicon\"]").size());
		for (Element row: playerTable.select("tr")) { //1 row per player
			Elements tds = row.select("td");

			Element nameCell = tds.get(1);
			Element nameLink = nameCell.select("a[class=\"dropdown-toggle\"]").first();
			System.out.println("name: " + nameLink.text());

			Element classesCell = tds.get(2);
			Elements classes = classesCell.select("i[class^=\"classicon\"");

			int totalSecs = 0;
			int totalOffclassSecs = 0;
			for (Element currClass: classes) { //for each class that the player played
				System.out.println("\t" + currClass.className());
				String timePlayedTableStr = currClass.attr("data-content");

				Document tableDoc = Jsoup.parse(timePlayedTableStr);
				Element timePlayedTable = tableDoc.select("table").first();
				String timePlayedStr = timePlayedTable.select("tbody").first().select("tr").first().select("td").first().text();
				System.out.println("\t\t" + timePlayedStr);
				String[] minsAndSecs = timePlayedStr.split(":");
				int timePlayed = Integer.parseInt(minsAndSecs[0]) * 60 + Integer.parseInt(minsAndSecs[1]); //time played by this player on this class in seconds
				totalSecs += timePlayed;

				for (String offclass: offclasses) {
					if (currClass.className().contains(offclass)) { //the player has played this offclass
						totalOffclassSecs += timePlayed;
					}
				}
			}

			System.out.println("\t offclass percentage: " + Math.floor((double)totalOffclassSecs / totalSecs * 100) + "%");
        }
	}
}