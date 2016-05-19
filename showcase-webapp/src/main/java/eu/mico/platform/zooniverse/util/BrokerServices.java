package eu.mico.platform.zooniverse.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
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

    /**
     * Queryies the broker using its REST interface about the current status of the given item ID
     *
     * @param itemId        Item ID
     * @return              ItemData object or null if the ID was not known by the broker
     * @throws IOException  Errors occur during HTTP request
     */
    public ItemData getItemData(String itemId) throws IOException {
        List<HashMap<String, Object>> items = getList(brokerUrl + "/status/items?parts=false&uri=" + marmottaUrl + "/" + itemId, false);
        if (items == null || items.size() != 1)
            return null;
        return new ItemData(items.get(0));
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
