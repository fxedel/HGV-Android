package de.gutekunst.florian.hgv.internet;

import android.content.*;
import android.net.*;
import android.os.*;
import android.util.*;

import org.apache.commons.io.*;
import org.jsoup.Connection.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.io.*;
import java.util.*;

import cz.msebera.android.httpclient.*;
import cz.msebera.android.httpclient.client.*;
import cz.msebera.android.httpclient.client.entity.*;
import cz.msebera.android.httpclient.client.methods.*;
import cz.msebera.android.httpclient.cookie.*;
import cz.msebera.android.httpclient.impl.client.*;
import cz.msebera.android.httpclient.impl.cookie.*;
import cz.msebera.android.httpclient.message.*;
import de.gutekunst.florian.hgv.elternbrief.*;
import de.gutekunst.florian.hgv.schwarzesbrett.*;

public class InternetManager {

    public String phpsessid = "";
    private BasicCookieStore bc;
    private HttpClient hc;
    private String contentVertretungsplan = "";

    //"service/vertretungsplan" when "hugyvat.eltern-portal.org/service/vertretungsplan" should be downloaded
    private String downloadUrl(String url) {
        String content = "";

        if (hc == null) {
            bc = new BasicCookieStore();
            BasicClientCookie cookie = new BasicClientCookie("PHPSESSID", phpsessid);
            cookie.setDomain("eltern-portal.org");
            cookie.setPath("/");
            cookie.setSecure(true);
            cookie.setAttribute(BasicClientCookie.PATH_ATTR, "/");
            cookie.setAttribute(BasicClientCookie.DOMAIN_ATTR, ".eltern-portal.org");
            cookie.setAttribute(BasicClientCookie.SECURE_ATTR, null);
            bc.addCookie(cookie);

            hc = HttpClients.custom().setDefaultCookieStore(bc).build();
        }

        try {
            //Request Vertretungsplan page
            HttpResponse r = hc.execute(new HttpGet("https://hugyvat.eltern-portal.org/" + url));

            content = IOUtils.toString(r.getEntity().getContent(), "UTF-8");
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return content;
    }

    public boolean login(String username, String password) throws DownloadFailedException {
        bc = new BasicCookieStore();
        hc = HttpClients.custom().setDefaultCookieStore(bc).build();
        List<NameValuePair> nvp = new ArrayList<>();
        nvp.add(new BasicNameValuePair("username", username));
        nvp.add(new BasicNameValuePair("password", password));

        String content = "";

        try {
            hc.execute(new HttpGet("https://hugyvat.eltern-portal.org"));

            HttpPost post = new HttpPost("https://eltern-portal.org/includes/project/auth/login.php");
            post.setEntity(new UrlEncodedFormEntity(nvp));
            hc.execute(post);

            HttpResponse r = hc.execute(new HttpGet("https://hugyvat.eltern-portal.org/service/vertretungsplan"));

            content = IOUtils.toString(r.getEntity().getContent(), "UTF-8");

            for (Cookie cookie : bc.getCookies()) {
                if (cookie.getName().equals("PHPSESSID")) {
                    phpsessid = cookie.getValue();

                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (content.length() == 0) {
            throw new DownloadFailedException(DownloadFailedException.ERR_ADRESS_UNREACHABLE);
        }

        Document doc = Jsoup.parse(content);

        return doc.select("div.list, div.bold, div.full_width, div.text_center").size() != 0;
    }

    public String getVertretungsplanToday() throws DownloadFailedException {
        if (contentVertretungsplan.length() == 0) {
            contentVertretungsplan = downloadUrl("service/vertretungsplan");
        }

        String vertretungsplan = "";
        Document doc = Jsoup.parse(contentVertretungsplan);

        if (doc.select("div.list, div.bold, div.full_width, div.text_center").size() == 0) {
            if (contentVertretungsplan.length() == 0) {
                throw new DownloadFailedException(DownloadFailedException.ERR_ADRESS_UNREACHABLE);
            } else {
                throw new DownloadFailedException(DownloadFailedException.ERR_WRONG_USERNAME);
            }
        } else {
            Element divMain = doc.select("div.main_center").first();

            Element date, table;
            try {
                date = divMain.select("div.list, div.bold, div.full_width, div.text_center").first();
                table = divMain.select("table.table").first();
            } catch (Exception e) {
                throw new DownloadFailedException(DownloadFailedException.ERR_UNKNOWN);
            }

            vertretungsplan = date.outerHtml() + table.outerHtml();
        }

        vertretungsplan = "<head><link href=\"https://eltern-portal.org/includes/project/css/concat_7.css\"  rel=\"stylesheet\"></head><body style=\"background: #ffffff\">" + vertretungsplan + "</body>";

        return vertretungsplan;
    }

    public String getVertretungsplanTomorrow() throws DownloadFailedException {
        if (contentVertretungsplan.length() == 0) {
            contentVertretungsplan = downloadUrl("service/vertretungsplan");
        }

        String vertretungsplan = "";
        Document doc = Jsoup.parse(contentVertretungsplan);
        if (doc.select("div.list, div.bold, div.full_width, div.text_center").size() == 0) {
            if (contentVertretungsplan.length() == 0) {
                throw new DownloadFailedException(DownloadFailedException.ERR_ADRESS_UNREACHABLE);
            } else {
                throw new DownloadFailedException(DownloadFailedException.ERR_WRONG_USERNAME);
            }
        } else {
            Element divMain = doc.select("div.main_center").first();
            Elements children = divMain.getAllElements().get(1).siblingElements();

            Element date, table;
            try {
                date = divMain.select("div.list, div.bold, div.full_width, div.text_center").get(1);
                table = divMain.select("table.table").get(1);
            } catch (Exception e) {
                throw new DownloadFailedException(DownloadFailedException.ERR_UNKNOWN);
            }

            vertretungsplan = date.outerHtml() + table.outerHtml();
        }

        vertretungsplan = "<head><link href=\"https://eltern-portal.org/includes/project/css/concat_7.css\"  rel=\"stylesheet\"></head><body style=\"background: #ffffff\">" + vertretungsplan + "</body>";

        return vertretungsplan;
    }

    public String getStand() throws DownloadFailedException {
        if (contentVertretungsplan.length() == 0) {
            contentVertretungsplan = downloadUrl("service/vertretungsplan");
        }

        String stand = "";
        Document doc = Jsoup.parse(contentVertretungsplan);
        if (doc.select("div.list, div.bold, div.full_width, div.text_center").size() == 0) {
            if (contentVertretungsplan.length() == 0) {
                throw new DownloadFailedException(DownloadFailedException.ERR_ADRESS_UNREACHABLE);
            } else {
                throw new DownloadFailedException(DownloadFailedException.ERR_WRONG_USERNAME);
            }
        } else {
            Element divMain = doc.select("div.main_center").first();
            Elements children = divMain.getAllElements().get(1).siblingElements();

            Element standElement = children.select("div.list, div.full_width").get(1);
            Element verbindlichkeitElement = children.select("p.pull-left").first();

            stand = standElement.outerHtml() + verbindlichkeitElement.html();
        }

        stand = "<head><link href=\"https://eltern-portal.org/includes/project/css/concat_7.css\"  rel=\"stylesheet\"></head><body style=\"background: #ffffff\"><center>" + stand + "</center></body>";

        return stand;
    }


    public String getStundenplan() throws DownloadFailedException {
        String contentStundenplan = downloadUrl("service/stundenplan");

        if (contentStundenplan.length() == 0) {
            throw new DownloadFailedException(DownloadFailedException.ERR_ADRESS_UNREACHABLE);
        }

        // Get Table
        Document doc = Jsoup.parse(contentStundenplan);

        Element stundenplanTable = doc.select("table.table, table.table-condensed, table.table-bordered").first();

        if (stundenplanTable == null) {
            throw new DownloadFailedException(DownloadFailedException.ERR_WRONG_USERNAME);
        }

        String stundenplan = "<head><link href=\"https://eltern-portal.org/includes/project/css/concat_7.css\"  rel=\"stylesheet\"></head><body style=\"background: #ffffff\">" + stundenplanTable.toString() + "</body>";

        return stundenplan;
    }

    public String getTermineSchulaufgaben() throws DownloadFailedException {
        String contentTermineSchulaufgaben = downloadUrl("service/termine/liste");

        if (contentTermineSchulaufgaben.length() == 0) {
            throw new DownloadFailedException(DownloadFailedException.ERR_ADRESS_UNREACHABLE);
        }

        // Get Table
        Document doc = Jsoup.parse(contentTermineSchulaufgaben);

        Element termineTable = doc.select("table.table2").first();

        if (termineTable == null) {
            throw new DownloadFailedException(DownloadFailedException.ERR_WRONG_USERNAME);
        }

        String termine = "<head><link href=\"https://eltern-portal.org/includes/project/css/concat_7.css\"  rel=\"stylesheet\"></head><body style=\"background: #ffffff\">" + termineTable.toString() + "</body>";

        return termine;
    }

    public String getTermineAllgemein() throws DownloadFailedException {
        String contentTermineAllgemein = downloadUrl("service/termine/liste/allgemein");

        if (contentTermineAllgemein.length() == 0) {
            throw new DownloadFailedException(DownloadFailedException.ERR_ADRESS_UNREACHABLE);
        }

        // Get Table
        Document doc = Jsoup.parse(contentTermineAllgemein);

        Element termineTable = doc.select("table.table2").first();

        if (termineTable == null) {
            throw new DownloadFailedException(DownloadFailedException.ERR_WRONG_USERNAME);
        }

        String termine = "<head><link href=\"https://eltern-portal.org/includes/project/css/concat_7.css\"  rel=\"stylesheet\"></head><body style=\"background: #ffffff\">" + termineTable.toString() + "</body>";

        return termine;
    }

    public SchwarzesBrett getSchwarzesBrett() throws DownloadFailedException {
        String contentSchwarzesBrett = downloadUrl("aktuelles/schwarzes_brett");

        if (contentSchwarzesBrett.length() == 0) {
            throw new DownloadFailedException(DownloadFailedException.ERR_ADRESS_UNREACHABLE);
        }

        //Get Div
        Document document = Jsoup.parse(contentSchwarzesBrett);

        Element sbDiv = document.select("div.grid").first();

        if (sbDiv == null) {
            throw new DownloadFailedException(DownloadFailedException.ERR_WRONG_USERNAME);
        }

        SchwarzesBrett sb = new SchwarzesBrett();

        for (Element e : sbDiv.select("div.grid-item")) {
            Element div = e.select("div").get(0);
            String date = e.select("p").get(0).html();
            String headline = e.select("h4").get(0).html();
            String message = e.select("p").get(1).html();

            SchwarzesBrettEintrag eintrag = new SchwarzesBrettEintrag(date, headline, message);

            if (e.select("a").size() > 0) {
                Element link = e.select("a").first();
                eintrag.setAnhang(link.attr("title"));
                eintrag.setAnhangLink(link.attr("href"));
            }

            sb.addEintrag(eintrag);
        }

        return sb;
    }

    public ArrayList<Elternbrief> getElternbriefe() throws DownloadFailedException {
        String contentElternbriefe = downloadUrl("aktuelles/elternbriefe");

        if (contentElternbriefe.length() == 0) {
            throw new DownloadFailedException(DownloadFailedException.ERR_ADRESS_UNREACHABLE);
        }

        Document document = Jsoup.parse(contentElternbriefe);
        Elements tables = document.select("table.table2, table.more_padding");
        if (tables.size() == 0) {
            throw new DownloadFailedException(DownloadFailedException.ERR_WRONG_USERNAME);
        }

        Element table = tables.get(0);
        Elements trs = table.select("tr");
        trs.remove(0);

        ArrayList<Elternbrief> elternbriefe = new ArrayList<>();

        for (Element tr : trs) {
            Elements tds = tr.select("td");

            Element linkElement = tds.get(1).select("a").first();
            String link = linkElement.attr("href");
            Element h = linkElement.select("h4").get(0);
            String name = h.text();
            Node datumElement = h.nextSibling();
            String datum = datumElement.toString().substring(1);

            String td = tds.get(1).html();
            String message = td.substring(td.indexOf("</a>") + 4);

            elternbriefe.add(new Elternbrief(name, datum, message, link));
        }

        return elternbriefe;
    }

    public int downloadPdf(String url) {
        int error = 0;

        try {
            Response response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com")
                    .timeout(12000)
                    .execute();

            String[] parts = url.split("/");
            String name = parts[parts.length - 1];
            File dest = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + name);

            FileUtils.writeByteArrayToFile(dest, response.bodyAsBytes());
        } catch (IOException e) {
            error = 1;
            Log.d("HGV PDF", e.toString());
        }

        return error;
    }

    public static boolean checkForIntenet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
