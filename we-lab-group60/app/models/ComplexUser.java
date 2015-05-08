package models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import at.ac.tuwien.big.we15.lab2.api.Avatar;
import play.data.validation.*;

@Entity
public class ComplexUser extends at.ac.tuwien.big.we15.lab2.api.impl.SimpleUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String firstname;
	private String avatar_name;
	private String lastname;

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
	public ComplexUser() {
	};

	/*public ComplexUser(String firstname, String lastname, Date birthdate,
			String gender, String username, String password) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.birthdate = birthdate;
		this.gender = gender;
		this.name = username;
		this.password = password;
		
	}*/

	public ComplexUser(String firstname, String lastname, Date birthdate,
			String gender, String username, String password, String avatar_name) {
		System.out.println("USER CONSTRUCTOR");
		this.firstname = firstname;
		this.lastname = lastname;
		this.birthdate = birthdate;
		this.gender = gender;
		this.name = username;
		this.password = password;
		this.avatar_name = avatar_name;
		setAvatar(Avatar.getAvatar(avatar_name));
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
		
		return "Username: "+ name + "\nPasswort: " + password + "\nGeschlecht: " + gender + "\nAvatar: "+ avatar_name;
		
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name=name;
		
	}
	
	public String getAvatar_name(){
		return this.avatar_name;
	}
	
	public void setAvatar_name(String avatar_name){
		this.avatar_name = avatar_name;
	}
}
