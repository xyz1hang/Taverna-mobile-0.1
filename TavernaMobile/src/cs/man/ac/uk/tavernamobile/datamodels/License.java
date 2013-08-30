package cs.man.ac.uk.tavernamobile.datamodels;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "license")
public class License extends ElementBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2320989667544646343L;

	@Element
	protected String id;
	
	@Element(name = "unique-name")
	protected String unique_name;
	
	@Element
	protected String title;
	
	@Element
	protected String description;
	
	@Element
	protected String url;
	
	@Element(name = "created-at")
	protected String created_at;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUnique_name() {
		return unique_name;
	}

	public void setUnique_name(String unique_name) {
		this.unique_name = unique_name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
}
