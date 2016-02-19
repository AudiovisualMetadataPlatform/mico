package eu.mico.platform.event.impl;

import com.github.anno4j.Anno4j;
import eu.mico.platform.event.api.AnalysisService;

public abstract class AnalysisServiceAnno4j implements AnalysisService {

    private Anno4j anno4j;

    public Anno4j getAnno4j() {
        return anno4j;
    }

    public void setAnno4j(Anno4j anno4j) {
        this.anno4j = anno4j;
    }
}
