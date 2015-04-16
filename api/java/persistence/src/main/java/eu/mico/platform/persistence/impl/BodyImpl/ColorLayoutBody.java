package eu.mico.platform.persistence.impl.BodyImpl;

import eu.mico.platform.persistence.impl.ModelPersistenceImpl.ModelPersistenceBodyImpl;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

@Iri(Ontology.COLORLAYOUT_BODY_MICO)
public class ColorLayoutBody extends ModelPersistenceBodyImpl {

    @Iri(Ontology.YDCCOEFF_MICO)
    private String YDC;

    @Iri(Ontology.CBDCCOEFF_MICO)
    private String CbDC;

    @Iri(Ontology.CRDCCOEFF_MICO)
    private String CrDC;

    @Iri(Ontology.CBACCOEFF_MICO)
    private String CbAC;

    @Iri(Ontology.CRACCOEFF_MICO)
    private String CrAC;

    @Iri(Ontology.YACCOEFF_MICO)
    private String YAC;

    public ColorLayoutBody() {
    }

    public ColorLayoutBody(String YDC, String CbDC, String CrDC, String CbAC, String CrAC, String YAC) {
        this.YDC = YDC;
        this.CbDC = CbDC;
        this.CrDC = CrDC;
        this.CbAC = CbAC;
        this.CrAC = CrAC;
        this.YAC = YAC;
    }

    public String getYDC() {
        return YDC;
    }

    public void setYDC(String YDC) {
        this.YDC = YDC;
    }

    public String getCbDC() {
        return CbDC;
    }

    public void setCbDC(String cbDC) {
        CbDC = cbDC;
    }

    public String getCrDC() {
        return CrDC;
    }

    public void setCrDC(String crDC) {
        CrDC = crDC;
    }

    public String getCbAC() {
        return CbAC;
    }

    public void setCbAC(String cbAC) {
        CbAC = cbAC;
    }

    public String getCrAC() {
        return CrAC;
    }

    public void setCrAC(String crAC) {
        CrAC = crAC;
    }

    public String getYAC() {
        return YAC;
    }

    public void setYAC(String YAC) {
        this.YAC = YAC;
    }
}
