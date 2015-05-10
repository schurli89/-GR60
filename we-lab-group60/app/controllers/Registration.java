package controllers;

import java.util.List;
import java.util.Map.Entry;

import javax.persistence.TypedQuery;

import models.ComplexUser;
import play.data.Form;
import play.data.validation.ValidationError;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.authentication;
import views.html.registration;

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
			return badRequest(registration.render(form));
        } else{
        	
        	ComplexUser user = form.get();
        	String queryString = "SELECT u FROM ComplexUser u where u.name = '" + user.getName() + "'";
    		TypedQuery<ComplexUser> query = play.db.jpa.JPA.em().createQuery(queryString, ComplexUser.class);
        	
    		if(!query.getResultList().isEmpty()){
    			form.reject("userExist", "");				
    			return badRequest(registration.render(form));
    		}
    		else{
    			JPA.em().persist(user);
    		}
        }
		return ok(authentication.render(signupForm));
		
	}
}
