package eu.mico.platform.event.impl;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import eu.mico.platform.event.api.AnalysisService;

public class AnalysisServiceUtil {


    public static URI getServiceID(AnalysisService service) {
        return new URIImpl("http://www.mico-project.org/services/"
                + getQueueName(service));
    }

    public static  String getQueueName(AnalysisService service) {
        return service.getExtractorID() 
                + "-" + service.getExtractorVersion()
                + "-" + service.getExtractorModeID();
    }
}
