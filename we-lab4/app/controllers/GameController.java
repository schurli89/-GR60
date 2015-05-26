package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.ws.WebServiceException;

import at.ac.tuwien.big.we.highscore.Failure;
import at.ac.tuwien.big.we.highscore.PublishHighScoreEndpoint;
import at.ac.tuwien.big.we.highscore.PublishHighScoreService;
import at.ac.tuwien.big.we.highscore.data.GenderType;
import at.ac.tuwien.big.we.highscore.data.HighScoreRequestType;
import at.ac.tuwien.big.we.highscore.data.UserDataType;
import at.ac.tuwien.big.we.highscore.data.UserType;
import models.Category;
import models.JeopardyDAO;
import models.JeopardyGame;
import models.JeopardyUser;
import models.Player;
import play.Logger;
import play.cache.Cache;
import play.data.DynamicForm;
import play.data.DynamicForm.Dynamic;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.jeopardy;
import views.html.question;
import views.html.winner;

@Security.Authenticated(Secured.class)
public class GameController extends Controller {
	
	protected static final int CATEGORY_LIMIT = 5;
	private static final String USER_KEY = "3ke93-gue34-dkeu9";
	private static final String ERROR_PUBLISH ="Service unavailable.";
	
	@Transactional
	public static Result index() {
		return redirect(routes.GameController.playGame());
	}
	
	@play.db.jpa.Transactional(readOnly = true)
	private static JeopardyGame createNewGame(String userName) {
		return createNewGame(JeopardyDAO.INSTANCE.findByUserName(userName));
	}
	
	@play.db.jpa.Transactional(readOnly = true)
	private static JeopardyGame createNewGame(JeopardyUser user) {
		if(user == null) // name still stored in session, but database dropped
			return null;

		Logger.info("[" + user + "] Creating a new game.");
		List<Category> allCategories = JeopardyDAO.INSTANCE.findEntities(Category.class);
		
		if(allCategories.size() > CATEGORY_LIMIT) {
			// select 5 categories randomly (simple)
			Collections.shuffle(allCategories);
			allCategories = allCategories.subList(0, CATEGORY_LIMIT);
		}
		Logger.info("Start game with " + allCategories.size() + " categories.");
		JeopardyGame game = new JeopardyGame(user, allCategories);
		cacheGame(game);
		return game;
	}
	
	private static void cacheGame(JeopardyGame game) {
		Cache.set(gameId(), game, 3600);
	}
	
	private static JeopardyGame cachedGame(String userName) {
		Object game = Cache.get(gameId());
		if(game instanceof JeopardyGame)
			return (JeopardyGame) game;
		return createNewGame(userName);
	}
	
	private static String gameId() {
		return "game." + uuid();
	}

	private static String uuid() {
		String uuid = session("uuid");
		if (uuid == null) {
			uuid = UUID.randomUUID().toString();
			session("uuid", uuid);
		}
		return uuid;
	}
	
	@Transactional
	public static Result newGame() {
		Logger.info("[" + request().username() + "] Start new game.");
		JeopardyGame game = createNewGame(request().username());
		return ok(jeopardy.render(game));
	}
	
	@Transactional
	public static Result playGame() {
		Logger.info("[" + request().username() + "] Play the game.");
		JeopardyGame game = cachedGame(request().username());
		if(game == null) // e.g., username still in session, but db dropped
			return redirect(routes.Authentication.login());
		if(game.isAnswerPending()) {
			Logger.info("[" + request().username() + "] Answer pending... redirect");
			return ok(question.render(game));
		} else if(game.isGameOver()) {
			Logger.info("[" + request().username() + "] Game over... redirect");
			return ok(winner.render(game));
		}			
		return ok(jeopardy.render(game));
	}
	
	@play.db.jpa.Transactional(readOnly = true)
	public static Result questionSelected() {
		JeopardyGame game = cachedGame(request().username());
		if(game == null || !game.isRoundStart())
			return redirect(routes.GameController.playGame());
		
		Logger.info("[" + request().username() + "] Questions selected.");		
		DynamicForm form = Form.form().bindFromRequest();
		
		String questionSelection = form.get("question_selection");
		
		if(questionSelection == null || questionSelection.equals("") || !game.isRoundStart()) {
			return badRequest(jeopardy.render(game));
		}
		
		game.chooseHumanQuestion(Long.parseLong(questionSelection));
		
		return ok(question.render(game));
	}
	
	@play.db.jpa.Transactional(readOnly = true)
	public static Result submitAnswers() {
		JeopardyGame game = cachedGame(request().username());
		if(game == null || !game.isAnswerPending())
			return redirect(routes.GameController.playGame());
		
		Logger.info("[" + request().username() + "] Answers submitted.");
		Dynamic form = Form.form().bindFromRequest().get();
		
		@SuppressWarnings("unchecked")
		Map<String,String> data = form.getData();
		List<Long> answerIds = new ArrayList<>();
		
		for(String key : data.keySet()) {
			if(key.startsWith("answers[")) {
				answerIds.add(Long.parseLong(data.get(key)));
			}
		}
		game.answerHumanQuestion(answerIds);
		if(game.isGameOver()) {
			return redirect(routes.GameController.gameOver());
		} else {
			return ok(jeopardy.render(game));
		}
	}
	
	@play.db.jpa.Transactional(readOnly = true)
	public static Result gameOver() {
		JeopardyGame game = cachedGame(request().username());
		if(game == null || !game.isGameOver())
			return redirect(routes.GameController.playGame());
		
		Logger.info("[" + request().username() + "] Game over.");	
		
		String uuid = publishHighscore(game);
		return ok(winner.render(game));
	}

	/**
	 * @return uuid if highscore could be posted; ERROR_PUBLISH else
	 * @param game current game instance	 
	 */
	private static String publishHighscore(JeopardyGame game) {
		HighScoreRequestType requestType = new HighScoreRequestType();
		requestType.setUserKey(USER_KEY);
		JeopardyUser player_winner = game.getWinner().getUser();
		GregorianCalendar date = new GregorianCalendar();
		date.setTimeInMillis(player_winner.getBirthDate().getTime());

		//set winner attributes
		UserType winner = new UserType();
		try {
			winner.setBirthDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(date));
		} catch (DatatypeConfigurationException e) { 
			Logger.error("Winner set Birthdate: " +e.getMessage());
			e.printStackTrace();
		}
		
		winner.setFirstName(player_winner.getFirstName());
		winner.setLastName(player_winner.getLastName());
		winner.setGender(GenderType.fromValue(player_winner.getGender().name()));
		winner.setPoints(game.getWinner().getProfit());
		winner.setPassword("");
		
		JeopardyUser player_loser = game.getLoser().getUser();
		//set loser attributes
		UserType loser = new UserType();
		
		date.setTimeInMillis(player_loser.getBirthDate().getTime());
		try {
			loser.setBirthDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(date));
		} catch (DatatypeConfigurationException e) {
			Logger.error("Loser set Birthdate: " + e.getMessage());
			
		}
		loser.setFirstName(player_loser.getFirstName());
		loser.setLastName(player_loser.getLastName());
		loser.setGender(GenderType.fromValue(player_loser.getGender().name()));
		loser.setPoints(game.getLoser().getProfit());
		loser.setPassword("");
				
				
		UserDataType userData = new UserDataType();
		userData.setLoser(loser);
		userData.setWinner(winner);
		
		Logger.info("POINTS: " + winner.getPoints());
		Logger.info("POINTS: " + loser.getPoints());
		//set request data 
		requestType.setUserData(userData);
		String uuid = ERROR_PUBLISH;
		
		try {
		//publish highscore
		PublishHighScoreService service = new PublishHighScoreService();
		//retrieve uuid
		PublishHighScoreEndpoint endpoint = service.getPublishHighScorePort();		
			uuid = endpoint.publishHighScore(requestType);
			game.setHighscorePosted(true);
			Logger.info("UUID from Highscoreboard: " +uuid);
		} catch (Failure e) {
			Logger.error("FAILURE - could not publisch highscore: " + e.getMessage());
			game.setHighscorePosted(false);
		}catch(WebServiceException e){
			Logger.error("FAILURE - could not publisch highscore: " + e.getMessage());
			game.setHighscorePosted(false);
		}
		
		return uuid;
	}
}
