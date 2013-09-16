package cs.man.ac.uk.tavernamobile.datamodels;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "group")
public class CreditGroup extends ElementBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8150161193328889242L;
	
	@Text
	protected String value;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
