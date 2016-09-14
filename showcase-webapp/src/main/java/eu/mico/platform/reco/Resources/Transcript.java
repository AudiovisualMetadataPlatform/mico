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
