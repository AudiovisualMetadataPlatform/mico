package eu.mico.platform.persistence.impl.SelectorImpl;

import eu.mico.platform.persistence.impl.ModelPersistenceSelectionImpl;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

@Iri(Ontology.FRAGMENT_SELECTOR_OA)
public class FragmentSelector extends ModelPersistenceSelectionImpl {

    // The x-coordinate of the fragment
    private int xCoord;

    // The y-coordinate of the fragment
    private int yCoord;

    // The width of the fragment
    private int width;

    // The height of the fragment
    private int height;
    
    // String representation of the x-coordinate, the y-coordinate, the width and the height
    @Iri(Ontology.VALUE_RDF)
    private String fragmentData;
    
    @Iri(Ontology.CONFORMS_TO_DCTERMS)
    private final String conformsTo = "http://www.w3.org/TR/mediafrags";
    
    public FragmentSelector() {
    }

    public FragmentSelector(int xCoord, int yCoord, int width, int height) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.width = width;
        this.height = height;
        this.fragmentData = "xywh=" + String.valueOf(xCoord) + "," + String.valueOf(yCoord) + "," + String.valueOf(width) + "," + String.valueOf(height);
    }

    /**
     * Getter for the String representation of the x-coordinate, the y-coordinate, the width and the height in the form:
     * 
     * "x-coordinate, y-coordinate, width, height"
     *  
     * @return
     */
    public String getFragmentData() {
        return fragmentData;
    }
    
    public int getxCoord() {
        return xCoord;
    }
    
    public void setxCoord(int xCoord) {
        this.xCoord = xCoord;
    }
    
    public int getyCoord() {
        return yCoord;
    }

    public void setyCoord(int yCoord) {
        this.yCoord = yCoord;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
