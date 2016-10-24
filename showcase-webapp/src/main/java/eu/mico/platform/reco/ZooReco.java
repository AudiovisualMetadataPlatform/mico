package eu.mico.platform.reco;

import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import eu.mico.platform.reco.Resources.*;

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

    private static final int CHAT_LENGTH_THRESHOLD = 3;
    private static final double CHAT_LENGTH_PENALTY = 0.3;
    private static final double SENTIMENT_PENALTY = 0.3;
    private static final double TALKED_ABOUT_PENALTY = 0.2;


    private MICOQueryHelperMMM mqh;

    public ZooReco(MICOQueryHelperMMM mqh) {
        this.mqh = mqh;
    }

    public double getDebatedScore(String subject_id) {

        List<String> animalDetectionItems = getAnimalDetectionItems(subject_id);
        List<String> chatAnalysisItems = getChatAnalysisItems(subject_id);

        List<String> chatTranscript = getChatTranscript(subject_id);

        SentimentResult sentiment = getChatSentiment(subject_id);

        List<EntityInfo> linkedEntities = new ArrayList<>();
        List<AnimalInfo> detectedAnimals = new ArrayList<>();

        for (String itemId : chatAnalysisItems) {
            Map<String, EntityInfo> currentEntities = NERQuery.getLinkedEntities(itemId, DataField.CONTENTITEM, mqh);


            if (currentEntities != null) {
                List<EntityInfo> currentEntityList = (List<EntityInfo>) currentEntities.values();
                linkedEntities.addAll(currentEntityList);
            }
        }

        for (String itemId : animalDetectionItems) {
            List<AnimalInfo> currentAnimals = getDetectedAnimals(itemId, mqh);
            if (currentAnimals != null) {
                detectedAnimals.addAll(currentAnimals);
            }
        }


        double score = 0;

        int overlappingCount = RecoUtils.countOverlappingEntites(linkedEntities, detectedAnimals);

        score += TALKED_ABOUT_PENALTY * (linkedEntities.size() - overlappingCount);

        if (chatTranscript.size() > CHAT_LENGTH_THRESHOLD) {
            score += CHAT_LENGTH_PENALTY;
        }

        if (sentiment == SentimentResult.NEGATIVE) {
            score += SENTIMENT_PENALTY;
        }


        return score;

    }

    private SentimentResult getChatSentiment(String subject_id) {
        return SentimentResult.POSITIVE;
    }

    private List<String> getChatTranscript(String subject_id) {
        return new ArrayList<>();
    }

    private List<AnimalInfo> getDetectedAnimals(String itemId, MICOQueryHelperMMM mqh) {
        throw new RuntimeException("Not implemented, yet");
    }

    private List<String> getChatAnalysisItems(String subject_id) {
        throw new RuntimeException("Not implemented, yet");
    }

    private List<String> getAnimalDetectionItems(String subject_id) {
        throw new RuntimeException("Not implemented, yet");

    }
}
