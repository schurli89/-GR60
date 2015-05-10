package controllers;

import models.ComplexUser;
import play.data.Form;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import views.html.authentication;

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
