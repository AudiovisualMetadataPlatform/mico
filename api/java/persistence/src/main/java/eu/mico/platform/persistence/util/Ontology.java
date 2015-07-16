package eu.mico.platform.persistence.util;

import com.github.anno4j.model.ontologies.DCTERMS;

public class Ontology {

    /**
     * MICO (mico:)
     */
    
    public final static String NS_MICO = "http://www.mico-project.eu/ns/platform/1.0/schema#";

    public final static String BINARY_BODY_MICO = NS_MICO + "BinaryBody";
    
    public final static String HAS_BODY_MICO = NS_MICO + "hasBody";

    public final static String HAS_CONFIDENCE_MICO = NS_MICO + "hasConfidence";

    public final static String HAS_CONTENT_MICO = NS_MICO + "hasContent";

    public final static String HAS_CONTENT_PART_MICO = NS_MICO + "hasContentPart";

    public final static String HAS_ID_MICO = NS_MICO + "hasId";

    public final static String HAS_LOCATION_MICO = NS_MICO + "hasLocation";

    public final static String HAS_QUEUE_NAME = NS_MICO + "hasQueueName";

    public final static String HAS_REDEFINED_TYPE_MICO = NS_MICO + "hasRefinedType";

    public final static String HAS_VERSION_TYPE_MICO = NS_MICO + "hasVersion";

    public final static String PROVIDES_MICO = NS_MICO + "provides";

    public final static String REQUIRES_MICO = NS_MICO + "requires";
    
    public final static String COLORLAYOUT_BODY_MICO = NS_MICO + "ColorLayoutBody";
    
    public final static String CONTENT_PART_MICO = NS_MICO + "ContentPart";

    public final static String CONTENT_ITEM_MICO = NS_MICO + "ContentItem";

    public final static String FACE_RECOGNITION_BODY_MICO = NS_MICO + "FaceRecognitionBody";
    
    public final static String ANNOTATION_BODY_MICO = NS_MICO + "AnnotatinBody";
    
    public final static String MULTIMEDIA_BODY_MICO = NS_MICO + "MultimediaBody";
    
    public final static String LOW_LEVEL_FEATURE_BODY_MICO = NS_MICO + "LowLevelFeatureBody";
    
    public final static String DETECTION_BODY_MICO = NS_MICO + "DetectionBody";
    
    public final static String FACE_DETECTION_BODY_MICO = NS_MICO + "FaceRecognitionBody";

    public final static String EYE_DETECTION_BODY_MICO = NS_MICO + "EyeDetectionBody";

    public final static String NOSE_DETECTION_BODY_MICO = NS_MICO + "NoseDetectionBody";

    public final static String MOUTH_DETECTION_BODY = NS_MICO + "MouthDetectionBody";

    public final static String ANIMAL_DETECTION_BODY_MICO = NS_MICO + "AnimalDetectionBody";

    public final static String RIGHT_EYE_DETECTION_BODY_MICO = NS_MICO + "RightEyeDetectionBody";

    public final static String LEFT_EYE_DETECTION_BODY_MICO = NS_MICO + "LeftEyeDetectionBody";

    public final static String MOUTH_CENTER_DETECTION_BODY_MICO = NS_MICO + "MouthCenterDetectionBody";

    public final static String AVQ_BODY_MICO = NS_MICO + "AVQBody";

    public final static String AVQ_SHOT_BODY_MICO = NS_MICO + "AVQShotBody";

    public final static String AVQ_KEY_FRAME_BODY_MICO = NS_MICO + "AVQKeyFrameBody";

    public final static String TVS_BODY_MICO = NS_MICO + "TVSBody";

    public final static String TVS_KEY_FRAME_BODY_MICO = NS_MICO + "TVSKeyFrameBody";

    public final static String TVS_SHOT_BODY_MICO = NS_MICO + "TVSShotBody";

    public final static String VSI_BODY_MICO = NS_MICO + "VSIBody";

    public final static String SMD_BODY_MICO = NS_MICO + "SMDBody";

    public final static String ACD_BODY_MICO = NS_MICO + "ACDBody";

    public final static String MEDIA_CONTAINER_TAG_BODY_MICO = NS_MICO + "MediaContainerTagBody";
    
    public final static String NER_BODY_MICO = NS_MICO + "NERBody";

    public final static String STT_BODY_MICO = NS_MICO + "STTBody";

    public final static String PSP_BODY_MICO = NS_MICO + "PSPBody";

    public final static String ASR_BODY_MICO = NS_MICO + "ASRDBody";

    public final static String SENTIMENT_ANALYSIS_BODY_MICO = NS_MICO + "SentimentAnalysisBody";

    public final static String CHATROOM_CLEANER_BODY_MICO = NS_MICO + "ChatRoomCleanerBody";

    public final static String IWG_BODY_MICO = NS_MICO + "IWGBody";

    public final static String TEXTUAL_FEATURE_BODY_MICO = NS_MICO + "TextualFeatureBody";

    public final static String QUESTION_DETECTION_BODY_MICO = NS_MICO + "QuestionDetectionBody";
    
    public final static String YDCCOEFF_MICO = NS_MICO + "YDCCoeff";

    public final static String CBACCOEFF_MICO = NS_MICO + "CbACCoeff2";

    public final static String CBDCCOEFF_MICO = NS_MICO + "CbDCCoeff";

    public final static String CRACCOEFF_MICO = NS_MICO + "CrACCoeff2";

    public final static String CRDCCOEFF_MICO = NS_MICO + "CrDCCoeff";

    public final static String YACCOEFF_MICO = NS_MICO + "YACCoeff5";

    public final static String COLOR_LAYOUT_RAW_BODY = NS_MICO + "CLraw";

    /**
     * Open Annotation (oa:)
     */

    public final static String NS_OA = "http://www.w3.org/ns/oa#";

    public final static String ANNOTATION_OA = NS_OA + "annotation";

    public final static String SPECIFIC_RESOURCE_OA = NS_OA + "SpecificResource";

    public final static String FRAGMENT_SELECTOR_OA = NS_OA + "FragmentSelector";

    public final static String HAS_BODY_OA = NS_OA + "hasBody";

    public final static String HAS_TARGET_OA = NS_OA + "hasTarget";

    public final static String ANNOTATED_BY_OA = NS_OA + "annotatedBy";

    public final static String ANNOTATED_AT_OA = NS_OA + "annotatedAt";

    public final static String SERIALIZED_BY_OA = NS_OA + "serializedBy";

    public final static String SERIALIZED_AT_OA = NS_OA + "serializedAt";

    public final static String HAS_SELECTOR_OA = NS_OA + "hasSelector";

    public final static String HAS_SOURCE_OA = NS_OA + "hasSource";

    public final static String SVG_SELECTOR_OA = NS_OA + "SVGSelector";


    /**
     * Dublin Core Terms (dcterms:)
     */

    public final static String FORMAT_DCTERMS = DCTERMS.NS + "format";

}

