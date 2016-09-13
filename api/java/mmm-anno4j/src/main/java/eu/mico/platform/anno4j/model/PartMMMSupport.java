package eu.mico.platform.anno4j.model;

import com.github.anno4j.annotations.Partial;
import com.github.anno4j.model.Agent;
import com.github.anno4j.model.Body;
import com.github.anno4j.model.Motivation;
import com.github.anno4j.model.Target;
import com.github.anno4j.model.impl.ResourceObjectSupport;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.rio.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Support class for the Part.
 */
@Partial
public abstract class PartMMMSupport extends ResourceObjectSupport implements PartMMM {

    @Override
    /**
     * {@inheritDoc}
#     */
    public void addInput(ResourceMMM input) {
        this.getInputs().add(input);
    }

    public void addTarget(Target target) {
        if(this.getTarget() == null) {
            this.setTarget(new HashSet());
        }

        this.getTarget().add(target);
    }

    public String getTriples(RDFFormat format) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RDFParser parser = Rio.createParser(RDFFormat.NTRIPLES);
        RDFWriter writer = Rio.createWriter(format, out);
        parser.setRDFHandler(writer);

        try {
            StringBuilder e = new StringBuilder();
            e.append(super.getTriples(RDFFormat.NTRIPLES));
            if(this.getBody() != null) {
                e.append(this.getBody().getTriples(RDFFormat.NTRIPLES));
            }

            if(this.getTarget() != null) {
                Iterator i$ = this.getTarget().iterator();

                while(i$.hasNext()) {
                    Target target = (Target)i$.next();
                    e.append(target.getTriples(RDFFormat.NTRIPLES));
                }
            }

            if(this.getAnnotatedBy() != null) {
                e.append(this.getAnnotatedBy().getTriples(RDFFormat.NTRIPLES));
            }

            if(this.getSerializedBy() != null) {
                e.append(this.getSerializedBy().getTriples(RDFFormat.NTRIPLES));
            }

            if(this.getMotivatedBy() != null) {
                e.append(this.getMotivatedBy().getTriples(RDFFormat.NTRIPLES));
            }

            parser.parse(IOUtils.toInputStream(e.toString()), "");
        } catch (RDFHandlerException | RDFParseException | IOException var8) {
            var8.printStackTrace();
        }

        return out.toString();
    }

    public void setSerializedAt(int year, int month, int day, int hours, int minutes, int seconds) {
        StringBuilder builder = new StringBuilder();
        builder.append(Integer.toString(year)).append("-").append(Integer.toString(month)).append("-").append(Integer.toString(day)).append("T");
        if(hours < 10) {
            builder.append(0);
        }

        builder.append(Integer.toString(hours));
        builder.append(":");
        if(minutes < 10) {
            builder.append(0);
        }

        builder.append(Integer.toString(minutes));
        builder.append(":");
        if(seconds < 10) {
            builder.append(0);
        }

        builder.append(Integer.toString(seconds));
        builder.append("Z");
        this.setSerializedAt(builder.toString());
    }

    public void setAnnotatedAt(int year, int month, int day, int hours, int minutes, int seconds) {
        StringBuilder builder = new StringBuilder();
        builder.append(Integer.toString(year)).append("-").append(Integer.toString(month)).append("-").append(Integer.toString(day)).append("T");
        if(hours < 10) {
            builder.append(0);
        }

        builder.append(Integer.toString(hours));
        builder.append(":");
        if(minutes < 10) {
            builder.append(0);
        }

        builder.append(Integer.toString(minutes));
        builder.append(":");
        if(seconds < 10) {
            builder.append(0);
        }

        builder.append(Integer.toString(seconds));
        builder.append("Z");
        this.setAnnotatedAt(builder.toString());
    }

    public void delete() {
        try {
            ObjectConnection e = this.getObjectConnection();
            if(this.getBody() != null) {
                this.getBody().delete();
                this.setBody((Body)null);
            }

            if(this.getTarget() != null) {
                Iterator i$ = this.getTarget().iterator();

                while(i$.hasNext()) {
                    Target target = (Target)i$.next();
                    target.delete();
                }

                this.setTarget((Set)null);
            }

            this.setAnnotatedBy((Agent)null);
            this.setAnnotatedAt((String)null);
            this.setMotivatedBy((Motivation)null);
            this.setSerializedBy((Agent)null);
            this.setSerializedAt((String)null);
            e.removeDesignation(this, (URI)this.getResource());
            e.remove(this.getResource(), (URI)null, (Value)null, new Resource[0]);
        } catch (RepositoryException var4) {
            var4.printStackTrace();
        }

    }
}
