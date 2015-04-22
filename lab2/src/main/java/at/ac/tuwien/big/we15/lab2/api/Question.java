/**
 * <copyright>
 *
 * Copyright (c) 2014 http://www.big.tuwien.ac.at All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * </copyright>
 */
package at.ac.tuwien.big.we15.lab2.api;

import java.util.List;

/**
 * Represents a quiz question
 */
public interface Question {

	/**
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(int id);

	/**
	 * 
	 * @return the unique identifier
	 */
	public int getId();

	/**
	 * 
	 * @return the question text (may contain xhtml5 markup)
	 */
	public String getText();

	/**
	 * sets the question text
	 * 
	 * @param text
	 *            the question text
	 */
	public void setText(String text);

	/**
	 * 
	 * @return the maximum available time for this question
	 */
	public int getValue();

	/**
	 * sets the maximum available time
	 * 
	 * @param maxTime
	 *            the maximum time to set
	 */
	public void setValue(int value);

	/**
	 * 
	 * @return temporary shuffled list of all choices
	 */
	public List<Answer> getAllAnswers();
	
	/**
	 * @param answerList
	 * 				all available answers to this Question
	 */
	public void setAllAnswers(List<Answer> answerList);

	/**
	 * 
	 * @return a list of all correct choices
	 */
	public List<Answer> getCorrectAnswers();

	/**
	 * Adds a new choice to this question
	 * 
	 * @param choice
	 *            the choice to add
	 * @param isCorrect
	 *            a flag indicating if the choice is correct or not
	 */
	public void addAnswer(Answer choice, boolean isCorrect);

	/**
	 * Removes the choice from the question.
	 * 
	 * @param choice
	 *            the choice to remove
	 */
	public void removeAnswer(Answer choice);

	/**
	 * 
	 * @return the category of the question
	 */
	public Category getCategory();

	/**
	 * 
	 * @param category
	 *            the category to set
	 */
	public void setCategory(Category category);
	
	/**
	 * 
	 * @param disabled
	 * 			true if already chosen; otherwise false
	 */
	public void setDisabled(boolean disabled);
	/**
	 * 
	 * @return "disabled" if question is not selectable
	 */
	public String getDisabled();
	
	/**
	 * 
	 * @param id
	 * 			ID of answer to be checked
	 * @return
	 * 		 true 
	 * 			if answer is correct
	 * 		 false
	 * 			else
	 */
	public boolean checkAnswer(Integer id);
}
