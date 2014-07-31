#ifndef HAVE_VOCABULARIES_H
#define HAVE_VOCABULARIES_H

#include "rdf_model.hpp"

// helper macros for defining vocabulary properties
#define DECLARE_DC(property) const mico::rdf::model::URI property("http://purl.org/dc/terms/" #property)
#define DECLARE_LDP(property) const mico::rdf::model::URI property("http://www.w3.org/ns/ldp#" #property)
#define DECLARE_MA(property) const mico::rdf::model::URI property("http://www.w3.org/ns/ma-ont#" #property)


namespace mico {
  namespace rdf {
    namespace vocabularies {

			
      namespace DC {
				
	DECLARE_DC(Agent);
	DECLARE_DC(AgentClass);
	DECLARE_DC(BibliographicResource);
	DECLARE_DC(Box);
	DECLARE_DC(DCMIType);
	DECLARE_DC(DDC);
	DECLARE_DC(FileFormat);
	DECLARE_DC(Frequency);
	DECLARE_DC(IMT);
	DECLARE_DC(ISO3166);
	DECLARE_DC(ISO639_2);
	DECLARE_DC(ISO639_3);
	DECLARE_DC(Jurisdiction);
	DECLARE_DC(LCC);
	DECLARE_DC(LCSH);
	DECLARE_DC(LicenseDocument);
	DECLARE_DC(LinguisticSystem);
	DECLARE_DC(Location);
	DECLARE_DC(LocationPeriodOrJurisdiction);
	DECLARE_DC(MESH);
	DECLARE_DC(MediaType);
	DECLARE_DC(MediaTypeOrExtent);
	DECLARE_DC(MethodOfAccrual);
	DECLARE_DC(MethodOfInstruction);
	DECLARE_DC(NLM);
	DECLARE_DC(Period);
	DECLARE_DC(PeriodOfTime);
	DECLARE_DC(PhysicalMedium);
	DECLARE_DC(PhysicalResource);
	DECLARE_DC(Point);
	DECLARE_DC(Policy);
	DECLARE_DC(ProvenanceStatement);
	DECLARE_DC(RFC1766);
	DECLARE_DC(RFC3066);
	DECLARE_DC(RFC4646);
	DECLARE_DC(RightsStatement);
	DECLARE_DC(SizeOrDuration);
	DECLARE_DC(Standard);
	DECLARE_DC(TGN);
	DECLARE_DC(UDC);
	DECLARE_DC(URI);
	DECLARE_DC(W3CDTF);
	DECLARE_DC(abstract);
	DECLARE_DC(accessRights);
	DECLARE_DC(accrualMethod);
	DECLARE_DC(accrualPeriodicity);
	DECLARE_DC(accrualPolicy);
	DECLARE_DC(alternative);
	DECLARE_DC(audience);
	DECLARE_DC(available);
	DECLARE_DC(bibliographicCitation);
	DECLARE_DC(conformsTo);
	DECLARE_DC(contributor);
	DECLARE_DC(coverage);
	DECLARE_DC(created);
	DECLARE_DC(creator);
	DECLARE_DC(date);
	DECLARE_DC(dateAccepted);
	DECLARE_DC(dateCopyrighted);
	DECLARE_DC(dateSubmitted);
	DECLARE_DC(description);
	DECLARE_DC(educationLevel);
	DECLARE_DC(extent);
	DECLARE_DC(format);
	DECLARE_DC(hasFormat);
	DECLARE_DC(hasPart);
	DECLARE_DC(hasVersion);
	DECLARE_DC(identifier);
	DECLARE_DC(instructionalMethod);
	DECLARE_DC(isFormatOf);
	DECLARE_DC(isPartOf);
	DECLARE_DC(isReferencedBy);
	DECLARE_DC(isReplacedBy);
	DECLARE_DC(isRequiredBy);
	DECLARE_DC(isVersionOf);
	DECLARE_DC(issued);
	DECLARE_DC(language);
	DECLARE_DC(license);
	DECLARE_DC(mediator);
	DECLARE_DC(medium);
	DECLARE_DC(modified);
	DECLARE_DC(provenance);
	DECLARE_DC(publisher);
	DECLARE_DC(references);
	DECLARE_DC(relation);
	DECLARE_DC(replaces);
	DECLARE_DC(requires);
	DECLARE_DC(rights);
	DECLARE_DC(rightsHolder);
	DECLARE_DC(source);
	DECLARE_DC(spatial);
	DECLARE_DC(subject);
	DECLARE_DC(tableOfContents);
	DECLARE_DC(temporal);
	DECLARE_DC(title);
	DECLARE_DC(type);
	DECLARE_DC(valid);
				
      };


      namespace LDP {

	DECLARE_LDP(BasicContainer);
	DECLARE_LDP(Container);
	DECLARE_LDP(contains);
	DECLARE_LDP(DirectContainer);
	DECLARE_LDP(hasMemberRelation);
	DECLARE_LDP(IndirectContainer);
	DECLARE_LDP(insertedContentRelation);
	DECLARE_LDP(isMemberOfRelation);
	DECLARE_LDP(member);
	DECLARE_LDP(membershipResource);
	DECLARE_LDP(MemberSubject);
	DECLARE_LDP(NonRDFSource);
	DECLARE_LDP(PreferContainment);
	DECLARE_LDP(PreferEmptyContainer);
	DECLARE_LDP(PreferMembership);
	DECLARE_LDP(RDFSource);
	DECLARE_LDP(Resource);
      }
	

      namespace MA {

	DECLARE_MA(Agent);
	DECLARE_MA(AudioTrack);
	DECLARE_MA(Collection);
	DECLARE_MA(DataTrack);
	DECLARE_MA(Image);
	DECLARE_MA(IsRatingOf);
	DECLARE_MA(Location);
	DECLARE_MA(MediaFragment);
	DECLARE_MA(MediaResource);
	DECLARE_MA(Organisation);
	DECLARE_MA(Person);
	DECLARE_MA(Rating);
	DECLARE_MA(TargetAudience);
	DECLARE_MA(Track);
	DECLARE_MA(VideoTrack);
	DECLARE_MA(alternativeTitle);
	DECLARE_MA(averageBitRate);
	DECLARE_MA(collectionName);
	DECLARE_MA(copyright);
	DECLARE_MA(createdIn);
	DECLARE_MA(creationDate);
	DECLARE_MA(date);
	DECLARE_MA(depictsFictionalLocation);
	DECLARE_MA(description);
	DECLARE_MA(duration);
	DECLARE_MA(editDate);
	DECLARE_MA(features);
	DECLARE_MA(fragmentName);
	DECLARE_MA(frameHeight);
	DECLARE_MA(frameRate);
	DECLARE_MA(frameSizeUnit);
	DECLARE_MA(frameWidth);
	DECLARE_MA(hasAccessConditions);
	DECLARE_MA(hasAudioDescription);
	DECLARE_MA(hasCaptioning);
	DECLARE_MA(hasChapter);
	DECLARE_MA(hasClassification);
	DECLARE_MA(hasClassificationSystem);
	DECLARE_MA(hasCompression);
	DECLARE_MA(hasContributedTo);
	DECLARE_MA(hasContributor);
	DECLARE_MA(hasCopyrightOver);
	DECLARE_MA(hasCreated);
	DECLARE_MA(hasCreator);
	DECLARE_MA(hasFormat);
	DECLARE_MA(hasFragment);
	DECLARE_MA(hasGenre);
	DECLARE_MA(hasKeyword);
	DECLARE_MA(hasLanguage);
	DECLARE_MA(hasLocationCoordinateSystem);
	DECLARE_MA(hasMember);
	DECLARE_MA(hasNamedFragment);
	DECLARE_MA(hasPermissions);
	DECLARE_MA(hasPolicy);
	DECLARE_MA(hasPublished);
	DECLARE_MA(hasPublisher);
	DECLARE_MA(hasRating);
	DECLARE_MA(hasRatingSystem);
	DECLARE_MA(hasRelatedImage);
	DECLARE_MA(hasRelatedLocation);
	DECLARE_MA(hasRelatedResource);
	DECLARE_MA(hasSigning);
	DECLARE_MA(hasSource);
	DECLARE_MA(hasSubtitling);
	DECLARE_MA(hasTargetAudience);
	DECLARE_MA(hasTrack);
	DECLARE_MA(isCaptioningOf);
	DECLARE_MA(isChapterOf);
	DECLARE_MA(isCopyrightedBy);
	DECLARE_MA(isCreationLocationOf);
	DECLARE_MA(isFictionalLocationDepictedIn);
	DECLARE_MA(isFragmentOf);
	DECLARE_MA(isImageRelatedTo);
	DECLARE_MA(isLocationRelatedTo);
	DECLARE_MA(isMemberOf);
	DECLARE_MA(isNamedFragmentOf);
	DECLARE_MA(isProvidedBy);
	DECLARE_MA(isRelatedTo);
	DECLARE_MA(isSigningOf);
	DECLARE_MA(isSourceOf);
	DECLARE_MA(isTargetAudienceOf);
	DECLARE_MA(isTrackOf);
	DECLARE_MA(locationAltitude);
	DECLARE_MA(locationLatitude);
	DECLARE_MA(locationLongitude);
	DECLARE_MA(locationName);
	DECLARE_MA(locator);
	DECLARE_MA(mainOriginalTitle);
	DECLARE_MA(numberOfTracks);
	DECLARE_MA(playsIn);
	DECLARE_MA(provides);
	DECLARE_MA(ratingScaleMax);
	DECLARE_MA(ratingScaleMin);
	DECLARE_MA(ratingValue);
	DECLARE_MA(recordDate);
	DECLARE_MA(releaseDate);
	DECLARE_MA(samplingRate);
	DECLARE_MA(title);
	DECLARE_MA(trackName);

      }
    }
  }
}

#endif
