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

package eu.mico.platform.anno4j.querying;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.namespaces.DC;
import com.github.anno4j.model.namespaces.DCTERMS;
import com.github.anno4j.querying.QueryService;
import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;

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
     * Returns the anno4j instance used by this class
     *
     * @return anno4j instance
     */
    public Anno4j getAnno4j() {
        return anno4j;
    }

    /**
     * Allows to query all annotation objects of a given content item.
     *
     * @param contentItemId The id (url) of the content item.
     * @return List of annotations related to the given content item.
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
     * Queries those PartMMM objects that are added to an item, whose AssetMMM has the given associated name
     * (e.g., the file name).
     *
     * @param name The name of the injected source.
     * @return List of parts related to the specific source name.
     * @throws RepositoryException
     * @throws QueryEvaluationException
     * @throws MalformedQueryException
     * @throws ParseException
     */
    public List<PartMMM> getPartsByAssetName(String name) throws RepositoryException, QueryEvaluationException, MalformedQueryException, ParseException {
        QueryService qs = anno4j.createQueryService()
                .addPrefix(MMM.PREFIX, MMM.NS)
                .addPrefix(DCTERMS.PREFIX, DCTERMS.NS)
                .addCriteria("^mmm:hasPart/mmm:hasAsset/mmm:hasName", name);

        processTypeRestriction(qs);

        return qs.execute(PartMMM.class);
    }


    public List<ItemMMM> getItemsByFormat(String format) throws RepositoryConfigException, RepositoryException, ParseException, MalformedQueryException, QueryEvaluationException {


        QueryService qs = anno4j.createQueryService()
                .addPrefix(MMM.PREFIX, MMM.NS)
                .addPrefix(DCTERMS.PREFIX, DCTERMS.NS)
                .addPrefix(DC.PREFIX, DC.NS)
                .addCriteria("mmm:hasAsset/dc:format", format);

        processTypeRestriction(qs);

        return qs.execute(ItemMMM.class);

    }


    /**
     * Queries those PartMMM objects that are added to an item, whose AssetMMM has the given physical location.
     *
     * @param location The name of the injected source.
     * @return List of parts related to the specific source name.
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
        this.bodyTypeRestriction = "[is-a " + type + "]";
        return this;
    }

    /**
     * @param type The type of the selector as String, i.e. "oa:FragmentSelector"
     */
    public MICOQueryHelperMMM filterSelectorType(String type) {
        this.selectorTypeRestriction = "[is-a " + type + "]";
        return this;
    }

    /**
     * @param type The type of the target as String, i.e. "mico:IntialTarget"
     */
    public MICOQueryHelperMMM filterTargetType(String type) {
        this.targetTypeRestriction = "[is-a " + type + "]";
        return this;
    }

    /**
     * Checks if type restrictions were set, adds them to the QueryService object and clears them for
     * further queries with the same mqh object.
     *
     * @param qs The anno4j QueryService object
     */
    private void processTypeRestriction(QueryService qs) {
        if (selectorTypeRestriction != null) {
            qs.addCriteria("oa:hasTarget/oa:hasSelector" + selectorTypeRestriction);
            selectorTypeRestriction = null;
        }

        if (bodyTypeRestriction != null) {
            qs.addCriteria("oa:hasBody" + bodyTypeRestriction);
            bodyTypeRestriction = null;
        }

        if (targetTypeRestriction != null) {
            qs.addCriteria("oa:hasTarget" + targetTypeRestriction);
            targetTypeRestriction = null;
        }
    }
}
