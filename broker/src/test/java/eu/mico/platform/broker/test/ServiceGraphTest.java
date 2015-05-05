/**
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
package eu.mico.platform.broker.test;

import eu.mico.platform.broker.exception.StateNotFoundException;
import eu.mico.platform.broker.model.ServiceDescriptor;
import eu.mico.platform.broker.model.ServiceGraph;
import eu.mico.platform.broker.model.TypeDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.impl.URIImpl;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ServiceGraphTest {

    @Test
    public void testAddEdges() throws StateNotFoundException {
        ServiceGraph g = new ServiceGraph();

        TypeDescriptor a = new TypeDescriptor("A");
        TypeDescriptor b = new TypeDescriptor("B");
        TypeDescriptor c = new TypeDescriptor("C");

        ServiceDescriptor ab = new ServiceDescriptor(new URIImpl("http://example.org/services/ABService"), "A-B-queue","A","B");
        ServiceDescriptor bc = new ServiceDescriptor(new URIImpl("http://example.org/services/BCService"), "B-C-queue","B","C");
        ServiceDescriptor ac = new ServiceDescriptor(new URIImpl("http://example.org/services/ACService"), "A-C-queue","A","C");

        g.addEdge(a,b,ab);
        g.addEdge(b,c,bc);
        g.addEdge(a,c,ac);

        Assert.assertEquals(3, g.edgeSet().size());
        Assert.assertEquals(b,g.getTargetState(ab.getUri()));
        Assert.assertEquals(c,g.getTargetState(ac.getUri()));
        Assert.assertEquals(c,g.getTargetState(bc.getUri()));
    }
}
