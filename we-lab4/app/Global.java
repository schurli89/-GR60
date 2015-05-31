import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import models.Answer;
import models.Category;
import models.JeopardyDAO;
import models.Question;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.libs.F.Function0;
import scala.util.Random;
import at.ac.tuwien.big.we.dbpedia.api.DBPediaService;
import at.ac.tuwien.big.we.dbpedia.api.SelectQueryBuilder;
import at.ac.tuwien.big.we.dbpedia.vocabulary.DBPProp;
import at.ac.tuwien.big.we.dbpedia.vocabulary.DBPedia;
import at.ac.tuwien.big.we.dbpedia.vocabulary.DBPediaOWL;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import data.JSONDataInserter;

public class Global extends GlobalSettings {
	
	private static final String writersBornInViennaDE = "Wer wurde in Wien geboren?";
	private static final String writersBornInViennaEN = "Who was born in Vienna?";
	
	private static final String capitalOfRepublicDE="Was ist eine Republik?";
	private static final String capitalOfRepublicEN="What is a Republic?";
	
	private static final String writerOfViennaDE="Was ist ein Schriftsteller?";
	private static final String writerOfViennaEN="What is a writer?";
	
	private static final String universityViennaDE="Was ist eine Universität?";
	private static final String universityViennaEN="What is a univeristy?";
	
	private static final String foodViennaDE="Was ist aus Wien?";
	private static final String foodViennaEN="What is from Vienna?";

	
	


	
	@play.db.jpa.Transactional
	public static void insertJSonData() throws IOException {
		String file = Play.application().configuration().getString("questions.filePath");		
		Logger.info("Data from: " + file);
		InputStream is = Play.application().resourceAsStream(file);
		List<Category> categories = JSONDataInserter.insertData(is);
		Logger.info(categories.size() + " categories from json file '" + file + "' inserted.");
		retrieveQuestion();
	}
	
	@play.db.jpa.Transactional
    public void onStart(Application app) {
       try {
    	   JPA.withTransaction(new Function0<Boolean>() {

			@Override
			public Boolean apply() throws Throwable {
				insertJSonData();
				return true;
			}
			   
			});
       } catch (Throwable e) {
    	   e.printStackTrace();
       }
    }

    public void onStop(Application app) {
        Logger.info("Application shutdown...");
    }
    
    private static void retrieveQuestion()
    {
    	
    
    	if(DBPediaService.isAvailable())
    	{
    		Category category=new Category();
        	List<Question> questions= new ArrayList<Question>();
        	
    		Logger.info("DBPediaService Available: " + DBPediaService.isAvailable());
    		
    		//load necessary Resources from DBPedia
    		
    		Resource vienna = DBPediaService.loadStatements(DBPedia.createResource("Vienna"));
    		Resource austrians= DBPediaService.loadStatements(DBPedia.createResource("Austrians"));
    		Resource republic= DBPediaService.loadStatements(DBPedia.createResource("Republic"));

    		Logger.debug("city englisch "+ DBPediaService.getResourceName(vienna, Locale.ENGLISH));
    		Logger.debug("city deutsch "+ DBPediaService.getResourceName(vienna, Locale.GERMAN));
    		
    		
    		//select all Person who are Writers, were born in Vienna and are Austrians
    		SelectQueryBuilder queryBuilder = DBPediaService.createQueryBuilder()
    				.setLimit(2000)
    				.addWhereClause(RDF.type, DBPediaOWL.Writer)
    				.addPredicateExistsClause(FOAF.name)
    				.addPredicateExistsClause(RDFS.label)
    				.addWhereClause(DBPediaOWL.birthPlace, vienna)
    				.addWhereClause(DBPediaOWL.nationality, austrians)
    				.addFilterClause(RDFS.label, Locale.ENGLISH)
    				.addFilterClause(RDFS.label, Locale.GERMAN);
    		//load Model for query
    		Model writersVienna = DBPediaService.loadStatements(queryBuilder.toQueryString());
        	Logger.debug("writer Vienna "+DBPediaService.getResourceNames(writersVienna,Locale.GERMAN));

    		//select all Person who are Writers, were not born in Vienna and are Austrians
    		queryBuilder = DBPediaService.createQueryBuilder()
    				.setLimit(2000)
    				.addWhereClause(RDF.type, DBPediaOWL.Writer)
    				.addPredicateExistsClause(FOAF.name)
    				.addPredicateExistsClause(RDFS.label)
    				.addMinusClause(DBPediaOWL.birthPlace, vienna)
    				.addWhereClause(DBPediaOWL.nationality, austrians)
    				.addFilterClause(RDFS.label, Locale.ENGLISH)
    				.addFilterClause(RDFS.label, Locale.GERMAN);
    		Model writersNotVienna = DBPediaService.loadStatements(queryBuilder.toQueryString());
        	Logger.debug("writers Not Vienna "+DBPediaService.getResourceNames(writersNotVienna,Locale.GERMAN));

        	//select all countries where Vienna is/was the capital and the govermentType is "Republic" 
			queryBuilder = DBPediaService.createQueryBuilder()
    				.setLimit(2000)
    				.addWhereClause(RDF.type, DBPediaOWL.Country)
    				.addPredicateExistsClause(FOAF.name)
    				.addPredicateExistsClause(RDFS.label)
    				.addWhereClause(DBPediaOWL.capital, vienna)
    				.addWhereClause(DBPProp.createProperty("governmentType"), republic)
    				.addFilterClause(RDFS.label, Locale.ENGLISH)
    				.addFilterClause(RDFS.label, Locale.GERMAN);
    		Model aRepublic = DBPediaService.loadStatements(queryBuilder.toQueryString());
    		Logger.debug("republic "+DBPediaService.getResourceNames(aRepublic, Locale.GERMAN));

        	//select all countries where Vienna is/was the capital and the govermentType is not "Republic" 
    		queryBuilder = DBPediaService.createQueryBuilder()
    				.setLimit(2000)
    				.addWhereClause(RDF.type, DBPediaOWL.Country)
    				.addPredicateExistsClause(FOAF.name)
    				.addPredicateExistsClause(RDFS.label)
    				.addWhereClause(DBPediaOWL.capital, vienna)
    				.addMinusClause(DBPProp.createProperty("governmentType"), republic)
    				.addFilterClause(RDFS.label, Locale.ENGLISH)
    				.addFilterClause(RDFS.label, Locale.GERMAN);
    		Model noRepublic = DBPediaService.loadStatements(queryBuilder.toQueryString());
     		Logger.debug("not republic "+DBPediaService.getResourceNames(noRepublic, Locale.GERMAN));
     		
     		//select all persons who were born in Vienna, died in Vienna, have been Austrians and were no Writers
    		queryBuilder = DBPediaService.createQueryBuilder()
    				.setLimit(2000)
    				.addWhereClause(RDF.type, DBPediaOWL.Person)
    				.addPredicateExistsClause(FOAF.name)
    				.addPredicateExistsClause(RDFS.label)
    				.addWhereClause(DBPediaOWL.birthPlace, vienna)
    				.addWhereClause(DBPediaOWL.nationality, austrians)
    				.addWhereClause(DBPediaOWL.deathPlace, vienna)
    				.addMinusClause(RDF.type, DBPediaOWL.Writer)
    				.addFilterClause(RDFS.label, Locale.ENGLISH)
    				.addFilterClause(RDFS.label, Locale.GERMAN);
    		Model personVienna = DBPediaService.loadStatements(queryBuilder.toQueryString());
      		Logger.debug("not a Writer "+DBPediaService.getResourceNames(personVienna, Locale.GERMAN));
    		
      		//select all universities in Vienna
    		queryBuilder = DBPediaService.createQueryBuilder()
    				.setLimit(2000)
    				.addWhereClause(RDF.type, DBPediaOWL.University)
    				.addPredicateExistsClause(FOAF.name)
    				.addPredicateExistsClause(RDFS.label)
    				.addWhereClause(DBPediaOWL.city, vienna)
    				.addFilterClause(RDFS.label, Locale.ENGLISH)
    				.addFilterClause(RDFS.label, Locale.GERMAN);
       		Model universityVienna = DBPediaService.loadStatements(queryBuilder.toQueryString());
      		Logger.debug("university vienna "+DBPediaService.getResourceNames(universityVienna, Locale.GERMAN));
      		
      		//select buildings belong to vienna, which are not universities
      		queryBuilder = DBPediaService.createQueryBuilder()
    				.setLimit(2000)
    				.addMinusClause(RDF.type, DBPediaOWL.University)
    				.addPredicateExistsClause(FOAF.name)
    				.addPredicateExistsClause(RDFS.label)
    				.addWhereClause(DBPediaOWL.city, vienna)
    				.addFilterClause(RDFS.label, Locale.ENGLISH)
    				.addFilterClause(RDFS.label, Locale.GERMAN);
       		Model buildingVienna = DBPediaService.loadStatements(queryBuilder.toQueryString());
      		Logger.debug("not a university "+DBPediaService.getResourceNames(buildingVienna, Locale.GERMAN));
      		
    		//select all kinds of food which has its origin in Vienna
    		queryBuilder = DBPediaService.createQueryBuilder()
    				.setLimit(2000)
    				.addWhereClause(RDF.type, DBPediaOWL.Food)
    				.addPredicateExistsClause(FOAF.name)
    				.addPredicateExistsClause(RDFS.label)
    				.addWhereClause(DBPediaOWL.origin, vienna)
    				.addFilterClause(RDFS.label, Locale.ENGLISH)
    				.addFilterClause(RDFS.label, Locale.GERMAN);
       		Model foodVienna = DBPediaService.loadStatements(queryBuilder.toQueryString());
      		Logger.debug("food vienna "+DBPediaService.getResourceNames(foodVienna, Locale.GERMAN));
      		
      		//select food which has not its origin in VIenna
      		queryBuilder = DBPediaService.createQueryBuilder()
    				.setLimit(2000)
    				.addWhereClause(RDF.type, DBPediaOWL.Food)
    				.addPredicateExistsClause(FOAF.name)
    				.addPredicateExistsClause(RDFS.label)
    				.addMinusClause(DBPediaOWL.origin, vienna)
    				.addFilterClause(RDFS.label, Locale.ENGLISH)
    				.addFilterClause(RDFS.label, Locale.GERMAN);
       		Model foodNotVienna = DBPediaService.loadStatements(queryBuilder.toQueryString());
      		Logger.debug("food not vienna "+DBPediaService.getResourceNames(foodNotVienna, Locale.GERMAN));
  

      		//build Questions
    		questions.add(buildQuestion(category,capitalOfRepublicDE,capitalOfRepublicEN,aRepublic, noRepublic,10));
    		questions.add(buildQuestion(category,universityViennaDE,universityViennaEN,universityVienna, buildingVienna,20));
    		questions.add(buildQuestion(category,foodViennaDE,foodViennaEN,foodVienna, foodNotVienna,30));
    		questions.add(buildQuestion(category,writerOfViennaDE,writerOfViennaEN,writersVienna, personVienna,40));
    		questions.add(buildQuestion(category,writersBornInViennaDE,writersBornInViennaEN,writersVienna, writersNotVienna,50));

    		//add Questions to category and persist
    		category.setQuestions(questions);
    		category.setName("Vienna", Locale.ENGLISH.toString());
    		category.setName("Wien", Locale.GERMAN.toString());
			JeopardyDAO.INSTANCE.persist(category);
    	}
    }
    
    
    private static Question buildQuestion(Category category, String questionTextDE, String questionTextEN, Model rightChoices, Model wrongChoices, int value) {
    	
    	List<String> rightDE=DBPediaService.getResourceNames(rightChoices, Locale.GERMAN.toString());
    	List<String> rightEN=DBPediaService.getResourceNames(rightChoices, Locale.ENGLISH.toString());
        	
    	List<String> wrongDE=DBPediaService.getResourceNames(wrongChoices, Locale.GERMAN.toString());
    	List<String> wrongEN=DBPediaService.getResourceNames(wrongChoices, Locale.ENGLISH.toString());

    	//create Question
		Question question= new Question();
    	question.setCategory(category);
    	question.setValue(value);
    	question.setTextDE(questionTextDE);	
    	question.setTextEN(questionTextEN);
    	
    	//calculate number of right Answers and wrong Answers
    	int answerLimit=4;
    	Random random= new Random();
    	int nrRightAnswer=Math.max(random.nextInt(Math.min(rightDE.size(), answerLimit)), 1);
    	int nrWrongAnswer=answerLimit-nrRightAnswer;
    	
    	
    	//create Answers
    	for(int i=0;i<nrRightAnswer;i++)
		{
        	Answer answer= new Answer();
    		answer.setTextDE(rightDE.get(i));
    		answer.setTextEN(rightEN.get(i));
    		answer.setCorrectAnswer(true);
    		answer.setQuestion(question);
    		question.addRightAnswer(answer);
		}
    	
    	for(int i=0;i<nrWrongAnswer;i++)
		{
        	Answer answer= new Answer();
    		answer.setTextDE(wrongDE.get(i));
    		answer.setTextEN(wrongEN.get(i));
    		answer.setCorrectAnswer(false);
    		answer.setQuestion(question);
    		question.addWrongAnswer(answer);
		}
    	return question;
    }


}