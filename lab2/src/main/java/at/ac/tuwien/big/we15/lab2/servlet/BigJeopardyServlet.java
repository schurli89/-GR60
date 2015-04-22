package at.ac.tuwien.big.we15.lab2.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.ac.tuwien.big.we15.lab2.api.Category;
import at.ac.tuwien.big.we15.lab2.api.Question;
import at.ac.tuwien.big.we15.lab2.api.QuestionDataProvider;
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
		System.out.println("init");
		// ServletContext coming from javax.servlet.GenericServlet or subclass
		ServletContext servletContext = getServletContext();
		/*QuizFactory*/ factory = new ServletJeopardyFactory(servletContext);
		
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// handle jeopardy question selection
		System.out.println("doGet");
		RequestDispatcher dispatcher;
	
	
		QuizFactory quiz = (QuizFactory) request.getSession().getAttribute("quiz");
		if(quiz == null){
			System.out.println("quiz object is null");
		initQuiz(request,response);
		}
		if(quiz.getState() == QuizState.QUIZ_ANSWER ){
			dispatcher = getServletContext().getRequestDispatcher("/jeopardy.jsp");
			quiz.nextState(QuizState.QUIZ_JEOPARDY);
			boolean result = true;
			String[] answerIds = request.getParameterValues("answer_selection");
			
			if(answerIds != null){
				for(String id : answerIds){
					result = result && q.checkAnswer(Integer.parseInt(id));
				}
			}
			else {
				result = false;
			}
			
			if(result){
				quiz.getUser().setPoints(quiz.getUser().getPoints() + q.getValue());
			}
			
			
			/*
			 * handle enemy answer
			 */
			
			
			if(quiz.getNumberOfQuestions() == 10){
				quiz.nextState(QuizState.QUIZ_FINISHED);
				dispatcher = getServletContext().getRequestDispatcher("/winner.jsp");
			}
			dispatcher.forward(request, response);
		}
		else if(quiz.getState() == QuizState.QUIZ_JEOPARDY || quiz.getState() == QuizState.QUIZ_INIT) {

			//retrieve selected question via attribute "question_selection"
			//input element with same id returns selected item
			int q_id = Integer.parseInt(request.getParameter("question_selection"));
			System.out.println("selected id:" + q_id);
			q = quiz.getQuestion(q_id);
			System.out.println("Question: "+ q.getText());
			
			//set question non-selectable
			q.setDisabled(true);
	
			quiz.setSelected_question(q);
			quiz.increaseNumberOfQuestions();
			quiz.nextState(QuizState.QUIZ_ANSWER);
			//set QuizFactory with selected question and pass it to 
			//question.jsp
			request.getSession().setAttribute("quiz", quiz);
			dispatcher = getServletContext().getRequestDispatcher("/question.jsp");
			dispatcher.forward(request, response);
		}


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
		QuizFactory quiz;// = (QuizFactory)request.getSession().getAttribute("quiz");
		//if(quiz == null){
			quiz = new QuizFactory();
			quiz.setCategories(factory.createQuestionDataProvider().getCategoryData());
			quiz.init();
			quiz.nextState(QuizState.QUIZ_INIT);
		//}
		
		request.setAttribute("quiz", quiz);
		request.getSession().setAttribute("quiz", quiz);
		//move to jeopard.jsp
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/jeopardy.jsp");
		dispatcher.forward(request, response);
	
	}

}
