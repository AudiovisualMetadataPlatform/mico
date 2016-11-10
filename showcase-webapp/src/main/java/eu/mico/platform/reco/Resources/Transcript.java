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

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;


public class Transcript {

    private List<Line> transcript = new ArrayList<>();

    public List<Line> getTranscript() {
        return transcript;
    }

    @Override
    public String toString() {

        Collections.sort(transcript);

        StringJoiner sj = new StringJoiner("\n");

        for (Line line : transcript) {
            sj.add(line.toString());
        }

        return sj.toString();
    }

    public int size() {
        return transcript.size();
    }

    public void add(Line line) {
        transcript.add(line);
    }

    public String toJson() {

        Collections.sort(transcript);


        JsonArrayBuilder transcriptJson = Json.createArrayBuilder();


        for (Line line : transcript) {

            JsonObject jo = Json.createObjectBuilder()
                    .add("timestamp", line.timecode)
                    .add("text", line.langstring.toString())
                    .add("language", line.langstring.getLang())
                    .build();

            transcriptJson.add(jo);

        }

        JsonObject retJson = Json.createObjectBuilder()
                .add("transcript", transcriptJson.build())
                .build();

        return retJson.toString();


    }
}
