package models;

import java.util.Date;

import javax.persistence.*;

import at.ac.tuwien.big.we15.lab2.api.Avatar;
import play.data.validation.*;

@Entity
public class User implements at.ac.tuwien.big.we15.lab2.api.User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String firstname;
	
	private String lastname;
	
	private Avatar avatar;

	@Temporal(TemporalType.DATE)
	private Date birthdate;

	private String gender;

	@Constraints.Required
	@Constraints.MinLength(4)
	@Constraints.MaxLength(8)
	private String name;

	@Constraints.Required
	@Constraints.MinLength(4)
	@Constraints.MaxLength(8)
	private String password;

	// needs a default constructor for the views
	public User() {
	};

	public User(String firstname, String lastname, Date birthdate,
			String gender, String username, String password) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.birthdate = birthdate;
		this.gender = gender;
		this.name = username;
		this.password = password;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String surname) {
		this.lastname = surname;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String toString(){
		
		return "Username: "+ name + "\nPasswort: " + password + "\nGeschlecht: " + gender;
		
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name=name;
		
	}

	public Avatar getAvatar() {
		return this.avatar;
	}

	public void setAvatar(Avatar avatar) {
		this.avatar=avatar;
	}


	
}
