/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.mico.platform.persistence.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.vocabulary.RDFS;

import com.github.anno4j.model.namespaces.DC;
import com.github.anno4j.model.namespaces.OADM;
import com.github.anno4j.model.namespaces.RDF;
import com.google.common.collect.ImmutableMap;

import eu.mico.platform.anno4j.model.namespaces.FAM;
import eu.mico.platform.anno4j.model.namespaces.gen.MA;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;

/**
 * @author Horst Stadler
 */
public class URITools {

    public static String normalizeURI(String uri) {
        try {
            return (new URI(uri)).normalize().toString();
        } catch (URISyntaxException ex) {
            return null;
        }
    }
    
  //map from namespace to prefix
  	public static final Map<String,String> knownNamespaces;
  	static{
  		Map<String,String> map = new HashMap<String,String>();
  		map.put(MMMTERMS.NS, MMMTERMS.PREFIX);
  		map.put(MMM.NS, MMM.PREFIX);
  		map.put(MA.NAMESPACE, MA.PREFIX);
  		map.put(FAM.NS, FAM.PREFIX);
  		
  		map.put(DC.NS,DC.PREFIX);
  		map.put(OADM.NS,OADM.PREFIX);
  		
  		map.put(RDF.NS, RDF.PREFIX);
  		map.put(RDFS.NAMESPACE, RDFS.PREFIX);
  		
  		knownNamespaces = ImmutableMap.copyOf(map);
  	}
  	
  	public static String demangleNamespaceIfKnown(String fullURI){
  		
  		String out = fullURI;
  		for(Map.Entry<String, String> entry : knownNamespaces.entrySet()){
  			if(fullURI.contains(entry.getKey())){
  				return out.replace(entry.getKey(), entry.getValue()+":");
  			}
  		}
  		return out;
  	}
}
