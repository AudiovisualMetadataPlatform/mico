package eu.mico.platform.anno4j.model.impl.bodymmm;

import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import org.openrdf.annotations.Iri;

@Iri(MMMTERMS.COLORLAYOUT_BODY)
public interface ColorLayoutBodyMMM extends BodyMMM {

    @Iri(MMMTERMS.YDCCOEFF)
    String getYDC();

    @Iri(MMMTERMS.YDCCOEFF)
    void setYDC(String YDC);

    @Iri(MMMTERMS.CBDCCOEFF)
    String getCbDC();

    @Iri(MMMTERMS.CBDCCOEFF)
    void setCbDC(String cbDC);

    @Iri(MMMTERMS.CRDCCOEFF)
    String getCrDC();

    @Iri(MMMTERMS.CRDCCOEFF)
    void setCrDC(String crDC);

    @Iri(MMMTERMS.CBACCOEFF)
    String getCbAC();

    @Iri(MMMTERMS.CBACCOEFF)
    void setCbAC(String cbAC);

    @Iri(MMMTERMS.CRACCOEFF)
    String getCrAC();

    @Iri(MMMTERMS.CRACCOEFF)
    void setCrAC(String crAC);

    @Iri(MMMTERMS.YACCOEFF)
    String getYAC();

    @Iri(MMMTERMS.YACCOEFF)
    void setYAC(String YAC);
}
