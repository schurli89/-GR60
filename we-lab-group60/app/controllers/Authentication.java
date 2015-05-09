package controllers;

import javax.persistence.TypedQuery;

import models.ComplexUser;
import play.cache.Cache;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.authentication;
import views.html.jeopardy;

import  at.ac.tuwien.big.we15.lab2.api.*;
import  at.ac.tuwien.big.we15.lab2.api.impl.*;

public class Authentication extends Controller {

	final static Form<ComplexUser> user = Form.form(ComplexUser.class);

	@Transactional
	public static Result login() {

		Form<ComplexUser> form = user.bindFromRequest();

		String username = form.field("name").value().trim();
		String password = form.field("password").value().trim();

		if (!username.matches(".{4,8}")) {
			form.reject("usernameError", "Benutzername ungültig.");
		}

		if (!password.matches(".{4,8}")) {
			form.reject("passwordError", "Passwort ungültig.");
		}

		if (form.hasErrors()) {
			return badRequest(authentication.render(form));
		}

		String queryString = "SELECT u FROM ComplexUser u where u.name = '"
				+ username + "' and u.password = '" + password + "'";
		TypedQuery<ComplexUser> query = play.db.jpa.JPA.em().createQuery(queryString,
				ComplexUser.class);

		ComplexUser user = null;

		if (!query.getResultList().isEmpty()) {
			user = query.getResultList().get(0);
		}

		if (user == null) {
			form.reject("authenticationError",
					"Benutzername und/oder Passwort falsch.");
			return badRequest(authentication.render(form));
		}

		System.out.println("LOCAL ID: " + user.getId()); // TEST
		System.out.println("LOG: USER: "+user.toString());
		session().clear();
		session("user", username);

		initCache(user);
		
		return redirect(controllers.routes.Jeopardy.start(user.getName()));
	}

	public static Result logout() {
		session().clear();
		return Application.index();
	}

	private static void initCache(ComplexUser user) {

		String uuid = session("uuid");
		if (uuid == null) {
			uuid = java.util.UUID.randomUUID().toString();
			session("uuid", uuid);
		}

		ComplexUser cacheUser = (ComplexUser) Cache.get(uuid + "user");
		if (cacheUser == null) {
			Cache.set("uuid", uuid);
			Cache.set(uuid + "user", user);
		}
	}
}
