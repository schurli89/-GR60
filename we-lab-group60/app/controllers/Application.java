package controllers;

import models.ComplexUser;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.authentication;

public class Application extends Controller {

	final static Form<ComplexUser> signinForm = Form.form(ComplexUser.class);
	
	public static Result index() {
		return ok(authentication.render(signinForm));
	}

}