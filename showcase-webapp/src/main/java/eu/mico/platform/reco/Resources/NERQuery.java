package eu.mico.platform.reco.Resources;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.impl.selector.FragmentSelector;
import com.github.anno4j.model.impl.targets.SpecificResource;
import eu.mico.platform.anno4j.model.fam.LinkedEntityBody;
import eu.mico.platform.anno4j.model.impl.bodymmm.SpeechToTextBodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;


import eu.mico.platform.anno4j.querying.MICOQueryHelper;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.object.LangString;
import org.openrdf.repository.sparql.SPARQLRepository;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static junit.framework.Assert.assertNotNull;


public class NERQuery {

    private static final String endpointUrl = "http://mico-platform:8080/marmotta/sparql/";
    private static Logger log = Logger.getAnonymousLogger();


    /**
     * Returns the results, the kaldi2rdf extractor stored to Marmotta for a given contentItem
     *
     * @param contentItem URI of the uploaded contentItem
     * @return eu.mico.platform.recommendation.Transcript object (this is just a hack, will be changed later)
     */
    public static Transcript getTranscript(String contentItem) {


        List<Annotation> annos;
        Transcript transcript = new Transcript();

        try {
            MICOQueryHelper mqh = getMicoQueryHelper();

            annos = mqh
                    //TODO: Bug report. Anno4j's EvalQuery does not print error message, if angle brackets are omitted,
                    //TODO: but silently ignores filter
                    .filterBodyType("<" + MMMTERMS.STT_BODY_MICO + ">")
                    .getAnnotationsOfContentItem(contentItem)
            ;

            assertNotNull(annos);
            log.info("# of annotations: " + annos.size());


            for (Annotation an : annos) {
                SpeechToTextBodyMMM b = (SpeechToTextBodyMMM) an.getBody();
                LangString speechValue = b.getValue();

                SpecificResource sr = (SpecificResource) an.getTarget();
                FragmentSelector fr = (FragmentSelector) sr.getSelector();

                String timeCode = fr.getValue();

                transcript.transcript.add(new Line(timeCode, speechValue));
            }
        } catch (OpenRDFException | ParseException e) {
            log.warning(MessageFormat.format("Query failed: {0}", e.getMessage()));
            return null;
        }

        return transcript;
    }


    /**
     * Returns the results, the ner-text extractor stored to Marmotta for a given contentItem
     *
     * @param identifier URI of the uploaded contentItem or source name
     * @param searchBy specifies whether to search by source name (i.e., file name) or content item ID
     * @return Entity -> eu.mico.platform.recommendation.EntityInfo mappings. (this is just a hack, will be changed later)
     */
    public static Map<String, EntityInfo> getLinkedEntities(String identifier, DataField searchBy) {

        List<String> ignoredNERResources = new ArrayList<>();
        ignoredNERResources.add("http://dbpedia.org/resource/Logical_conjunction");
        ignoredNERResources.add("http://dbpedia.org/resource/Logical_disjunction");

        Map<String, EntityInfo> entities = new HashMap<>();

        List<Annotation> annos;

        try {


            MICOQueryHelper mqh = getMicoQueryHelper();

            mqh = mqh.filterBodyType("<http://vocab.fusepool.info/fam#LinkedEntity>");

            switch (searchBy)   {
                case CONTENTITEM:
                    annos = mqh.getAnnotationsOfContentItem(identifier);
                    break;
                case SOURCE:
                default:
                    annos = mqh.getAnnotationsBySourceName(identifier);
                    break;
            }


            assertNotNull(annos);
            log.info("# of annotations: " + annos.size());

            for (Annotation an : annos) {
                LinkedEntityBody b = (LinkedEntityBody) an.getBody();

                if (ignoredNERResources.contains(b.getEntity().toString())) {
                    continue;
                }

                entities.put(b.getEntity().toString(), new EntityInfo(b));
            }
        } catch (OpenRDFException | ParseException e) {
            log.warning(MessageFormat.format("Query failed: {0}", e.getMessage()));
            return null;
        }


        return entities;
    }

    private static MICOQueryHelper getMicoQueryHelper() throws RepositoryException, RepositoryConfigException {
        Anno4j anno4j = new Anno4j();
        Repository micoSparqlEndpoint = new SPARQLRepository(endpointUrl);
        micoSparqlEndpoint.initialize();
        anno4j.setRepository(micoSparqlEndpoint);
        return new MICOQueryHelper(anno4j);
    }
}
