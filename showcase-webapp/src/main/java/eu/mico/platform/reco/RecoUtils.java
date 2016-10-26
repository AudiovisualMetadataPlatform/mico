package eu.mico.platform.reco;

import eu.mico.platform.reco.Resources.SimpleExtractionResult;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class RecoUtils {
    public static String getCmdOutput(String cmdLine) {
        String response = "";

        try {
            Process p_docker = Runtime.getRuntime().exec(cmdLine);
            p_docker.waitFor();
            InputStream respStream = p_docker.getInputStream();

            if (p_docker.exitValue() != 0) {
                respStream = p_docker.getErrorStream();
            }
            response += IOUtils.toString(respStream, "UTF-8");

        } catch (IOException | InterruptedException e) {
            return "Error execurint command:" + e.getMessage();
        }

        return response;
    }

    public static String getDockerCmd(String psauxOutput) {
        Matcher m = Pattern.compile("(?m)^.*docker.*$").matcher(psauxOutput);
        if (m.find()) {
            return m.group();
        } else {
            return null;
        }
    }


    public static String forwardGET(URI requestUri) throws IOException {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet getRequest = new HttpGet(requestUri);

        CloseableHttpResponse getResponse = httpclient.execute(getRequest);

        HttpEntity entity = getResponse.getEntity();
        if (entity != null) {
            return IOUtils.toString(entity.getContent(), "UTF-8");
        } else {
            throw new IOException("Invalid response");
        }

    }


    public static String forwardPOST(URI recopath, String data) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost postRequest = new HttpPost(recopath);
        postRequest.setEntity(new StringEntity(data, "UTF-8"));

        CloseableHttpResponse postResponse = httpclient.execute(postRequest);

        HttpEntity entity = postResponse.getEntity();
        if (entity != null) {
            return IOUtils.toString(entity.getContent(), "UTF-8");
        } else {
            throw new IOException("Invalid response");
        }

    }

    public static int countOverlappingEntites(
            List<? extends SimpleExtractionResult> list1,
            List<? extends SimpleExtractionResult> list2) {

        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();

        for (SimpleExtractionResult ser : list1) {
            //strip fusepool namespace
            set1.add(ser.getLabel().substring(ser.getLabel().lastIndexOf("#") + 1));
        }
        for (SimpleExtractionResult ser : list2) {
            set2.add(ser.getLabel());
        }

        set1.retainAll(set2);
        return set1.size();

    }

    public static double getMedian(double... d) {


        Arrays.sort(d);
        int length = d.length;

        double median = 0;

        if (length % 2 == 1) {
            median = d[(length - 1) / 2];
        } else {
            double s1 = d[length / 2];
            double s2 = d[length / 2 - 1];

            median = (s1 + s2) / 2;
        }

        return median;

    }
}
