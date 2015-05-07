package controllers;

import javax.persistence.TypedQuery;

import models.ComplexUser;
import play.api.i18n.Lang;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.authentication;
import views.html.registration;

public class Registration extends Controller {

	final static Form<ComplexUser> signupForm = Form.form(ComplexUser.class);
	final static String datePattern = "^(0?[1-9]|[12][0-9]|3[01]).(0?[1-9]|1[012]).((19|20)\\d\\d)$";
	
	public static Result registration() {
		return ok(registration.render(signupForm));
	}
	
	@Transactional
	public static Result register() {
		Form<ComplexUser> form = signupForm.bindFromRequest();
		
		if(!form.field("birthdate").valueOr("").isEmpty()){
			if (!form.field("birthdate").value().matches(datePattern)){
				form.reject("birthdateError", "Verwenden Sie bitte folgendes Datumsformat: dd.mm.yyyy (z.B. 24.12.2012).");
			}
		}
		
		if (!form.field("name").value().matches(".{4,8}")){
			form.reject("usernameError", "Der Benutzername muss mindestens 4 Zeichen und darf maximal 8 Zeichen enthalten.");
		}
		
		if (!form.field("password").value().matches(".{4,8}")){
			form.reject("passwordError", "Das Passwort muss mindestens 4 Zeichen und darf maximal 8 Zeichen enthalten.");
		}
		
		
		if(form.hasErrors()) {
            return badRequest(registration.render(form));
        } else{
        	
        	ComplexUser user = form.get();
        	
        	String queryString = "SELECT u FROM User u where u.name = '" + user.getName() + "'";
    		TypedQuery<ComplexUser> query = play.db.jpa.JPA.em().createQuery(queryString, ComplexUser.class);
        	
    		if(!query.getResultList().isEmpty()){
    			form.reject("userExist", "User existiert bereits.");
    			
    			System.out.println(query.getResultList().get(0));
    			
    			
    			return badRequest(registration.render(form));
    		}
    		else{
    			JPA.em().persist(user);
    		}
        }
		

		System.out.println("ENGLISH: "+Messages.get(new Lang("en", "US"), "user.gender"));	// TEST
		System.out.println("DEUTSCH: "+Messages.get(new Lang("de", "AT"), "user.gender"));	// TEST
		
		return ok(authentication.render(signupForm));
		
	}
}
