package eu.mico.platform.uc.zooniverse.model;

import eu.mico.platform.persistence.model.ContentItem;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class TextAnalysisOutput {

    public String id;
    public final String status = "finished";

    public TextAnalysisOutput(ContentItem ci) {
        this.id = ci.getID();
    }



}
