package controllers;

import play.data.Form;
import play.mvc.*;
import views.html.*;
import models.User;

public class Application extends Controller {

	final static Form<User> signinForm = Form.form(User.class);
	
	public static Result index() {
		return ok(authentication.render(signinForm));
	}
}