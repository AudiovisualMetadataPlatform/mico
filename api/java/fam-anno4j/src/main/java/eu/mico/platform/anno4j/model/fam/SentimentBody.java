package eu.mico.platform.anno4j.model.fam;

import org.openrdf.annotations.Iri;

import eu.mico.platform.anno4j.model.namespaces.FAM;

@Iri(FAM.SENTIMENT_ANNOTATION)
public interface SentimentBody extends FAMBody {

    @Iri(FAM.SENTIMENT)
    public double getSentiment();
    
    @Iri(FAM.SENTIMENT)
    public void setSentiment(double sentiment);
}
