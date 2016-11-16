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

package eu.mico.platform.reco.Resources;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.Target;
import com.github.anno4j.model.impl.selector.FragmentSelector;
import com.github.anno4j.model.impl.targets.SpecificResource;
import eu.mico.platform.anno4j.model.AssetMMM;
import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.anno4j.model.fam.LinkedEntityBody;
import eu.mico.platform.anno4j.model.impl.bodymmm.SpeechToTextBodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.object.LangString;
import org.openrdf.repository.sparql.SPARQLRepository;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

import static junit.framework.Assert.assertNotNull;


public class NERQuery {

    private static Logger log = Logger.getAnonymousLogger();


    /**
     * Returns the results, the kaldi2rdf extractor stored to Marmotta for a given identifier
     *
     * @param identifier URI of the uploaded identifier
     * @param searchBy   specifies whether to search by source name (i.e., file name) or content item ID
     * @param mqh        Initialized {@link MICOQueryHelperMMM }
     * @return eu.mico.platform.recommendation.Transcript object (this is just a hack, will be changed later)
     */
    public static Transcript getTranscript(String identifier, DataField searchBy, MICOQueryHelperMMM mqh) {


        List<PartMMM> partMMMList = null;
        Transcript transcript = new Transcript();

        try {

            switch (searchBy) {
                case CONTENTITEM:
                    partMMMList = mqh
                            //TODO: Bug report. Anno4j's EvalQuery does not print error message, if angle brackets are omitted,
                            //TODO: but silently ignores filter
                            .filterBodyType("<" + MMMTERMS.STT_BODY_MICO + ">")
                            .getPartsOfItem(identifier)
                    ;
                    break;


                case NAME:
                    partMMMList = mqh
                            .filterBodyType("<" + MMMTERMS.STT_BODY_MICO + ">")
                            .getPartsByAssetName(identifier)
                    ;
                    break;
            }


            assertNotNull(partMMMList);
            log.info("# of parts: " + partMMMList.size());


            for (PartMMM partMMM : partMMMList) {

                SpeechToTextBodyMMM b = (SpeechToTextBodyMMM) partMMM.getBody();
                LangString speechValue = b.getValue();

                Set<Target> targetSet = partMMM.getTarget();

                String timeCode = "<No Timecode>";

                for (Target target : targetSet) {

                    SpecificResource sr = (SpecificResource) target;
                    if (sr == null) {
                        log.warning("SpecificResource is null");
                        break;
                    }

                    FragmentSelector fr = (FragmentSelector) sr.getSelector();

                    if (null == fr) {
                        log.warning("FragmentSelector is null");
                    } else {
                        timeCode = fr.getValue();
                    }


                }

                transcript.add(new Line(timeCode, speechValue));

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
     * @param searchBy   specifies whether to search by source name (i.e., file name) or content item ID
     * @return Entity -> eu.mico.platform.recommendation.EntityInfo mappings.
     */
    public static Map<String, EntityInfo> getLinkedEntities(String identifier, DataField searchBy, MICOQueryHelperMMM mqh) {

        List<String> ignoredNERResources = new ArrayList<>();
        ignoredNERResources.add("http://dbpedia.org/resource/Logical_conjunction");
        ignoredNERResources.add("http://dbpedia.org/resource/Logical_disjunction");

        Map<String, EntityInfo> entities = new HashMap<>();

        List<PartMMM> annos;

        try {

            mqh = mqh.filterBodyType("<http://vocab.fusepool.info/fam#LinkedEntity>");

            switch (searchBy) {
                case CONTENTITEM:
                    annos = mqh.getPartsOfItem(identifier);
                    break;
                case NAME:
                default:
                    annos = mqh.getPartsByAssetName(identifier);
                    break;
            }

            assertNotNull(annos);
            log.info("# of annotations: " + annos.size());

            for (PartMMM an : annos) {
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


    public static List<String> getItemsByFormat(String format, MICOQueryHelperMMM mqh) {

        List<String> retList = new ArrayList<>();

        try {
            List<ItemMMM> items = mqh.getItemsByFormat(format);

            for (ItemMMM item : items) {
                retList.add(item.getResourceAsString());
            }


        } catch (OpenRDFException | ParseException e) {
            e.printStackTrace();

        }

        return retList;

    }

    public static List<ItemDescription> getItemDescriptionByFormat(String format, MICOQueryHelperMMM mqh) {

        List<ItemDescription> retList = new ArrayList<>();

        try {
            List<ItemMMM> items = mqh.getItemsByFormat(format);

            for (ItemMMM item : items) {
                AssetMMM asset = item.getAsset();
                if (asset != null) {

                    String fileName = asset.getName();
                    String itemUri = item.toString();

                    String id, prefix;


                    int splitPoint = itemUri.lastIndexOf("/") + 1;

                    prefix = itemUri.substring(0, splitPoint);
                    id = itemUri.substring(splitPoint);


                    try {
                        //noinspection ResultOfMethodCallIgnored
                        UUID.fromString(id);

                        retList.add(
                                new ItemDescription(fileName, id, prefix)
                        );

                    } catch (IllegalArgumentException e) {

                        log.warning("Unable to parse item " + itemUri);
                    }
                }
            }
        } catch (OpenRDFException | ParseException e) {
            e.printStackTrace();

        }

        return retList;

    }

    public static List<String> getFileNamesByFormat(String format, MICOQueryHelperMMM mqh) {

        List<String> retList = new ArrayList<>();

        try {
            List<ItemMMM> items = mqh.getItemsByFormat(format);

            for (ItemMMM item : items) {
                AssetMMM asset = item.getAsset();
                if (asset != null) {
                    retList.add(
                            asset.getName()
                    );
                }
            }


        } catch (OpenRDFException | ParseException e) {
            e.printStackTrace();

        }

        return retList;

    }

    public static MICOQueryHelperMMM createMicoQueryHelper(String marmottaBaseUri) throws RepositoryException, RepositoryConfigException {

        String marmotta_base;
        String endpointUrl;

        marmotta_base = marmottaBaseUri;
        if (marmotta_base.endsWith("/")) {
            endpointUrl = marmottaBaseUri + "sparql/select";
        } else {
            endpointUrl = marmottaBaseUri + "/sparql/select";
        }


        Anno4j anno4j = new Anno4j();
        Repository micoSparqlEndpoint = new SPARQLRepository(endpointUrl);
        micoSparqlEndpoint.initialize();
        anno4j.setRepository(micoSparqlEndpoint);
        return new MICOQueryHelperMMM(anno4j);
    }


    public static class ItemDescription {
        private String filename;
        private String id;
        private String prefix;

        ItemDescription(String filename, String id, String prefix) {
            this.filename = filename;

            this.id = id;
            this.prefix = prefix;
        }

        public String getFilename() {
            if (filename == null) {
                return "";
            }
            return filename;
        }

        public String getId() {
            if (id == null) {
                return "";
            }
            return id;
        }

        public String getPrefix() {
            if (prefix == null) {
                return "";
            }
            return prefix;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemDescription that = (ItemDescription) o;
            return Objects.equals(filename, that.filename) &&
                    Objects.equals(id, that.id) &&
                    Objects.equals(prefix, that.prefix);
        }

        @Override
        public int hashCode() {
            return Objects.hash(filename, id, prefix);
        }
    }
}
