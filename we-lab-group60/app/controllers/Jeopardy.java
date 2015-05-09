package controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.TypedQuery;

import models.ComplexUser;
import at.ac.tuwien.big.we15.lab2.api.Answer;
import at.ac.tuwien.big.we15.lab2.api.JeopardyFactory;
import at.ac.tuwien.big.we15.lab2.api.JeopardyGame;
import at.ac.tuwien.big.we15.lab2.api.Question;
import at.ac.tuwien.big.we15.lab2.api.impl.PlayJeopardyFactory;
import at.ac.tuwien.big.we15.lab2.api.impl.SimpleQuestion;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.*;
import views.html.jeopardy;
import views.html.question;

public class Jeopardy extends Controller{

	
	private static JeopardyGame game;

	@Transactional
	public static Result start(ComplexUser user){
			JeopardyFactory factory= new PlayJeopardyFactory("data.de.json");
			game= factory.createGame(user);
			return ok(jeopardy.render(game));
	
	}
	
	public static Result loadQuestion()	{

		DynamicForm form = Form.form().bindFromRequest();
		String questionId=form.get("question_selection");
		System.out.println("LOG QUESTION: "+questionId);
		game.chooseHumanQuestion(Integer.valueOf(questionId));
		SimpleQuestion quest=(SimpleQuestion) game.getHumanPlayer().getChosenQuestion();
		System.out.println("LOG CHOOSEN QUESTION "+quest.getText());
	
		return ok(question.render(game, quest));
	}
	
	public static Result answerQuestion()
	{
		DynamicForm form= Form.form().bindFromRequest();
		System.out.println("LOG answer Question ");

		return ok(jeopardy.render(game));
	}
	
	public static Result retrieveAnswers(){
		
		DynamicForm form = Form.form().bindFromRequest();
		
		//retrieve answer ids
		Collection<String> ids = form.data().values();
		Iterator<String> iterator = ids.iterator();
		iterator.next();
		iterator.remove();
		List<Answer> correctAnswers = game.getHumanPlayer().getChosenQuestion().getCorrectAnswers();
		List<Integer> answerIds = new ArrayList<Integer>();
		while(iterator.hasNext()){
			//System.out.println("form.data: " + s);	
			answerIds.add(Integer.parseInt(iterator.next()));
		}
		
		//answer
		game.answerHumanQuestion(answerIds);
		return ok(jeopardy.render(game));
	}
}
