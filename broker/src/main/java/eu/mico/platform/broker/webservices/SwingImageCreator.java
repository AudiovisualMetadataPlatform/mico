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
package eu.mico.platform.broker.webservices;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.JGraphModelFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;
import com.jgraph.layout.organic.JGraphFastOrganicLayout;
import com.jgraph.layout.organic.JGraphOrganicLayout;
import com.jgraph.layout.simple.SimpleGridLayout;
import org.jgraph.JGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphModelAdapter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SwingImageCreator {

    /**
     * Creates a buffered image of type TYPE_INT_RGB
     * from the supplied component. This method will
     * use the preferred size of the component as the
     * image's size.
     * @param component the component to draw
     * @return an image of the component
     */
    public static BufferedImage createImage(JComponent component){
        return createImage(component, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Creates a buffered image (of the specified type)
     * from the supplied component. This method will use
     * the preferred size of the component as the image's size
     * @param component the component to draw
     * @param imageType the type of buffered image to draw
     *
     * @return an image of the component
     */
    public static BufferedImage createImage(JComponent component,  int imageType){
        Dimension componentSize = component.getPreferredSize();
        component.setSize(componentSize); //Make sure these
        //are the same
        BufferedImage img = new BufferedImage(componentSize.width,
                componentSize.height,
                imageType);
        Graphics2D grap = img.createGraphics();
        grap.fillRect(0,0,img.getWidth(),img.getHeight());
        component.paint(grap);
        return img;
    }

    /**
     * Create a buffered image and write it to the given output stream using the given image type.
     *
     * @param component
     * @param type
     * @param out
     * @throws IOException
     */
    public static void createImage(JComponent component, String type, OutputStream out) throws IOException {
        BufferedImage img = createImage(component);
        ImageIO.write(img, type, out);
    }


    public static <V,E> void createGraph(Graph<V,E> g, Dimension size, String type, OutputStream out) throws IOException {


        JGraphModelAdapter<V,E> model = new JGraphModelAdapter<V, E>(g);
        JGraph graph = new JGraph(model);
        graph.setPreferredSize(size);
        graph.setSize(size);

        BufferedImage img = new BufferedImage(size.width,size.height,BufferedImage.TYPE_INT_RGB);
        Graphics2D grap = img.createGraphics();
        grap.fillRect(0,0,img.getWidth(),img.getHeight());

        if(g.edgeSet().size() > 0) {
            graph.paint(grap);


            graph.setBackground(Color.white);
            graph.setEditable(false);
            graph.setGridVisible(false);

            JGraphFacade facade = new JGraphFacade(graph);
            JGraphFastOrganicLayout layout = new JGraphFastOrganicLayout();
            layout.run(facade);
            facade.scale(new Rectangle(size.width, size.height));
            graph.getGraphLayoutCache().edit(facade.createNestedMap(true, true));

            graph.paint(grap);
        }

        ImageIO.write(img, type, out);
    }
}
