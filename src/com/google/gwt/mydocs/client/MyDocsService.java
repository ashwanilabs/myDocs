package com.google.gwt.mydocs.client;

import com.google.gwt.mydocs.shared.Tweet;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("myDocsService")
public interface MyDocsService extends RemoteService {
	String getTweetList(String docId) ;
	Tweet getTweet(String docId, String tweetId);
	Boolean createTweet(Tweet tweet);
	Boolean removeTweet(Tweet tweet);
	Boolean changeTweetStatus(Tweet tweet);
	Boolean addComment(Tweet tweet, String commentId);
	Boolean removeComment(Tweet tweet, String commentId);

}

