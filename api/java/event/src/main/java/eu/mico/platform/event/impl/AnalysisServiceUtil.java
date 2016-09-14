package eu.mico.platform.event.impl;

import eu.mico.platform.event.api.AnalysisServiceBase;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import eu.mico.platform.event.api.AnalysisService;

public class AnalysisServiceUtil {

    private AnalysisServiceUtil(){
        // this class has no instance
    }

    public static URI getServiceID(AnalysisServiceBase service) {
        return new URIImpl("http://www.mico-project.org/services/"
                + getQueueName(service));
    }

    public static  String getQueueName(AnalysisServiceBase service) {
        return service.getExtractorID() 
                + "-" + service.getExtractorVersion()
                + "-" + service.getExtractorModeID();
    }
}
