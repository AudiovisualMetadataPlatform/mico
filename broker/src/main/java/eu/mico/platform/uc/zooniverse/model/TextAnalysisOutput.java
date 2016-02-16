package eu.mico.platform.uc.zooniverse.model;

import eu.mico.platform.persistence.model.Item;

import java.util.List;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class TextAnalysisOutput {

    public String id;
    public final String status = "finished";

    public Object sentiment;
    public List topics;
    public List entities;

    public TextAnalysisOutput(Item ci) {
        this.id = ci.getID();
    }



}
