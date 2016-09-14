package eu.mico.platform.reco.Resources;

import org.openrdf.repository.object.LangString;

import java.text.MessageFormat;

public class Line implements Comparable<Line>{
    LangString langstring;
    String timecode;


    public Line(String timecode, LangString langstring) {
        this.langstring = langstring;

        if (timecode == null) {
            timecode = "Unknown";
        }

        this.timecode = timecode;
    }


    @Override
    public String toString() {

        if (langstring == null) {
            langstring = new LangString("Unknown");
        }

        return MessageFormat.format("[{0}] - {1}@{2}", timecode, langstring.toString(), langstring.getLang());
    }

    @Override
    public int compareTo(Line o) {
        //TODO implement!

        // simple string comparison for now:

        return this.timecode.compareTo(o.timecode);
    }
}
