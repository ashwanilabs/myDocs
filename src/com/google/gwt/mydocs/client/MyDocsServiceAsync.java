package com.google.gwt.mydocs.client;

import com.google.gwt.mydocs.shared.Tweet;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MyDocsServiceAsync {

	void getTweetList(String docId, AsyncCallback<String> callback);
	void getTweet(String docId, String tweetId, AsyncCallback<Tweet> callback);
	void createTweet(Tweet tweet, AsyncCallback<Boolean> callback);
	void removeTweet(Tweet tweet, AsyncCallback<Boolean> callback);
	void addComment(Tweet tweet, String commentId, AsyncCallback<Boolean> callback);
	void removeComment(Tweet tweet, String commentId, AsyncCallback<Boolean> callback);
	void changeTweetStatus(Tweet tweet, AsyncCallback<Boolean> callback);
}
