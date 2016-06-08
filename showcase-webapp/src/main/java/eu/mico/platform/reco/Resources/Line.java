package eu.mico.platform.reco.Resources;

import org.openrdf.repository.object.LangString;

import java.text.MessageFormat;

public class Line {
    LangString langstring;
    String timecode;


    public Line(String timecode, LangString langstring) {
        this.langstring = langstring;
        this.timecode = timecode;
    }


    @Override
    public String toString() {
        return MessageFormat.format("[{0}] - {1}@{2}", timecode, langstring.toString(), langstring.getLang());
    }
}
