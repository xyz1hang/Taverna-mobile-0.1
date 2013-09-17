package cs.man.ac.uk.tavernamobile.datamodels;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "pack")
public class PackBrief extends ElementBase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9140827762152166802L;
	
	@Text
	protected String value;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
