package eu.mico.platform.zooniverse.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

/**
 * @author Horst Stadler (horst.stadler@salzburgresearch.at)
 * @since 17.05.2016
 */


public class BrokerServices {

    private final String brokerUrl;
    private final String marmottaUrl;

    public BrokerServices(String brokerUrl, String marmottaUrl) {
        this.brokerUrl = brokerUrl;
        this.marmottaUrl = marmottaUrl;
    }

    public ItemCreateResponse createItem(InputStream payload, String filename, String syntacticalType, String mimeType)  throws IOException{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(brokerUrl + "/inject/create?type=" + syntacticalType + "&name=" + filename);
            ContentType contentType;
            if (mimeType != null && mimeType.trim().length() > 0) {
                contentType = ContentType.create(mimeType);
            } else {
                contentType = ContentType.APPLICATION_OCTET_STREAM;
            }
            httpPost.setEntity(new InputStreamEntity(payload, contentType));
            HttpResponse response = httpClient.execute(httpPost);
            checkJsonResponse(response);
            ObjectMapper oMapper = new ObjectMapper();
            return oMapper.readValue(response.getEntity().getContent(), ItemCreateResponse.class);
        }finally {
            httpClient.close();
        }
    }

    public void submitItem(String itemId, int routeId) throws IOException, BrokerException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(brokerUrl + "/inject/submit?item=" + marmottaUrl + "/" + itemId + "&route=" + routeId);
            HttpResponse response = httpClient.execute(httpPost);
            checkResponse(response);
        }finally {
            httpClient.close();
        }
    }

    public List<Item> getItems() throws IOException, BrokerException {
        return getItems(null);
    }

    public Item getItem(String itemId) throws IOException, BrokerException {
        List<Item> items = getItems(itemId);
        if (items.size() == 1) {
            return items.get(0);
        }
        return null;
    }

    private List<Item> getItems(String itemId)  throws IOException, BrokerException{
        String url = brokerUrl + "/status/items?parts=false";
        if (itemId != null) {
            url += "&uri=" + marmottaUrl + "/" + itemId;
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);
            checkJsonResponse(response);
            ObjectMapper oMapper = new ObjectMapper();
            return oMapper.readValue(response.getEntity().getContent(), new TypeReference<List<Item>>() {
            });
        } finally {
            httpClient.close();
        }
    }

    private void checkResponse(HttpResponse response) throws BrokerException {
        int responseStatus = response.getStatusLine().getStatusCode();
        if (responseStatus < 200 || responseStatus > 299) {
            StringWriter brokerMessageWriter = new StringWriter();
            try {
                IOUtils.copy(response.getEntity().getContent(), brokerMessageWriter);
            } catch (IOException ex) { }

            throw new BrokerException("Broker returned an error: " + brokerMessageWriter.toString(), responseStatus);
        }
    }

    private void checkJsonResponse(HttpResponse response) throws BrokerException {
        checkResponse(response);
        String responseType = response.getEntity().getContentType().getValue();
        if (!responseType.trim().equalsIgnoreCase("application/json")) {
            throw new BrokerException("Expected Broker response content type to be application/json but is " + responseType);
        }
    }

    public List<Map<String, String>> getServices() throws IOException {
        List<Map<String, String>> services = new ArrayList<>();
        for (HashMap<String, Object> service : getList(brokerUrl + "/status/services", true)) {
            HashMap<String, String> serviceMap = new HashMap<>();
            for (final String name : service.keySet()) {
                serviceMap.put(name, (String)service.get(name));
            }
            services.add(Collections.unmodifiableMap(serviceMap));
        }
        return Collections.unmodifiableList(services);
    }

    private List<HashMap<String, Object>> getList(String url, boolean notFoundException) throws IOException{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpGet httpGet = new HttpGet(url);

            ResponseHandler<List<HashMap<String, Object>>> responseHandler = new ResponseHandler<List<HashMap<String, Object>>>() {
                @Override
                public List<HashMap<String, Object>> handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                    StatusLine statusLine = response.getStatusLine();
                    int status = statusLine.getStatusCode();
                    if (!notFoundException && status == 404) {
                        return null;
                    } else  if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        if (entity == null) {
                            throw new ClientProtocolException("Response has no content");
                        }
                        JsonFactory jsonF = new JsonFactory();
                        ObjectMapper oMapper = new ObjectMapper(jsonF);
                        TypeReference<List<HashMap<String, Object>>> typeRef = new TypeReference<List<HashMap<String, Object>>>() {};
                        InputStream inStream = entity.getContent();
                        try {
                            return oMapper.readValue(inStream, typeRef);
                        } finally {
                            inStream.close();
                        }
                    } else {
                        throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
                    }
                }
            };
            return httpClient.execute(httpGet, responseHandler);
        } finally {
            httpClient.close();
        }
    }
}
