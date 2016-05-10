package eu.mico.platform.camel;

import java.io.IOException;
import java.net.ConnectException;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Represents a MicoRabbit endpoint.
 */
@ManagedResource(description = "Managed MicoRabbitMQEndpoint")
@UriEndpoint(scheme = "mico-comp", title = "MicoRabbit", syntax="mico-comp:name", consumerClass = MicoRabbitConsumer.class, label = "MicoRabbit")
public class MicoRabbitEndpoint extends DefaultEndpoint {

    @Override
    public boolean isLenientProperties() {
        return true;
    }

    @UriPath(name=":name") @Metadata(required = "true")
    private String name;
    @UriParam(defaultValue = "10")
    private int option = 10;

    @UriParam(name="host", defaultValue = "mico-box")
    private String host = "mico-box";
    @UriParam(name="user", defaultValue = "mico")
    private String user = "mico";
    @UriParam(defaultValue = "mico")
    private String password = "mico";
    @UriParam(defaultValue = "5672", description = "the port where the rabbitmq broker listens for connections")
    private int rabbitPort = 5672;

    @UriParam(name="modeId")
    @Metadata(required = "true")
    private String modeId ="";

    @UriParam(name="extractorVersion")
    @Metadata(required = "true")
    private String extractorVersion = "0.0.0";

    @UriParam(name="parameters")
    @Metadata(required = "false")
    private String parameters;

    @UriParam(name="extractorId")
    @Metadata(required = "true")
    private String extractorId;

    
    private Connection connection = null;

    
    public MicoRabbitEndpoint() {
    }

    public MicoRabbitEndpoint(String uri, MicoRabbitComponent component) {
        super(uri, component);
    }

    public Producer createProducer() throws Exception {
        return new MicoRabbitProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new MicoRabbitConsumer(this, processor);
    }

    public boolean isSingleton() {
        return true;
    }

	public Connection getConnection() throws IOException {
	    if (connection == null){
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(getHost());
            factory.setPort(getRabbitPort());
            factory.setUsername(getUser());
            factory.setPassword(getPassword());
    
            try{
                connection = factory.newConnection();
            }catch(ConnectException e){
                e.printStackTrace();
            }
	    }
	    
        return connection;
	}

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setOption(int option) {
        this.option = option;
    }

    public int getOption() {
        return option;
    }

	public int getRabbitPort() {
		return rabbitPort;
	}

	public void setRabbitPort(int rabbitPort) {
		this.rabbitPort = rabbitPort;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

    /**
     * each extractor service has is own id, in rabbitmq this id is used a routing key
     */
    public String getQueueId() {
        return getExtractorId() + "-" +getExtractorVersion()+ "-" + getModeId();
    }

    public String getModeId() {
        return modeId;
    }

    public void setModeId(String mode) {
        this.modeId = mode;
    }

    public String getExtractorVersion() {
        return extractorVersion;
    }

    public void setExtractorVersion(String extractorVersion) {
        this.extractorVersion = extractorVersion;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getExtractorId() {
        return extractorId;
    }

    public void setExtractorId(String extractorId) {
        this.extractorId = extractorId;
    }


}
