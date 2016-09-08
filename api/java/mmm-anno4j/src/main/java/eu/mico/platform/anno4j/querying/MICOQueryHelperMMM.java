package eu.mico.platform.anno4j.querying;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.namespaces.DCTERMS;
import com.github.anno4j.querying.QueryService;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import java.util.List;

/**
 * Class with some predefined queries for the set Anno4j object.
 */
public class MICOQueryHelperMMM {

    /**
     * Selector type restriction
     */
    private String selectorTypeRestriction;

    /**
     * Body type restriction
     */
    private String bodyTypeRestriction;

    /**
     * Target type restriction
     */
    private String targetTypeRestriction;

    /**
     * A configured instance of anno4j
     */
    private Anno4j anno4j;

    public MICOQueryHelperMMM(Anno4j anno4j) {
        this.anno4j = anno4j;
    }

    /**
     * Allows to query all annotation objects of a given content item.
     *
     * @param contentItemId The id (url) of the content item.
     * @return List of annotations related to the given content item.
     *
     * @throws RepositoryException
     * @throws QueryEvaluationException
     * @throws MalformedQueryException
     * @throws ParseException
     */
    public List<PartMMM> getPartsOfItem(String contentItemId) throws RepositoryException, QueryEvaluationException, MalformedQueryException, ParseException {
        QueryService qs = anno4j.createQueryService()
                .addPrefix(MMM.PREFIX, MMM.NS)
                .addCriteria("^mmm:hasPart", contentItemId);

        processTypeRestriction(qs);

        return qs.execute(PartMMM.class);
    }

    /**
     * Queries those PartMMM objects that are added to an item, whose AssetMMM has the given sourceName.
     *
     * @param sourceName The name of the injected source.
     * @return List of parts related to the specific source name.
     *
     * @throws RepositoryException
     * @throws QueryEvaluationException
     * @throws MalformedQueryException
     * @throws ParseException
     */
    public List<PartMMM> getPartsBySourceNameOfAsset(String sourceName) throws RepositoryException, QueryEvaluationException, MalformedQueryException, ParseException {
        QueryService qs = anno4j.createQueryService()
                .addPrefix(MMM.PREFIX, MMM.NS)
                .addPrefix(DCTERMS.PREFIX, DCTERMS.NS)
                .addCriteria("^mmm:hasPart/mmm:hasAsset", sourceName);

        processTypeRestriction(qs);

        return qs.execute(PartMMM.class);
    }

    /**
     * Queries those PartMMM objects that are added to an item, whose AssetMMM has the given physical location.
     *
     * @param location The name of the injected source.
     * @return List of parts related to the specific source name.
     *
     * @throws RepositoryException
     * @throws QueryEvaluationException
     * @throws MalformedQueryException
     * @throws ParseException
     */
    public List<PartMMM> getPartsBySourceLocationOfAsset(String location) throws RepositoryException, QueryEvaluationException, MalformedQueryException, ParseException {
        QueryService qs = anno4j.createQueryService()
                .addPrefix(MMM.PREFIX, MMM.NS)
                .addPrefix(DCTERMS.PREFIX, DCTERMS.NS)
                .addCriteria("^mmm:hasPart/mmm:hasAsset/mmm:hasLocation", location);

        processTypeRestriction(qs);

        return qs.execute(PartMMM.class);
    }

    /**
     * @param type The type of the body as String, i.e. "mico:AVQBody"
     */
    public MICOQueryHelperMMM filterBodyType(String type) {
        this.bodyTypeRestriction = "[is-a "+ type + "]";
        return this;
    }

    /**
     * @param type The type of the selector as String, i.e. "oa:FragmentSelector"
     */
    public MICOQueryHelperMMM filterSelectorType(String type) {
        this.selectorTypeRestriction  = "[is-a "+ type + "]";
        return this;
    }

    /**
     * @param type The type of the target as String, i.e. "mico:IntialTarget"
     */
    public MICOQueryHelperMMM filterTargetType(String type) {
        this.targetTypeRestriction = "[is-a "+ type + "]";
        return this;
    }

    /**
     * Checks if type restrictions were set and adds them to the QueryService object.
     *
     * @param qs The anno4j QueryService object
     */
    private void processTypeRestriction(QueryService qs) {
        if(selectorTypeRestriction != null) {

            qs.addCriteria("oa:hasTarget/oa:hasSelector" + selectorTypeRestriction);
        }

        if(bodyTypeRestriction != null) {
            qs.addCriteria("oa:hasBody" + bodyTypeRestriction);
        }

        if(targetTypeRestriction != null) {
            qs.addCriteria("oa:hasTarget" + targetTypeRestriction);
        }
    }
}
