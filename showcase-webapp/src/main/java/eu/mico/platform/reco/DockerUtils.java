package eu.mico.platform.reco;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
public class DockerUtils {
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
        Matcher m = Pattern.compile("(?m)^^.*docker run.*wp5$$").matcher(psauxOutput);
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
}
