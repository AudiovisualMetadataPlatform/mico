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

package eu.mico.platform.reco.model;


import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;


public class PioEventDataTest {

    @Test
    public void testFromJSON() throws Exception {


        String inputJson = "{\n" +
                "  \"event\" : \"rate\",\n" +
                "  \"entityType\" : \"user\",\n" +
                "  \"entityId\" : \"1\",\n" +
                "  \"targetEntityType\" : \"item\",\n" +
                "  \"targetEntityId\" : \"1\",\n" +
                "  \"properties\" : {\n" +
                "    \"rating\" : 4\n" +
                "  }\n" +
                "}";


        PioEventData refData = new PioEventData();
        refData.entityId = "1";
        refData.entityType = "user";
        refData.event = "rate";
        refData.targetEntityId = "1";
        refData.targetEntityType = "item";


        PioEventData testData = PioEventData.fromJSON(inputJson);

        Assert.assertEquals(refData, testData);
    }

    @Test(expected = IOException.class)
    public void testInvalidJson() throws Exception {

        String inputJson = "{some...crap}";
        PioEventData.fromJSON(inputJson);
    }
}