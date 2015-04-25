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
 
    
    /** 
     * @see HttpServlet#HttpServlet()
     */
    public BigJeopardyServlet() {
        super();
        // TODO Auto-generated constructor stub
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
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/jeopardy.jsp");;		
		QuizFactory quiz = (QuizFactory) request.getSession().getAttribute("quiz");
		
		if(quiz == null){ 
			initQuiz(request,response);
		}
		System.out.println("quiz_state = "+quiz.getState());
		
		if(quiz.getState() == QuizState.QUIZ_ANSWER ){			
			quiz.nextState(QuizState.QUIZ_JEOPARDY);
			
			//retrieve checked answers
			boolean result = false;
			String[] answerIds = request.getParameterValues("answer_selection");

			if(answerIds !=null){//compare answers
				result = q.checkAnswers(answerIds);
			}
			
			if(result){ //add points if answer was correct
				quiz.getUser().setPoints(quiz.getUser().getPoints() + q.getValue());
				quiz.setClassinfoUser("user-info positive-change");
				quiz.setMessageUser("Du hast richtig geantwortet +"+q.getValue()+"€");
	
			} 
			else 
			{
				quiz.getUser().setPoints(quiz.getUser().getPoints() - q.getValue());
				quiz.setClassinfoUser("user-info negative-change");
				quiz.setMessageUser("Du hast falsch geantwortet -"+q.getValue()+"€");
			}
			/*
			 * handle enemy answer
			 */
			
			int nextQuestion=randomGenerator.nextInt((questions.size()-1));
			System.out.println("questions size "+questions.size());
			System.out.println("next Category for enemy "+nextQuestion);
			
			Question enemyQuestion=questions.get(nextQuestion);
			quiz.setEnemy_question(enemyQuestion);
			
			quiz.setMessageQuestionEnemy(quiz.getEnemy().getAvatar().getName()+" hat "
			+ enemyQuestion.getCategory().getName()+" für €"+enemyQuestion.getValue() + " gewählt.");
			quiz.setClassinfo("user-info");
			System.out.println("enemy answer "+enemyQuestion.getCategory()+" "+enemyQuestion.getValue()+ " " +enemyQuestion.getText());
			if(randomGenerator.nextBoolean())
			{
				System.out.println("enemy answer is right");
				quiz.getEnemy().setPoints(quiz.getEnemy().getPoints()+enemyQuestion.getValue());
				quiz.setClassinfoEnemy("user-info positive-change");
				quiz.setMessageEnemy(quiz.getEnemy().getAvatar().getName() +" hat richtig geantwortet +"+enemyQuestion.getValue()+"€");
			} else
			{
				quiz.setClassinfoEnemy("user-info negative-change");
				quiz.setMessageEnemy(quiz.getEnemy().getAvatar().getName() +" hat falsch geantwortet -"+enemyQuestion.getValue()+"€");
				quiz.getEnemy().setPoints(quiz.getEnemy().getPoints()-enemyQuestion.getValue());


			}
			questions.remove(nextQuestion);
			enemyQuestion.setDisabled(true);
			quiz.setHidden("");
			if(quiz.getNumberOfQuestions() == 10){
				quiz.nextState(QuizState.QUIZ_FINISHED);	
				dispatcher = getServletContext().getRequestDispatcher("/winner.jsp");
			}
		}
		else if(quiz.getState() == QuizState.QUIZ_JEOPARDY) {
			
			
				//retrieve selected question via attribute "question_selection"
				//input element with same id returns selected item
				String sel_question = request.getParameter("question_selection");
				
				if(sel_question != null){ //question is selected
					int q_id = Integer.parseInt(sel_question);
					
					System.out.println("selected id:" + q_id);
					q = quiz.getQuestion(q_id);
					System.out.println("Question: "+ q.getText());
					System.out.println("Category: "+q.getCategory().getName());
					
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
			System.out.println("init quiz null");
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
		
		//move to jeopard.jsp
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/jeopardy.jsp");
		dispatcher.forward(request, response);
	
	}


}
