package com.rrockathon.artcollective;

import java.util.Date;

/**
 *  Entity class for Art Events
 * @author ram
 *
 */
public class ArtEventData {
	private String title;
	private String category;
	private String description;
	private String location;
	private String website;
	private String telephone;
    private String name;
    
    public ArtEventData(String name, String title, String category, String description, String location,
			 String website, String telephone) {
		this.title = title;
		this.category = category;
		this.description = description;
		this.location = location;
		this.website = website;
		this.telephone = telephone;
	}
    
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
