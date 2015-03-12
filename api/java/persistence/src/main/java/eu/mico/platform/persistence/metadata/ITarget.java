package eu.mico.platform.persistence.metadata;

import org.openrdf.repository.object.RDFObject;

public interface ITarget extends RDFObject {
    
    public ISelection getSelection();
    
    public void setSelection(ISelection selection);
    
    public void setSource(String source);
    
    public String getSource();
}
