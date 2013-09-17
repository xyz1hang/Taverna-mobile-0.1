package cs.man.ac.uk.tavernamobile.datamodels;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "file")
public class FileBrief extends ElementBase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5654608790373965991L;
	@Text
	protected String value;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
