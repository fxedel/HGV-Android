package de.gutekunst.florian.hgv.schwarzesbrett;

import java.util.*;

public class SchwarzesBrett {

    private ArrayList<SchwarzesBrettEintrag> eintraege = new ArrayList<>();

    public void addEintrag(SchwarzesBrettEintrag eintrag) {
        eintraege.add(eintrag);
    }

    public ArrayList<SchwarzesBrettEintrag> getEintraege() {
        return eintraege;
    }
}
