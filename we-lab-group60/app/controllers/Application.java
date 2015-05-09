package controllers;

import play.data.Form;
import play.mvc.*;
import views.html.*;
import models.ComplexUser;

public class Application extends Controller {

	final static Form<ComplexUser> signinForm = Form.form(ComplexUser.class);
	
	public static Result index() {
		return ok(authentication.render(signinForm));
	}

}