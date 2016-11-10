/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.mico.platform.persistence.test;

import eu.mico.platform.persistence.util.URITools;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import static org.junit.Assert.*;

public class URIToolsTest {
	
	private static final Logger log = LoggerFactory.getLogger(URIToolsTest.class);
	
	private final static String DUMMY_ONTOLOGY_ELEMENT =  "dummyOntolgyElement";

    @Test
    public void unkownNamespaceTest(){
    	
    	final String inURI = "http://not.existing/namespace#"+DUMMY_ONTOLOGY_ELEMENT;
    	final String outURI = URITools.demangleNamespaceIfKnown(inURI);
    	
    	assertTrue("the input URI should not have changed", outURI.contentEquals(inURI));
        
    }
    
    @Test
    public void knownNamespacesTest(){ 
    	
    	
    	for(Map.Entry<String, String> entry : URITools.knownNamespaces.entrySet()){
    		String inURI  = entry.getKey()+DUMMY_ONTOLOGY_ELEMENT;
    		String outURI = URITools.demangleNamespaceIfKnown(inURI);
    		
    		log.debug("Demangled {} into {}",inURI,outURI);
    		
    		String expectedURI = entry.getValue()+":"+DUMMY_ONTOLOGY_ELEMENT;
    		log.debug("The expected result is {}",expectedURI);
    		
        	assertFalse("The input URI should have changed", outURI.contentEquals(inURI));
        	assertTrue("The output URI does not match the expected value",outURI.contentEquals(expectedURI));
    	}
    	
    	
    	
        
    }

}
