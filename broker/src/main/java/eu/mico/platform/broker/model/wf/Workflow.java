package eu.mico.platform.broker.model.wf;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;

@Entity
@NamedQueries(value = {
        @NamedQuery(name=Workflow.QUERY_WORKFLOW_IDS_BY_USER, query="Select w.id from Workflow w where w.user = :user" )
})
@Table( /*uniqueConstraints=@UniqueConstraint(columnNames={"workflowName"})*/ )
public class Workflow{

    private static final long   serialVersionUID = 1L;
    public  static final String QUERY_WORKFLOW_IDS_BY_USER = "Workflow.queryIdByUser";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String user;
    @Column(name="workflowName", nullable=false)
    private String name;
    @Type(type="text")
    private String route;
    @Type(type="text")
    private String links;
    @Type(type="text")
    private String nodes;
    
    public Workflow() {
    }
    
    public Workflow(String user, String name, String route,
            String links, String nodes) {
        this.user = user;
        this.name = name;
        this.route = route;
        this.links = links;
        this.nodes = nodes;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getRoute() {
        return route;
    }
    public void setRoute(String route) {
        this.route = route;
    }
    public String getLinks() {
        return links;
    }
    public void setLinks(String links) {
        this.links = links;
    }
    public String getNodes() {
        return nodes;
    }
    public void setNodes(String nodes) {
        this.nodes = nodes;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
}
