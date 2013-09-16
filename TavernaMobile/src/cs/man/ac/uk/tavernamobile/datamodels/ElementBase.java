package cs.man.ac.uk.tavernamobile.datamodels;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;

public abstract class ElementBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2302471292632857881L;

	@Attribute(required = false)
	protected String resource;
	
	@Attribute(required = false)
	protected String uri;
	
	@Attribute(required = false)
	protected String version;

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
