package eu.mico.platform.zooniverse.model;

import java.util.List;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class TextAnalysisOutput {

    public String id;
    public String status = "finished";

    public Object sentiment;
    public List topics;
    public List entities;

    public TextAnalysisOutput(String id) {
        this.id = id;
    }
}
