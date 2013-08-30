package cs.man.ac.uk.tavernamobile.datamodels;

import java.io.Serializable;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "tag")
public class Tag extends ElementBase implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5801667215071830478L;
	
	@Text
	protected String value;

	public String getValue() {
		return value;
	}

}
