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

package eu.mico.platform.reco;

import org.junit.Assert;
import org.junit.Test;

public class RecoUtilsTest {
    @Test
    public void testGrepper() throws Exception {
        String psaux = "USER       PID %CPU %MEM    VSZ   RSS TTY      STAT START   TIME COMMAND\n" +
                "root         1  0.1  0.1  28724  4960 ?        Ss   18:43   0:00 /sbin/init\n" +
                "root         2  0.0  0.0      0     0 ?        S    18:43   0:00 [kthreadd]\n" +
                "root         3  0.0  0.0      0     0 ?        S    18:43   0:00 [ksoftirqd/0]\n" +
                "root         5  0.0  0.0      0     0 ?        S<   18:43   0:00 [kworker/0:0H]\n" +
                "root         6  0.0  0.0      0     0 ?        S    18:43   0:00 [kworker/u2:0]\n" +
                "root         7  0.0  0.0      0     0 ?        S    18:43   0:00 [rcu_sched]\n" +
                "user      1960  0.0  0.7 168900 28528 pts/0    Sl+  18:57   0:00 docker run -p 7070:7070 -p 9000:9000 wp5\n" +
                "statd      489  0.0  0.0  37280  3016 ?        Ss   18:43   0:00 /sbin/rpc.statd\n" +
                "root       494  0.0  0.0      0     0 ?        S<   18:43   0:00 [rpciod]\n" +
                "root       496  0.0  0.0      0     0 ?        S<   18:43   0:00 [nfsiod]\n" +
                "root       503  0.0  0.0  23356   204 ?        Ss   18:43   0:00 /usr/sbin/rpc.idmapd\n" +
                "daemon     506  0.0  0.0  19024  1796 ?        Ss   18:43   0:00 /usr/sbin/atd -f\n";


        Assert.assertNotNull(RecoUtils.getDockerCmd(psaux));
    }

    @Test
    public void testMedian() throws Exception {

        double med = RecoUtils.getMedian(25d);
        Assert.assertEquals(25, med, 0.01d);


        med = RecoUtils.getMedian(0d, 1d, -1d);
        Assert.assertEquals(0, med, 0.01d);

        med = RecoUtils.getMedian(1.0, 2.0, 3.0, 4.0);
        Assert.assertEquals(2.5, med, 0.01d);


    }
}