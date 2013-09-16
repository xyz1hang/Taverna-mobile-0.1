package cs.man.ac.uk.tavernamobile.datamodels;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "user")
public class CreditUser extends ElementBase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5172493207959592922L;
	@Text
	protected String value;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
