package at.ac.tuwien.big.we15.lab2.api.impl;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.big.we15.lab2.api.Avatar;
import at.ac.tuwien.big.we15.lab2.api.Category;
import at.ac.tuwien.big.we15.lab2.api.Player;
import at.ac.tuwien.big.we15.lab2.api.Question;

public class QuizFactory{

	private List<Category> categories;
	private Player enemy;
	private Player user;
	private int numberOfQuestions; 
	private Question selected_question;
	private Question enemy_question;
	private String messageUser="";
	private String messageEnemy="";
	private QuizState state;
	private String hidden="hidden='hidden'";
	
	private String classinfoUser;
	
	private String classinfoEnemy;
	
	private String classinfo;
	
	private String messageQuestionEnemy="";
	

	
	
	public QuizFactory() { 
	}
	
	public QuizState getState(){
		return state;
	}
	
	public void nextState(QuizState newState){
		state = newState;
	}
	
	public void init(){
		enemy = new Player();
				
		numberOfQuestions = 0;
		state = QuizState.QUIZ_INIT;
		enemy.setAvatar(Avatar.getOpponent(user.getAvatar()));		
	}
	
	/**
	 * must be called before init()
	 */
	public void initUser(){
		user = new Player();
		user.setAvatar(Avatar.getRandomAvatar());
	}
	
	public void setCategories(List<Category> categories){
		this.categories = categories;
	} 

	public List<Category> getCategories(){
		return this.categories;
	}

	public Player getEnemy() {
		return enemy;
	}

	public void setEnemy(Player enemy) {
		this.enemy = enemy;
	}

	public Player getUser() {
		return user;
	}

	public void setUser(Player user) {
		this.user = user;
	}

	public Player getFirstPlayer() {
		if(user.getPoints() >= enemy.getPoints()){
			return user;
		}
		
		return enemy;
	}
	
	public Player getSecondPlayer() {
		if(user.getPoints() >= enemy.getPoints()){
			return enemy;
		}
		
		return user;
	}
	
	public int getNumberOfQuestions() {
		return numberOfQuestions;
	}

	public void increaseNumberOfQuestions(){
		++this.numberOfQuestions;
	}
	/**
	 * 
	 * @param id question id 
	 * @return question identified by id; null if not found
	 */
	public Question getQuestion(int id) {
		for(Category cat : categories){
			for(Question q : cat.getQuestions()){
				if(q.getId() == id){
					return q;
				}
			}		
		}	
		return null;
	}
	
	public List<Question> getQuestions() {
		
		ArrayList<Question> questions= new ArrayList<Question>(); 
		
		for(Category cat: categories)
		{
			questions.addAll(cat.getQuestions());
		}
		return questions;
	}

	public Question getSelected_question() {
		return selected_question;
	}

	public void setSelected_question(Question selected_question) {
		this.selected_question = selected_question;
	}

	public Question getEnemy_question() {
		return enemy_question;
	}

	public void setEnemy_question(Question enemy_question) {
		this.enemy_question = enemy_question;
	}

	public String getMessageUser() {
		return messageUser;
	}

	public void setMessageUser(String messageUser) {
		this.messageUser = messageUser;
	}

	public String getMessageEnemy() {
		return messageEnemy;
	}

	public void setMessageEnemy(String messageEnemy) {
		this.messageEnemy = messageEnemy;
	}

	public String getHidden() {
		return hidden;
	}

	public void setHidden(String hidden) {
		this.hidden = hidden;
	}

	public String getClassinfoUser() {
		return classinfoUser;
	}

	public void setClassinfoUser(String classinfoUser) {
		this.classinfoUser = classinfoUser;
	}

	public String getClassinfoEnemy() {
		return classinfoEnemy;
	}

	public void setClassinfoEnemy(String classinfoEnemy) {
		this.classinfoEnemy = classinfoEnemy;
	}

	public String getClassinfo() {
		return classinfo;
	}

	public void setClassinfo(String classinfo) {
		this.classinfo = classinfo;
	}

	public String getMessageQuestionEnemy() {
		return messageQuestionEnemy;
	}

	public void setMessageQuestionEnemy(String messageQuestionEnemy) {
		this.messageQuestionEnemy = messageQuestionEnemy;
	}

	
}
