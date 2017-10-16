package eu.mico.platform.anno4j.model.impl.bodymmm;

import org.openrdf.annotations.Iri;

import com.github.anno4j.model.Body;

import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;



@Iri(MMMTERMS.MICRO_FORMATS_BODY)
public interface MicroformatsBody extends Body {

	// LICENSE
	@Iri(MMMTERMS.MICRO_FORMATS_BODY+"#license")
	void setLicense(String license);

	@Iri(MMMTERMS.MICRO_FORMATS_BODY+"#license")
	String getLicense();

	// IS LICENSE SIGNED
	@Iri(MMMTERMS.MICRO_FORMATS_BODY+"#signed")
	void setIsLicenseSigned(boolean isSigned);

	@Iri(MMMTERMS.MICRO_FORMATS_BODY+"#signed")
	boolean getIsLicenseSigned();

	// RESULT
	@Iri(MMMTERMS.MICRO_FORMATS_BODY+"#result")
	void setResult(String result);

	@Iri(MMMTERMS.MICRO_FORMATS_BODY+"#result")
	String getResult();

	// SIGNER
	@Iri(MMMTERMS.MICRO_FORMATS_BODY+"#signer")
	void setSigner(String signer);

	@Iri(MMMTERMS.MICRO_FORMATS_BODY+"#signer")
	String getSigner();

}