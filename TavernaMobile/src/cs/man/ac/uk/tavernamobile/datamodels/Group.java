package cs.man.ac.uk.tavernamobile.datamodels;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "group")
public class Group extends ElementBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4733591207568245834L;

	@Element(required = false)
	protected String id;
	
	@Element(name = "created-at")
	protected String created_at;
	
	@Element(required = false)
	protected String new_member_policy;
	
	@Element(required = false)
	protected String description;
	
	@Element(required = false)
	protected String title;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNew_member_policy() {
		return new_member_policy;
	}

	public void setNew_member_policy(String new_member_policy) {
		this.new_member_policy = new_member_policy;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
