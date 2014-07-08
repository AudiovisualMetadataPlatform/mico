package eu.mico.platform.persistence.util;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SPARQLUtil {

    private static Logger log = LoggerFactory.getLogger(SPARQLUtil.class);

    /**
     * Create a "named" query, loading it from the system resources and applying variable substitutions according to
     * the parameter map passed as argument. Variables are of the form $(...).
     * @param name
     * @param parameters
     * @return
     */
    public static String createNamed(String name, Map<String,String> parameters) {
        StrSubstitutor subst = new StrSubstitutor(parameters,"$(",")");

        try {
            String queryFmt = IOUtils.toString(SPARQLUtil.class.getResourceAsStream("/sparql/"+name+".sparql"), Charset.defaultCharset());
            String query = subst.replace(queryFmt);

            return query;
        } catch (IOException e) {
            throw new UnsupportedOperationException("the query with name "+name+" could not be found");
        }
    }

    // helpers
    public static String createNamed(String name, String k1, String v1) {
        return createNamed(name, ImmutableMap.of(k1,v1));
    }

    public static String createNamed(String name, String k1, String v1, String k2, String v2) {
        return createNamed(name, ImmutableMap.of(k1,v1, k2, v2));
    }

    public static String createNamed(String name, String k1, String v1, String k2, String v2, String k3, String v3) {
        return createNamed(name, ImmutableMap.of(k1,v1,k2,v2,k3,v3));
    }

}
