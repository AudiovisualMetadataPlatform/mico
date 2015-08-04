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
    @UriPath @Metadata(required = "true")
    private String name;
    @UriParam(defaultValue = "10")
    private int option = 10;

    @UriParam(name="host", defaultValue = "10.129.40.216")
    private String host = "10.129.40.216";
    @UriParam(name="user", defaultValue = "mico")
    private String user = "mico";
    @UriParam(defaultValue = "mico")
    private String password = "mico";
    @UriParam(defaultValue = "5672")
    private int rabbitPort = 5672;
    
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

}
