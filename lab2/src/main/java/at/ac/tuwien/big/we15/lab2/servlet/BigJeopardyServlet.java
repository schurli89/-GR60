package at.ac.tuwien.big.we15.lab2.servlet;

import java.io.IOException;
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
import at.ac.tuwien.big.we15.lab2.api.impl.ServletJeopardyFactory;

/**
 * Servlet implementation class BigJeopardyServlet
 */
@WebServlet("/BigJeopardyServlet")
public class BigJeopardyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private QuizFactory quiz;
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
		/*QuizFactory*/ServletJeopardyFactory factory = new ServletJeopardyFactory(servletContext);
		QuestionDataProvider provider = factory.createQuestionDataProvider();
		List<Category> categories = provider.getCategoryData();
		quiz = new QuizFactory();
		quiz.setCategories(categories);
		quiz.init();
		// category has name and holds questions
		// questions have attributes and answers
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// handle jeopardy question selection
		System.out.println("doGet");
	 
			//retrieve selected question via attribute "question_selection"
			//input element with same id returns selected item
			int q_id = Integer.parseInt(request.getParameter("question_selection"));
			System.out.println("selected id:" + q_id);
			Question q = quiz.getQuestion(q_id);
			System.out.println("Question: "+ q.getText());
			
			//set question non-selectable
			q.setDisabled(true);

			quiz.setSelected_question(q);
		
		//set QuizFactory with selected question and pass it to 
		//question.jsp
		request.getSession().setAttribute("quiz", quiz);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/question.jsp");
		dispatcher.forward(request, response);


	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// handle login.jsp 
		//set QuizFactory for jeopard.jsp
		request.setAttribute("quiz", quiz);
		//move to jeopard.jsp
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/jeopardy.jsp");
		dispatcher.forward(request, response);
	}

}
