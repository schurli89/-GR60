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
    	
    	Category category=new Category();
    	List<Question> questions= new ArrayList<Question>();
    	if(DBPediaService.isAvailable())
    	{
    		Logger.info("DBPediaService Available: " + DBPediaService.isAvailable());
    		Resource vienna = DBPediaService.loadStatements(DBPedia.createResource("Vienna"));
    		Resource austrians= DBPediaService.loadStatements(DBPedia.createResource("Austrians"));
    		Logger.info("frameworks englisch "+ DBPediaService.getResourceName(vienna, Locale.ENGLISH));
    		Logger.info("frameworks deutsch "+ DBPediaService.getResourceName(vienna, Locale.GERMAN));
    		
    		SelectQueryBuilder queryBuilder = DBPediaService.createQueryBuilder()
    				.setLimit(2000)
    				.addWhereClause(RDF.type, DBPediaOWL.Writer)
    				.addPredicateExistsClause(FOAF.name)
    				.addPredicateExistsClause(RDFS.label)
    				.addWhereClause(DBPediaOWL.birthPlace, vienna)
    				.addWhereClause(DBPediaOWL.nationality, austrians)
    				.addFilterClause(RDFS.label, Locale.ENGLISH)
    				.addFilterClause(RDFS.label, Locale.GERMAN);
    	
    		
    		System.out.println(queryBuilder);

    		Model writersVienna = DBPediaService.loadStatements(queryBuilder.toQueryString());
    		List<String> namesOfViennaWriter=DBPediaService.getResourceNames(writersVienna, Locale.GERMAN);
    		Logger.info(""+namesOfViennaWriter);

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
    		List<String> namesOfNotViennaWriter=DBPediaService.getResourceNames(writersNotVienna, Locale.GERMAN);
    		Logger.info(""+namesOfNotViennaWriter);
    		
    	
	
    		Resource republic= DBPediaService.loadStatements(DBPedia.createResource("Republic"));

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
    		List<String> politicanViennaList=DBPediaService.getResourceNames(aRepublic, Locale.GERMAN);
    		Logger.info(" "+politicanViennaList);

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
    		 politicanViennaList=DBPediaService.getResourceNames(noRepublic, Locale.GERMAN);
     		Logger.info(" "+politicanViennaList);
     		
  
    		
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
    		List<String> actorsViennaList=DBPediaService.getResourceNames(personVienna, Locale.GERMAN);
      		Logger.info(" "+actorsViennaList);
    		
     	
    		
    		queryBuilder = DBPediaService.createQueryBuilder()
    				.setLimit(2000)
    				.addWhereClause(RDF.type, DBPediaOWL.University)
    				.addPredicateExistsClause(FOAF.name)
    				.addPredicateExistsClause(RDFS.label)
    				.addWhereClause(DBPediaOWL.city, vienna)
    				.addFilterClause(RDFS.label, Locale.ENGLISH)
    				.addFilterClause(RDFS.label, Locale.GERMAN);
       		Model universityVienna = DBPediaService.loadStatements(queryBuilder.toQueryString());
    		List<String> list=DBPediaService.getResourceNames(universityVienna, Locale.GERMAN);
      		Logger.info(" "+list);
      		
      		queryBuilder = DBPediaService.createQueryBuilder()
    				.setLimit(2000)
    				.addMinusClause(RDF.type, DBPediaOWL.University)
    				.addPredicateExistsClause(FOAF.name)
    				.addPredicateExistsClause(RDFS.label)
    				.addWhereClause(DBPediaOWL.city, vienna)
    				.addFilterClause(RDFS.label, Locale.ENGLISH)
    				.addFilterClause(RDFS.label, Locale.GERMAN);
       		Model buildingVienna = DBPediaService.loadStatements(queryBuilder.toQueryString());
    		list=DBPediaService.getResourceNames(buildingVienna, Locale.GERMAN);
      		Logger.info(" "+list);
      		
    		
    		queryBuilder = DBPediaService.createQueryBuilder()
    				.setLimit(2000)
    				.addWhereClause(RDF.type, DBPediaOWL.Food)
    				.addPredicateExistsClause(FOAF.name)
    				.addPredicateExistsClause(RDFS.label)
    				.addWhereClause(DBPediaOWL.origin, vienna)
    				.addFilterClause(RDFS.label, Locale.ENGLISH)
    				.addFilterClause(RDFS.label, Locale.GERMAN);
       		Model foodVienna = DBPediaService.loadStatements(queryBuilder.toQueryString());
    		list=DBPediaService.getResourceNames(foodVienna, Locale.GERMAN);
      		Logger.info(" "+list);
      		
      		queryBuilder = DBPediaService.createQueryBuilder()
    				.setLimit(2000)
    				.addWhereClause(RDF.type, DBPediaOWL.Food)
    				.addPredicateExistsClause(FOAF.name)
    				.addPredicateExistsClause(RDFS.label)
    				.addMinusClause(DBPediaOWL.origin, vienna)
    				.addFilterClause(RDFS.label, Locale.ENGLISH)
    				.addFilterClause(RDFS.label, Locale.GERMAN);
       		Model foodNotVienna = DBPediaService.loadStatements(queryBuilder.toQueryString());
    		list=DBPediaService.getResourceNames(foodNotVienna, Locale.GERMAN);
      		Logger.info(" "+list);
  

    		questions.add(buildQuestion(category,capitalOfRepublicDE,capitalOfRepublicEN,aRepublic, noRepublic,10));
    		questions.add(buildQuestion(category,universityViennaDE,universityViennaEN,universityVienna, buildingVienna,20));
    		questions.add(buildQuestion(category,foodViennaDE,foodViennaEN,foodVienna, foodNotVienna,30));
    		questions.add(buildQuestion(category,writerOfViennaDE,writerOfViennaEN,writersVienna, personVienna,40));
    		questions.add(buildQuestion(category,writersBornInViennaDE,writersBornInViennaEN,writersVienna, writersNotVienna,50));

    		
    		category.setQuestions(questions);
    		category.setName("Vienna", Locale.ENGLISH.toString());
    		category.setName("Wien", Locale.GERMAN.toString());
			JeopardyDAO.INSTANCE.persist(category);
    	}
    }
    
    private static Question buildQuestion(Category category, String questionTextDE, String questionTextEN, Model rightChoices, Model wrongChoices, int value) {
    	
    	int answerLimit=4;
    	List<String> rightDE=DBPediaService.getResourceNames(rightChoices, Locale.GERMAN.toString());
    	List<String> rightEN=DBPediaService.getResourceNames(rightChoices, Locale.ENGLISH.toString());

    	List<String> wrongDE=DBPediaService.getResourceNames(wrongChoices, Locale.GERMAN.toString());
    	List<String> wrongEN=DBPediaService.getResourceNames(wrongChoices, Locale.ENGLISH.toString());

		Question question= new Question();
    	question.setCategory(category);
    	question.setValue(value);
    	question.setTextDE(questionTextDE);	
    	question.setTextEN(questionTextEN);

    	
    	for(int i=0;i<Math.min(rightDE.size(),answerLimit);i++)
		{
        	Answer answer= new Answer();

    		answer.setTextDE(rightDE.get(i));
    		answer.setTextEN(rightEN.get(i));
    		answer.setCorrectAnswer(true);
    		answer.setQuestion(question);
    		question.addRightAnswer(answer);
		}
    	
    	for(int i=0;i<(answerLimit-rightDE.size());i++)
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