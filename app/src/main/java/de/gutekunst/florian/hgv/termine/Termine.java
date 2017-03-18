package de.gutekunst.florian.hgv.termine;

import android.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.text.*;
import java.util.*;

public class Termine {

    public static String insert(String html, ArrayList<Vector<String>> termine) {
        Document doc = Jsoup.parse(html);

        Log.d("Termine", termine.toString());

        //Liest die Termine aus dem HTML ein
        for (Element tr : doc.select("tr")) {
            Elements tds = tr.select("td");
            if (tds.size() == 3) {
                Vector<String> termin = new Vector<>(2);
                termin.add(tds.first().text().trim());
                termin.add(tds.last().text().trim());

                termine.add(termin);
            }
        }

        //Sortiert die Liste der Termine (aufsteigend nach Datum)
        Collections.sort(termine, new Comparator<Vector<String>>() {
            @Override
            public int compare(Vector<String> t1, Vector<String> t2) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                try {
                    if (sdf.parse(t1.firstElement()).before(sdf.parse(t2.firstElement()))) {
                        return -1;
                    } else if (sdf.parse(t1.firstElement()).after(sdf.parse(t2.firstElement()))) {
                        return 1;
                    } else {
                        return 0;
                    }
                } catch (ParseException e) {
                    return 0;
                }
            }
        });

        //Erstellt das HTML
        String ret = "<table class=\"table2\" width=\"100%\"><tbody>";

        int curMonth = 0;
        int curYear = 0;
        String[] monthNames = new String[] { "Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August",
                "September", "Oktober", "November", "Dezember" };

        for (Vector<String> termin : termine) {
            int month = Integer.parseInt(termin.firstElement().substring(3, 5));
            int year = Integer.parseInt(termin.firstElement().substring(6, 10));

            if (year > curYear) {
                ret += "<tr><td colspan=\"3\" class=\"no_border\"><h4 style=\"margin-top: 10px;\"> " + year
                        + "</h4></td></tr><tr></tr>";

                curYear = year;
                curMonth = 0;
            }

            if (month > curMonth) {
                ret += "<tr><td colspan=\"3\" style=\"vertical-align: bottom; background-color: #dddddd\"><h4>"
                        + monthNames[month - 1] + "</h4></td></tr><tr></tr>";
                curMonth = month;
            }

            ret += "<tr><td class=\"\" width=\"15%\" valign=\"top\">" + termin.firstElement()
                    + "</td><td class=\"\" width=\"15%\" valign=\"top\">&nbsp;</td><td class=\"\" width=\"70%\" valign=\"top\" align=\"left\">"
                    + termin.lastElement() + "</td></tr>";
        }
		ret += "</tbody></table>";

        ret = "<head><link href=\"https://eltern-portal.org/includes/project/css/concat_7.css\"  rel=\"stylesheet\"></head><body style=\"background: #ffffff\">" + ret + "</body>";

        return ret;
    }

}
