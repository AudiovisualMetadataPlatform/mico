/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
