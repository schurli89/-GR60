package controllers;

import play.mvc.*;

import views.html.authentication;
import models.ComplexUser;
import play.cache.Cache;
import play.data.DynamicForm;
import play.data.Form;

public class JeopardyAuthenticator extends Security.Authenticator {
	
	public JeopardyAuthenticator(){
	    super();
	}
	
	public String getUsername(Http.Context ctx){
        return ctx.session().get("user");
	}
	
	public Result onUnauthorized(Http.Context ctx) {
	    return ok(authentication.render(Form.form(ComplexUser.class)));
	}

}
