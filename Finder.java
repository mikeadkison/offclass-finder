import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


public class Finder {
	public static void main(String[] args) throws IOException {
		Document doc = Jsoup.connect("http://logs.tf/1240238").get();
		Element playerTable = doc.select("table[id=\"players\"]").get(0).select("tbody").get(0);
		//System.out.println(doc.select("i[class^=\"classicon\"]").size());
		for (Element row: playerTable.select("tr")) { //1 row per player
			Elements tds = row.select("td");

			Element nameCell = tds.get(1);
			Element nameLink = nameCell.select("a[class=\"dropdown-toggle\"]").get(0);
			System.out.println("name: " + nameLink.text());

			Element classesCell = tds.get(2);
			Elements classes = classesCell.select("i[class^=\"classicon\"");

			for (Element currClass: classes) {
				System.out.println("\t" + currClass.className());
			}
        }
	}
}