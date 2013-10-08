package de.enterprise.lokaServer.tools;

import de.enterprise.lokaServer.pojos.PostListPojo;

public class Rating {

	public static final int POST_TIME_RATING_SIZE = 60000;
	public static final float COOL_DOWN_CONST = 0.23f;
	public static final int MIN_TEMP_CONST = 0;
	public static final int MAX_TEMP_CONST = 30;
	public static final float TIME_MODIFIER = 0.01f;
	public static final int VOTE_HEAT_CONST = 2;
	public static final int COMMENT_HEAT_CONST = 3;
	//in minutes
	public static final int START_TIME_BUFFER = 30;
	//in minutes
	public static final int TEAR_DOWN_LENGTH = 1440;
	
	public static void calculateRatingIndex(PostListPojo p, int deltaVote, int deltaComment){
		int goodRating = p.getGoodRating();
		int badRating = p.getBadRating();
		int goodness = goodRating-badRating;
		long age = (System.currentTimeMillis() - p.getDate())/POST_TIME_RATING_SIZE;
		
		//delta time in minutes
		long deltaTime = (System.currentTimeMillis() - p.getLast_action())/POST_TIME_RATING_SIZE;
		
		//cool down
		float currentAttentionPoints = p.getCurrent_attentionPoints() - deltaTime*COOL_DOWN_CONST;
		
		//add attention
		currentAttentionPoints += deltaVote*VOTE_HEAT_CONST;
		currentAttentionPoints += deltaComment*COMMENT_HEAT_CONST;
		currentAttentionPoints = Math.max(currentAttentionPoints, 0);
		currentAttentionPoints = Math.min(currentAttentionPoints, MAX_TEMP_CONST);
		
		//add to total attention points
		float attentionPoints = p.getAttentionPoints() + currentAttentionPoints;
		
		float otherRating = goodness + attentionPoints - (age - START_TIME_BUFFER)*TIME_MODIFIER;
		
		p.setAttentionPoints(attentionPoints);
		p.setCurrent_attentionPoints(currentAttentionPoints);
		p.setOtherRating(otherRating);
	}
	
}
