package cs.man.ac.uk.tavernamobile.datamodels;

import java.io.Serializable;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "user")
public class Credit extends ElementBase implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7953085921577181968L;
	@Text
	protected String value;

	public String getValue() {
		return value;
	}
}
