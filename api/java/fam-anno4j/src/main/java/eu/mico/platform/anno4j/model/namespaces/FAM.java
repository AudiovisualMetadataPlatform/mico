package eu.mico.platform.anno4j.model.namespaces;

public interface FAM {

    String PREFIX = "fam";
    String NS = "http://vocab.fusepool.info/fam#";
    
    String EXTRACTED_FROM = NS + "extracted-from";
    String CONFIDENCE = NS + "confidence";
    String SELECTOR = NS + "selector";

    String ANNOTATION_BODY = NS + "AnnotationBody";
    String LANGUAGE_ANNOTATION = NS + "LanguageAnnotation";
    String ENTITY_MENTION_ANNOTATION = NS + "EntityMention";
    String LINKED_ENTITY_ANNOTATION = NS + "LinkedEntity";
    String TOPIC_CLASSIFICATION_ANNOTATION = NS + "TopicClassification";
    String TOPIC_ANNOTATION = NS + "TopicAnnotation";
    String SENTIMENT_ANNOTATION = NS + "SentimentAnnotation";

    String ENTITY_LABEL = NS + "entity-label";

    String ENTITY_REFERENCE = NS + "entity-reference";

    String ENTITY_MENTION = NS + "entity-mention";

    String ENTITY_TYPE = NS + "entity-type";

    String CLASSIFICATION_SCHEME = NS + "classification-scheme";
    
    String TOPIC_REFERENCE = NS + "topic-reference";

    String TOPIC_LABEL = NS + "topic-label";

    String SENTIMENT = NS + "sentiment";



}
