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
import play.cache.Cache;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.*;
import views.html.jeopardy;
import views.html.question;
import views.html.winner;

public class Jeopardy extends Controller{

	@Transactional
	public static Result start(ComplexUser user){
			JeopardyFactory factory= new PlayJeopardyFactory("data.de.json");
			JeopardyGame game= factory.createGame(user);
			String uuid = session("uuid");
			Cache.set(uuid+"game", game);
			return ok(jeopardy.render(game));
	}
	
	public static Result loadQuestion()	{
		String uuid = session("uuid");
		JeopardyGame game = (JeopardyGame) Cache.get(uuid+"game");
		
		DynamicForm form = Form.form().bindFromRequest();
		String questionId=form.get("question_selection");
		System.out.println("LOG QUESTION: "+questionId);
		game.chooseHumanQuestion(Integer.valueOf(questionId));
		SimpleQuestion quest=(SimpleQuestion) game.getHumanPlayer().getChosenQuestion();
		System.out.println("LOG CHOOSEN QUESTION "+quest.getText());
		
		Cache.set(uuid+"game", game);
		return ok(question.render(game, quest));
	}
	
	public static Result answerQuestion(){
		String uuid = session("uuid");
		JeopardyGame game = (JeopardyGame) Cache.get(uuid+"game");
		
		DynamicForm form= Form.form().bindFromRequest();
		System.out.println("LOG answer Question ");

		return ok(jeopardy.render(game));
	}
	
	public static Result retrieveAnswers(){
		String uuid = session("uuid");
		JeopardyGame game = (JeopardyGame) Cache.get(uuid+"game");
		
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
		Cache.set(uuid+"game", game);
		if(game.getHumanPlayer().getAnsweredQuestions().size() == game.getMaxQuestions()){
		    return ok(winner.render(game));
		}
		else{
		    return ok(jeopardy.render(game));
		}
	}
}
