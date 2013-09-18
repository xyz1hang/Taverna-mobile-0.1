package cs.man.ac.uk.tavernamobile.datamodels;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "user")
public class User extends ElementBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3467195671046297377L;

	@Element(required = false)
	protected String id;
	
	@Element(name = "created-at", required = false)
	protected String created_at;
	
	@Element(required = false)
	protected String name;
	
	@Element(required = false)
	protected String description;
	
	@Element(required = false)
	protected String email;
	
	@Element(required = false)
	protected Avatar avatar;
	
	@Element(required = false)
	protected String city;
	
	@Element(required = false)
	protected String country;
	
	@Element(required = false)
	protected String website;
	
	@Element(required = false)
	protected UserFavourited favourited;
	
	@Element(required = false)
	protected WorkflowBriefCollection workflows;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Avatar getAvatar() {
		return avatar;
	}

	public void setAvatar(Avatar avatar) {
		this.avatar = avatar;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public UserFavourited getFavourited() {
		return favourited;
	}

	public void setFavourited(UserFavourited favourited) {
		this.favourited = favourited;
	}

	public WorkflowBriefCollection getWorkflows() {
		return workflows;
	}

	public void setWorkflows(WorkflowBriefCollection workflows) {
		this.workflows = workflows;
	}
}
