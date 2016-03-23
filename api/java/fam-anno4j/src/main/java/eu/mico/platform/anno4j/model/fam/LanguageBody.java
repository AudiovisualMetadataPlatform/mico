package eu.mico.platform.anno4j.model.fam;

import org.openrdf.annotations.Iri;

import com.github.anno4j.model.namespaces.DCTERMS;

import eu.mico.platform.anno4j.model.namespaces.FAM;

@Iri(FAM.LANGUAGE_ANNOTATION)
public interface LanguageBody extends FAMBody {

    @Iri(DCTERMS.NS + "language")
    public String getLanguage();
    
    @Iri(DCTERMS.NS + "language")
    public void setLanguage(String language);
}
