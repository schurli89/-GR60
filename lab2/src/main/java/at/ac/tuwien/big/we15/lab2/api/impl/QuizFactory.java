package at.ac.tuwien.big.we15.lab2.api.impl;

import java.util.List;

import javax.servlet.ServletContext;

import at.ac.tuwien.big.we15.lab2.api.Avatar;
import at.ac.tuwien.big.we15.lab2.api.Category;
import at.ac.tuwien.big.we15.lab2.api.JeopardyFactory;
import at.ac.tuwien.big.we15.lab2.api.Player;

public class QuizFactory{

	private List<Category> categories;
	private Player enemy;
	private Player user;
	private int numberOfQuestions = 0;
	
	public QuizFactory( ) { 
		System.out.println("Quizfactory constructor");
	//	init();
		//System.out.println("categories.size: " + categories.size());
	}
	
	public void init(){
		user = new Player();
		user.setAvatar(Avatar.getRandomAvatar());
		enemy = new Player();
		enemy.setAvatar(Avatar.getOpponent(user.getAvatar()));
		System.out.println("enemy name: " + enemy.getAvatar().getName());
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

	public int getNumberOfQuestions() {
		return numberOfQuestions;
	}

	public void setNumberOfQuestions(int numberOfQuestions) {
		this.numberOfQuestions = numberOfQuestions;
	}
	
}
