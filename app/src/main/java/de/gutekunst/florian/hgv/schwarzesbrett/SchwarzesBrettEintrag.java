package de.gutekunst.florian.hgv.schwarzesbrett;

public class SchwarzesBrettEintrag {

    private String date;
    private String headline;
    private String message;
    private String anhang = "";
    private String anhangLink = "";

    public SchwarzesBrettEintrag(String date, String headline, String message) {
        this.date = date;
        this.headline = headline;
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public String getHeadline() {
        return headline;
    }

    public String getMessage() {
        return message;
    }

    public String getAnhang() {
        return anhang;
    }

    public void setAnhang(String anhang) {
        this.anhang = anhang;
    }

    public String getAnhangLink() {
        return anhangLink;
    }

    public void setAnhangLink(String anhangLink) {
        this.anhangLink = anhangLink;
    }

    @Override
    public String toString() {
        return "SchwarzesBrettEintrag{" +
                "date='" + date + '\'' +
                ", headline='" + headline + '\'' +
                ", message='" + message + '\'' +
                ", anhang='" + anhang + '\'' +
                ", anhangLink='" + anhangLink + '\'' +
                '}';
    }
}
