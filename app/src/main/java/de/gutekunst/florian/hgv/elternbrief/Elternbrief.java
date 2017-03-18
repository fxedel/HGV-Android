package de.gutekunst.florian.hgv.elternbrief;

public class Elternbrief {

    private String name;
    private String date;
    private String message;
    private String link;

    public Elternbrief(String name, String date, String message, String link) {
        this.name = name;
        this.date = date;
        this.message = message;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public String getLink() {
        return link;
    }
}
