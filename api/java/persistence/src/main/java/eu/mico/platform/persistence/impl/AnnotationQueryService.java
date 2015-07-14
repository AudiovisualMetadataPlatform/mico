package eu.mico.platform.persistence.impl;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.Annotation;
import com.github.anno4j.querying.Criteria;
import com.github.anno4j.querying.QueryService;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper service for the Anno4j QueryService
 */
public class AnnotationQueryService {

    private static Logger log = LoggerFactory.getLogger(AnnotationQueryService.class);

    /**
     * Finding all persisted annotations restricted by the delivered criteria
     *
     * @param criteria
     * @return
     * @throws RepositoryException
     */
    public static List<Annotation> findAnnotations(Criteria criteria) throws RepositoryException {
        List<Criteria> criteriaList = new ArrayList();
        criteriaList.add(criteria);

        return findAnnotations(criteriaList);
    }

    /**
     * Finding all persisted annotations restricted by the delivered list of criteria
     *
     * @param criteriaList
     * @return
     * @throws RepositoryException
     */
    public static List<Annotation> findAnnotations(List<Criteria> criteriaList) throws RepositoryException {
        QueryService<Annotation> queryService = Anno4j.getInstance().createQueryService(Annotation.class);
        queryService.addPrefix("mico", "http://www.mico-project.eu/ns/platform/1.0/schema#");

        for(Criteria criteria : criteriaList) {
            queryService.setAnnotationCriteria(criteria.getLdpath(), criteria.getConstraint(), criteria.getComparison());
        }

        try {
            return queryService.execute();
        } catch (ParseException e) {
            log.error("could not parse the ldpath expressions to SPARQL queries:", e);
            throw new RepositoryException("could not parse the ldpath expressions to SPARQL queries", e);
        } catch (MalformedQueryException e) {
            log.error("the executed sparql query was malformed:", e);
            throw new RepositoryException("the executed sparql query was malformed", e);
        } catch (QueryEvaluationException e) {
            log.error("the SPARQL query could not have been evaluated:", e);
            throw new RepositoryException("the SPARQL query could not have been evaluated", e);
        }
    }
}
