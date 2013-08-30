package cs.man.ac.uk.tavernamobile.datamodels;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "privilege")
public class Privilege implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -138731965952769968L;
	
	@Attribute
	protected String type;

	public String getType() {
		return type;
	}
}
