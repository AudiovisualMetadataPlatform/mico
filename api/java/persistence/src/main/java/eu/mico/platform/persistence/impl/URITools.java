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

package eu.mico.platform.persistence.impl;

import java.net.URI;
import java.net.URISyntaxException;

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

    /**
     * Test if the given ID consists of invalid characters (?, #, /)
     *
     * @param id The id to check
     * @return True if the ID is valid.
     */
    public static boolean validID(String id) {
        char[] invalidChars = {'/', '?', '#'};

        for (char c :invalidChars) {
            if (id.indexOf(c) != -1)
                return false;
        }
        return true;
    }

    /**
     * Test if the given URI starts with the baseURI
     *
     * @param testURI The URI to test.
     * @param baseURI The base URI.
     * @return True, if the testURI starts with the baseURI, false otherwise.
     */
    public static boolean validBaseURI(String testURI, String baseURI) {
        if(splitURI(testURI, baseURI) == null)
            return false;
        return true;
    }

    /**
     * Test if the given URI addresses a content item.
     *
     * @param testURI The URI to test.
     * @param baseURI The base URI.
     * @return True, if the testURI exactly adresses a content item, false otherwise.
     */
    public static boolean validContentItemURI(String testURI, String baseURI) {
        String[] uriParts = splitURI(testURI, baseURI);
        if(uriParts != null && uriParts.length == 1 && !uriParts[0].isEmpty())
            return true;
        return false;
    }

    /**
     * Test if the given URI adresses a content part.
     *
     * @param testURI The URI to test.
     * @param baseURI The base URI.
     * @return True, if the testURI exactly adresses a content part, false otherwise.
     */
    public static boolean validContentPartURI(String testURI, String baseURI) {
        String[] uriParts = splitURI(testURI, baseURI);
        if(uriParts != null && uriParts.length == 2 && !uriParts[0].isEmpty() && !uriParts[1].isEmpty())
            return true;
        return false;
    }

    /**
     *
     * Get the content item ID of the given URI.
     *
     * @param uri The URI
     * @param baseURI The base URI.
     * @return The content item ID, or null if it doesn't contain it.
     */
    public static String getContentItemID(String uri, String baseURI) {
        String[] uriParts = splitURI(uri, baseURI);
        if (uriParts == null || uriParts.length < 1 || uriParts[0].isEmpty())
            return null;
        return uriParts[0];
    }

    /**
     *
     * Get the content part ID of the given URI.
     *
     * @param uri The URI
     * @param baseURI The base URI.
     * @return The content part ID, or null if it doesn't contain it.
     */
    public static String getContentPartID(String uri, String baseURI) {
        String[] uriParts = splitURI(uri, baseURI);
        if (uriParts == null || uriParts.length < 2 || uriParts[0].isEmpty() || uriParts[1].isEmpty())
            return null;
        return uriParts[1];
    }
    /**
     *
     * @param uri The URI to split
     * @param baseURI The base URI
     * @return null, if the uri does not start with the baseURI
     *         String[0], if uri does not contain a content item or content ID.
     *         String[1], with the content item ID
     *         String[2], with the content item ID (index 0) and the content ID (index 1)
     *         String[3], with the content item ID (index 0), the content ID (index 1) and the rest of the path (index 2)
     */
    private static String[] splitURI(String uri, String baseURI) {
        if (!uri.startsWith(baseURI))
            return null;

        String path = uri.substring(baseURI.length());
        path = path.split("\\?", 2)[0]; // skip anything after a query string
        path = path.split("#", 2)[0]; // skip anything after a fragment identifier
        path = path.replaceAll("^/*", ""); //remove leading slashes
        path = path.replaceAll("/*$", ""); //remove trailing slashes

        String parts[] =  path.split("/", 3);

        if (parts.length == 1 && parts[0].isEmpty())
            return new String[0];

        return parts;
    }
}
