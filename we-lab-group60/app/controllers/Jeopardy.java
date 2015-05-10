package controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import models.ComplexUser;
import play.cache.Cache;
import play.data.DynamicForm;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.jeopardy;
import views.html.question;
import views.html.winner;
import at.ac.tuwien.big.we15.lab2.api.JeopardyFactory;
import at.ac.tuwien.big.we15.lab2.api.JeopardyGame;
import at.ac.tuwien.big.we15.lab2.api.Question;
import at.ac.tuwien.big.we15.lab2.api.impl.PlayJeopardyFactory;
import at.ac.tuwien.big.we15.lab2.api.impl.SimpleQuestion;

public class Jeopardy extends Controller{

    @Security.Authenticated(JeopardyAuthenticator.class)
	public static Result start(ComplexUser user){
			JeopardyFactory factory= new PlayJeopardyFactory(Messages.get("file"));
			JeopardyGame game= factory.createGame(user);
			String uuid = session("uuid");
			Cache.set(uuid+"game", game);
			return ok(jeopardy.render(game, game.getMarvinPlayer().getChosenQuestion()));
	}

	@Security.Authenticated(JeopardyAuthenticator.class)
	public static Result newGame(){
		String uuid = session("uuid");
		ComplexUser user = (ComplexUser) Cache.get(uuid+"user");
		
		JeopardyFactory factory= new PlayJeopardyFactory(Messages.get("file"));
		JeopardyGame game= factory.createGame(user);
		
		Cache.set(uuid+"game", game);
		return ok(jeopardy.render(game, game.getMarvinPlayer().getChosenQuestion()));
	}

    @Security.Authenticated(JeopardyAuthenticator.class)
	public static Result loadQuestion()	{
		String uuid = session("uuid");
		JeopardyGame game = (JeopardyGame) Cache.get(uuid+"game");
		
		DynamicForm form = Form.form().bindFromRequest();
		String questionId=form.data().get("question_selection");
		
		if(questionId == null){
			return badRequest(jeopardy.render(game, game.getMarvinPlayer().getChosenQuestion()));
		}
		
		game.chooseHumanQuestion(Integer.valueOf(questionId));
		SimpleQuestion quest=(SimpleQuestion) game.getHumanPlayer().getChosenQuestion();
			
		Cache.set(uuid+"game", game);
		return ok(question.render(game, quest));
	}
	
	@Security.Authenticated(JeopardyAuthenticator.class)
	public static Result retrieveAnswers(){
		String uuid = session("uuid");
		JeopardyGame game = (JeopardyGame) Cache.get(uuid+"game");
		
		DynamicForm form = Form.form().bindFromRequest();
		
		//retrieve answer ids
		Collection<String> ids = form.data().values();
		Iterator<String> iterator = ids.iterator();
		iterator.next();
		iterator.remove();
		List<Integer> answerIds = new ArrayList<Integer>();
		while(iterator.hasNext()){
			//System.out.println("form.data: " + s);	
			answerIds.add(Integer.parseInt(iterator.next()));
		}
		
		Question question = game.getMarvinPlayer().getChosenQuestion();
		//answer
		game.answerHumanQuestion(answerIds);
		Cache.set(uuid+"game", game);
		if(game.getHumanPlayer().getAnsweredQuestions().size() == game.getMaxQuestions()){
		    return ok(winner.render(game));
		}
		else{
		    return ok(jeopardy.render(game, question));
		}
	}
}
