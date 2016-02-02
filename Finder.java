import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


public class Finder {
	public static void main(String[] args) throws IOException {
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
			for (Element currClass: classes) { //for each class that the player played
				System.out.println("\t" + currClass.className());
				String timePlayedTableStr = currClass.attr("data-content");

				Document tableDoc = Jsoup.parse(timePlayedTableStr);
				Element timePlayedTable = tableDoc.select("table").first();
				String timePlayedStr = timePlayedTable.select("tbody").first().select("tr").first().select("td").first().text();
				System.out.println("\t\t" + timePlayedStr);

			}


        }
	}
}