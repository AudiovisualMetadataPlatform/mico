package eu.mico.platform.anno4j.model.impl.bodymmm;

import eu.mico.platform.anno4j.model.namespaces.gen.MA;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;

import org.openrdf.annotations.Iri;

/**
 * Generic body designed for low level media Info. Ontology for Media Resources 1.0 offers many possible relationships.
 * Should be more fine-grained for different media files.
 *
 * XML file associated (via Asset) which contains all extracted information.
 */
@Iri(MMMTERMS.MEDIA_INFO_BODY)
public interface MediaInfoBodyMMM extends MultiMediaBodyMMM, ImageDimensionBodyMMM {

  @Iri(MA.SAMPLING_RATE_STRING)
  public String getSamplingRate();

  @Iri(MA.SAMPLING_RATE_STRING)
  public void setSamplingRate(String rate);

  @Iri(MA.AVERAGE_BIT_RATE_STRING)
  public String getAvgBitRate();

  @Iri(MA.AVERAGE_BIT_RATE_STRING)
  public void setAvgBitRate(String avgBitRate);
}
