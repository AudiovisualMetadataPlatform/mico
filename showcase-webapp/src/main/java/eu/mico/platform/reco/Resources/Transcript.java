package eu.mico.platform.reco.Resources;

import java.util.ArrayList;
import java.util.List;


public class Transcript {

    private List<Line> transcript = new ArrayList<>();

    public List<Line> getTranscript() {
        return transcript;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Line line : transcript) {
            sb.append(line.toString());
            sb.append("\n");
        }

        return sb.toString();
    }

    public int size() {
        return transcript.size();
    }

    public void add(Line line) {
        transcript.add(line);
    }
}
