package controllers;

import javax.persistence.TypedQuery;

import models.ComplexUser;
import at.ac.tuwien.big.we15.lab2.api.JeopardyFactory;
import at.ac.tuwien.big.we15.lab2.api.JeopardyGame;
import at.ac.tuwien.big.we15.lab2.api.Question;
import at.ac.tuwien.big.we15.lab2.api.impl.PlayJeopardyFactory;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.*;
import views.html.jeopardy;
import views.html.question;

public class Jeopardy extends Controller{

	
	private static JeopardyGame game;

	@Transactional
	public static Result start(String username)
	{
		String queryString = "SELECT u FROM ComplexUser u where u.name = '"+ username+"'";
		TypedQuery<ComplexUser> query = play.db.jpa.JPA.em().createQuery(queryString,
				ComplexUser.class);
		if (!query.getResultList().isEmpty()) {
			ComplexUser user = query.getResultList().get(0);
			System.out.println("LOG: GET AVATAR "+user.getAvatar().getName());
			JeopardyFactory factory= new PlayJeopardyFactory("data.de.json");
			game= factory.createGame(user);
			return ok(jeopardy.render(game));
		}
		return badRequest("Wrong User");
	
	}
	
	public static Result loadQuestion()
	{

		DynamicForm form = Form.form().bindFromRequest();
		String questionId=form.get("question_selection");
		System.out.println("LOG QUESTION: "+questionId);
		game.chooseHumanQuestion(Integer.valueOf(questionId));
		Question quest=game.getHumanPlayer().getChosenQuestion();
		System.out.println("LOG CHOOSEN QUESTION "+quest.getText());
	
		return ok(question.render(quest));
	}
	
	public static Result answerQuestion()
	{
		DynamicForm form= Form.form().bindFromRequest();
		
		System.out.println("LOG answer Question ");

		return ok(jeopardy.render(game));
	}
	
}
