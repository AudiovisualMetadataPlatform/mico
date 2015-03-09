package eu.mico.platform.persistence.impl.BodyImpl;


import eu.mico.platform.persistence.impl.ModelPersistenceImpl;
import eu.mico.platform.persistence.metadata.IBody;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

@Iri(Ontology.FACE_DETECTION_BODY_MICO)
public class FaceDetectionBody extends ModelPersistenceImpl implements IBody {

    public FaceDetectionBody() {
    }
}
