package at.ac.tuwien.big.we15.lab2.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import at.ac.tuwien.big.we15.lab2.api.Question;
import at.ac.tuwien.big.we15.lab2.api.impl.QuizFactory;
import at.ac.tuwien.big.we15.lab2.api.impl.QuizState;
import at.ac.tuwien.big.we15.lab2.api.impl.ServletJeopardyFactory;

/**
 * Servlet implementation class BigJeopardyServlet
 */
@WebServlet("/BigJeopardyServlet")
public class BigJeopardyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private ServletJeopardyFactory factory;
    private Question q;
    private Random randomGenerator = new Random();
    private List<Question> questions;
   /* private int userCnt = 0;
    private int enemyCnt = 0;*/
 
    
    /** 
     * @see HttpServlet#HttpServlet()
     */
    public BigJeopardyServlet() {
        super();
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config); 
		// ServletContext coming from javax.servlet.GenericServlet or subclass
		ServletContext servletContext = getServletContext();
		factory = new ServletJeopardyFactory(servletContext);		
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// handle jeopardy question selection		
		HttpSession session = request.getSession();
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/jeopardy.jsp");;		
		QuizFactory quiz = (QuizFactory) session.getAttribute("quiz");
		
		if(quiz == null){ 
			initQuiz(request,response);
		}
	
		if(quiz.getState() == QuizState.QUIZ_ANSWER ){			
			quiz.nextState(QuizState.QUIZ_JEOPARDY);
			
			handleUserAnswer(quiz, request);
			
			if(session.getAttribute("enemy_missing")!= null){//enemy did not choose question yet
				handleEnemyAnswer(quiz);
			}
			
			if(quiz.getNumberOfQuestions() == 10){
				quiz.nextState(QuizState.QUIZ_FINISHED);	
				dispatcher = getServletContext().getRequestDispatcher("/winner.jsp");
			}else{
			
			//case switch
			if(quiz.getUser().getPoints() <= quiz.getEnemy().getPoints()){ //first user, enemy is missing				
				session.setAttribute("enemy_missing", true);
				
			}else{ //first enemy, user is missing
				handleEnemyAnswer(quiz);
				session.setAttribute("enemy_missing", null);
			}			
		}
		}
		else if(quiz.getState() == QuizState.QUIZ_JEOPARDY) {
						
				//retrieve selected question via attribute "question_selection"
				//input element with same id returns selected item
				String sel_question = request.getParameter("question_selection");
				
				if(sel_question != null){ //question is selected
					int q_id = Integer.parseInt(sel_question);
					
					q = quiz.getQuestion(q_id);
					
					//set question non-selectable
					q.setDisabled(true);
			
					quiz.setSelected_question(q);
					quiz.increaseNumberOfQuestions();
					questions.remove(questions.indexOf(q));
					quiz.nextState(QuizState.QUIZ_ANSWER);
				
				//set QuizFactory with selected question and pass it to 
				//question.jsp
				//request.getSession().setAttribute("quiz", quiz);
				dispatcher = getServletContext().getRequestDispatcher("/question.jsp");
				}
			} 
			request.getSession().setAttribute("quiz", quiz);
			dispatcher.forward(request, response);
	} 


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		initQuiz(request,response);
	}
	
	private void initQuiz(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// handle login.jsp 
		//set (new) QuizFactory for jeopard.jsp 
		QuizFactory quiz = (QuizFactory)request.getSession().getAttribute("quiz");
		
		if(quiz == null){
			quiz = new QuizFactory();	
			//initialize user to prevent NullPointerException
			quiz.initUser();
			quiz.init();	
		
		}else{
			if(quiz.getState() == QuizState.QUIZ_FINISHED){
				quiz.getUser().setPoints(0);
				quiz.setHidden("hidden='hidden'");
				quiz.setClassinfoEnemy("");
				quiz.setClassinfoUser("");
				quiz.setClassinfo("");
				quiz.setMessageEnemy("");
				quiz.setMessageUser("");
				quiz.setMessageQuestionEnemy("");
				quiz.init();
			}
		}
		
		quiz.setCategories(factory.createQuestionDataProvider().getCategoryData());
		questions=quiz.getQuestions();
		quiz.nextState(QuizState.QUIZ_JEOPARDY);
		
		request.setAttribute("quiz", quiz);
		request.getSession().setAttribute("quiz", quiz);
		request.getSession().setAttribute("enemy_missing", true);
		
		//move to jeopard.jsp
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/jeopardy.jsp");
		dispatcher.forward(request, response);
	
	}

	/**
	 * Handles user answer process
	 * 
	 * @param quiz current instance
	 * @param request current instance
	 */
	private void handleUserAnswer(QuizFactory quiz, HttpServletRequest request){
		//retrieve checked answers
		System.out.println("USER POINTS: "+quiz.getUser().getPoints());
		boolean result = false;
		String[] answerIds = request.getParameterValues("answer_selection");

		if(answerIds !=null){//compare answers
			result = q.checkAnswers(answerIds);
		}
		
		if(result){ //add points if answer was correct
			quiz.getUser().setPoints(quiz.getUser().getPoints() + q.getValue());
			quiz.setClassinfoUser("user-info positive-change");
			quiz.setMessageUser("Du hast richtig geantwortet +"+q.getValue()+" €");

		} 
		else 
		{
			quiz.getUser().setPoints(quiz.getUser().getPoints() - q.getValue());
			quiz.setClassinfoUser("user-info negative-change");
			quiz.setMessageUser("Du hast falsch geantwortet -"+q.getValue()+" €");
		}	
	}
	
	/**
	 * Handles the question selection and answer process of the enemy
	 * 
	 * @param quiz current instance
	 */
	private void handleEnemyAnswer(QuizFactory quiz){		
		System.out.println("ENEMY POINTS: "+quiz.getEnemy().getPoints());
		int nextQuestion=randomGenerator.nextInt((questions.size()-1));
		
		Question enemyQuestion=questions.get(nextQuestion);
		quiz.setEnemy_question(enemyQuestion);
		
		quiz.setMessageQuestionEnemy(quiz.getEnemy().getAvatar().getName()+" hat "
		+ enemyQuestion.getCategory().getName()+" für € "+enemyQuestion.getValue() + " gewählt.");
		
		quiz.setClassinfo("user-info");
		
		if(randomGenerator.nextBoolean()){
			quiz.getEnemy().setPoints(quiz.getEnemy().getPoints()+enemyQuestion.getValue());
			quiz.setClassinfoEnemy("user-info positive-change");
			quiz.setMessageEnemy(quiz.getEnemy().getAvatar().getName() +" hat richtig geantwortet +"+enemyQuestion.getValue()+" €");
		} else{
			quiz.setClassinfoEnemy("user-info negative-change");
			quiz.setMessageEnemy(quiz.getEnemy().getAvatar().getName() +" hat falsch geantwortet -"+enemyQuestion.getValue()+" €");
			quiz.getEnemy().setPoints(quiz.getEnemy().getPoints()-enemyQuestion.getValue());


		}
		questions.remove(nextQuestion);
		enemyQuestion.setDisabled(true);
		quiz.setHidden("");
	}
}
