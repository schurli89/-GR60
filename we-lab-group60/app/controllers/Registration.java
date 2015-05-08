package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.SimpleFormatter;

import javax.persistence.TypedQuery;

import at.ac.tuwien.big.we15.lab2.api.Avatar;
import models.ComplexUser;
import play.api.i18n.Lang;
import play.api.libs.iteratee.Input.El;
import play.data.Form;
import play.data.format.Formatters;
import play.data.validation.ValidationError;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.authentication;
import views.html.registration;
import play.data.*;

public class Registration extends Controller {

	final static Form<ComplexUser> signupForm = Form.form(ComplexUser.class);
	final static String datePattern = "^(0?[1-9]|[12][0-9]|3[01]).(0?[1-9]|1[012]).((19|20)\\d\\d)$";
	
	public Registration(){
	}
	
	public static Result registration() {
		return ok(registration.render(signupForm));
	}
	

	@Transactional
	public static Result register() {
		Form<ComplexUser> form = signupForm.bindFromRequest();
		
		System.out.println("LOG: New Registration request");
		System.out.println("LOG GENDER: " + form.field("gender").value());
		System.out.println("LOG avatar: " + form.field("avatar").value());
		
		if(!form.field("birthdate").valueOr("").isEmpty()){
			if (!form.field("birthdate").value().matches(datePattern)){
				form.reject("birthdateError", "");
			}
		}
		
		if (!form.field("name").value().matches(".{4,8}")){
			form.reject("usernameError", "");
		}
		
		if (!form.field("password").value().matches(".{4,8}")){
			form.reject("passwordError", "");
		}
		
		
		if(form.hasErrors()) {
			
			for(Entry<String, List<ValidationError>> e : form.errors().entrySet()){
				System.out.println("LOG has errors: " + e.getKey() + " , " + e.getValue());
			}
			return badRequest(registration.render(form));
        } else{
        	
        	ComplexUser user = form.get();
        	
        	String queryString = "SELECT u FROM ComplexUser u where u.name = '" + user.getName() + "'";
    		TypedQuery<ComplexUser> query = play.db.jpa.JPA.em().createQuery(queryString, ComplexUser.class);
        	
    		if(!query.getResultList().isEmpty()){
    			form.reject("userExist", "");
    			
    			System.out.println("LOG reject" + query.getResultList().get(0));
    			
    			
    			return badRequest(registration.render(form));
    		}
    		else{
    			JPA.em().persist(user);
    			System.out.println("LOG persist: " + user);
    		}
        }
		System.out.println("LOG: redirect to authentication");
		return ok(authentication.render(signupForm));
		
	}
}
