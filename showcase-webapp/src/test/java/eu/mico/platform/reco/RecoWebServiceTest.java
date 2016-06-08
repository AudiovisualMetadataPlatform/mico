package eu.mico.platform.reco;

import eu.mico.platform.testutils.TestServer;
import org.junit.BeforeClass;

import static eu.mico.platform.testutils.Mockups.mockEvenmanager;

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class RecoWebServiceTest {


    static private TestServer server;

    @BeforeClass
    public static void init() throws Exception {


        RecoWebService recoWebService = new RecoWebService(
                mockEvenmanager(null),
                "http://mico-platform:8080/marmotta"
        );

           //init server
        server = new TestServer();

        server.addWebservice(recoWebService);

        server.start();
    }
}
