package cs.man.ac.uk.tavernamobile.datamodels;

import java.io.Serializable;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "rating")
public class Rating extends ElementBase implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8634382152297708592L;
	@Text
	protected String value;

	public String getValue() {
		return value;
	}
}
