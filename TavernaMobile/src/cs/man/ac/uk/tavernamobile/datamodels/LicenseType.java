package cs.man.ac.uk.tavernamobile.datamodels;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "license-type")
public class LicenseType extends ElementBase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7589369608355675772L;
	
	@Text
	protected String value;

	public String getValue() {
		return value;
	}
}
