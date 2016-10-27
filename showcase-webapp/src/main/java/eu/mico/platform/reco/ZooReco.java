package eu.mico.platform.reco;

import com.github.anno4j.model.namespaces.DCTERMS;
import com.github.anno4j.querying.QueryService;
import eu.mico.platform.anno4j.model.fam.SentimentBody;
import eu.mico.platform.anno4j.model.impl.bodymmm.AnimalDetectionBodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import eu.mico.platform.reco.Resources.*;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.openrdf.OpenRDFException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ZooReco {

    private static final int CHAT_LENGTH_THRESHOLD = 5;
    private static final double CHAT_LENGTH_PENALTY = 0.3;
    private static final double SENTIMENT_PENALTY = 0.3;
    private static final double TALKED_ABOUT_PENALTY = 0.1;


    private MICOQueryHelperMMM mqh;

    public ZooReco(MICOQueryHelperMMM mqh) {
        this.mqh = mqh;
    }

    List<AnimalInfo> getDetectedAnimals(String itemId, MICOQueryHelperMMM mqh) {

        List<AnimalInfo> retList = new ArrayList<>();

        QueryService qs;
        try {
            qs = mqh.getAnno4j().createQueryService()
                    .addPrefix(MMM.PREFIX, MMM.NS)
                    .addPrefix("fusepool", "http://vocab.fusepool.info/fam#")
                    .addPrefix(DCTERMS.PREFIX, DCTERMS.NS)
                    .addCriteria("^mmm:hasBody/^mmm:hasPart", itemId);


            List<AnimalDetectionBodyMMM> adbs = qs.execute(AnimalDetectionBodyMMM.class);

            for (AnimalDetectionBodyMMM adbMMM : adbs) {
                AnimalInfo ai = new AnimalInfo(adbMMM.getValue(), adbMMM.getConfidence());
                retList.add(ai);
            }


        } catch (ParseException | OpenRDFException e) {
            e.printStackTrace();
        }

        return retList;
    }


    //TODO: item - subject-id-Mapping

    public double getDebatedScore(String subject_id) {

        List<String> animalDetectionItems = getAnimalDetectionItems(subject_id);
        List<String> chatAnalysisItems = getChatAnalysisItems(subject_id);

        return getDebatedScore(animalDetectionItems.get(0), chatAnalysisItems);


    }

    /**
     * Retrieves sentiment values of all items in itemIdList and returns SentimentResult based on their median.
     *
     * @param itemIdList List of items with sentiment annotation
     * @return SentimentResult based on median, Sentiment.NEUTRAL if nothing was found
     */
    SentimentResult getChatSentiment(List<String> itemIdList) {

        double[] sentimentList = new double[itemIdList.size()];
        int i = 0;
        QueryService qs;

        for (String itemId : itemIdList) {

            try {
                qs = mqh.getAnno4j().createQueryService()
                        .addPrefix(MMM.PREFIX, MMM.NS)
                        .addPrefix("fusepool", "http://vocab.fusepool.info/fam#")
                        .addPrefix(DCTERMS.PREFIX, DCTERMS.NS)
                        .addCriteria("^mmm:hasBody/^mmm:hasPart", itemId);


                List<SentimentBody> sentiments = qs.execute(SentimentBody.class);

                for (SentimentBody sb : sentiments) {
                    sentimentList[i] = sb.getSentiment();
                }


            } catch (ParseException | OpenRDFException e) {
                e.printStackTrace();
            }

            i++;

        }

        double medianSentiment = RecoUtils.getMedian(sentimentList);

        if (medianSentiment > 0) {
            return SentimentResult.POSITIVE;
        } else if (medianSentiment < 0) {
            return SentimentResult.NEGATIVE;
        } else {
            return SentimentResult.NEUTRAL;
        }
    }

    private List<String> getChatTranscript(String subject_id) {
        throw new RuntimeException("Not implemented, yet");
    }

    private List<String> getChatAnalysisItems(String subject_id) {
        throw new RuntimeException("Not implemented, yet");
    }

    private List<String> getAnimalDetectionItems(String subject_id) {
        throw new RuntimeException("Not implemented, yet");

    }

    public double getDebatedScore(String subject_item, List<String> chatItems) {

        SentimentResult sentiment = SentimentResult.NEUTRAL;
        if (chatItems.size() > 0) {
            sentiment = getChatSentiment(chatItems);
        }


        List<EntityInfo> linkedEntities = new ArrayList<>();
        List<AnimalInfo> detectedAnimals = new ArrayList<>();

        for (String itemId : chatItems) {
            Map<String, EntityInfo> currentEntities = NERQuery.getLinkedEntities(itemId, DataField.CONTENTITEM, mqh);

            if (currentEntities != null && currentEntities.size() > 0) {
                //List<EntityInfo> currentEntityList = (List<EntityInfo>) currentEntities.values();
                List<EntityInfo> currentEntityList = new ArrayList<>();
                for (EntityInfo ei: currentEntities.values()) {
                    currentEntityList.add(ei);
                }
                linkedEntities.addAll(currentEntityList);
            }
        }

        List<AnimalInfo> currentAnimals = getDetectedAnimals(subject_item, mqh);
        if (currentAnimals != null) {
            detectedAnimals.addAll(currentAnimals);
        }


        double score = 0;

        int overlappingCount = RecoUtils.countOverlappingEntites(linkedEntities, detectedAnimals);

        score += TALKED_ABOUT_PENALTY * (linkedEntities.stream().distinct().count() - overlappingCount);

        if (chatItems.size() > CHAT_LENGTH_THRESHOLD) {
            score += CHAT_LENGTH_PENALTY;
        }

        if (sentiment == SentimentResult.NEGATIVE) {
            score += SENTIMENT_PENALTY;
        }


        return score;
    }
}
