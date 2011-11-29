package com.google.gwt.mydocs.client;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.mydocs.shared.Tweet;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.DOMException;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

@SuppressWarnings("deprecation")
class StringComparator implements Comparator<String>
{
	public int compare(String s1,String s2)
	{
		return -s1.compareTo(s2);
	}
}

public class MyDocs implements EntryPoint { 
	private String DOC_ID = "";
	private String USER_ID = "root";
	private String CURR_VIEW = "o";
	private Frame docFrame = new Frame();
	private DecoratorPanel mainPanel = new DecoratorPanel();
	@SuppressWarnings("deprecation")
	HorizontalSplitPanel hSplitPanel = new HorizontalSplitPanel();
	private VerticalPanel commentsPanel = new VerticalPanel();
	private DecoratorPanel tweetDecPanel = new DecoratorPanel();
	private FlexTable tweetsFlexTable = new FlexTable();
	private HorizontalPanel tweetPanel = new HorizontalPanel();
	private TextArea tweetTextArea = new TextArea();
	private Button tweetButton = new Button("Post");
	private Map<String, Tweet> tweets = new TreeMap<String, Tweet>(new StringComparator());
	private Map<String, FlexTable> commentsTableList = new HashMap<String, FlexTable>();
	private MyDocsServiceAsync myDocsSvc = GWT.create(MyDocsService.class);

	/**
	 * Entry point method.
	 */
	@SuppressWarnings("deprecation")
	public void onModuleLoad() {

		tweetPanel.setSpacing(5);

		Button newCommentButton = new Button("New");
		newCommentButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				getNewComment();
			}
		});
		tweetPanel.add(newCommentButton);

		Button openComments = new Button("Open");
		openComments.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				CURR_VIEW = "o";
				tweetsFlexTable.removeAllRows();
				drawTweets("o");
			}
		});
		tweetPanel.add(openComments);

		Button resolvedComments = new Button("Resolved");
		resolvedComments.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				CURR_VIEW = "r";
				tweetsFlexTable.removeAllRows();
				drawTweets("r");
			}
		});
		tweetPanel.add(resolvedComments);

		//		Button allComments = new Button("All");
		//		allComments.addClickHandler(new ClickHandler() {
		//
		//			@Override
		//			public void onClick(ClickEvent event) {
		//				CURR_VIEW = "a";
		//				tweetsFlexTable.removeAllRows();
		//				drawTweets();
		//			}
		//		});
		//		tweetPanel.add(allComments);

		Button refreshComments = new Button("Refresh");
		refreshComments.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				tweetsFlexTable.removeAllRows();
				loadTweets();
			}
		});
		tweetPanel.add(refreshComments);

		tweetDecPanel.add(tweetPanel);

		tweetsFlexTable.setCellSpacing(20);

		// Assemble commentsPanel.
		commentsPanel.add(tweetDecPanel);
		commentsPanel.add(tweetsFlexTable);
		commentsPanel.addStyleName("mainPanel");
		commentsPanel.setSpacing(10);
		//<iframe src="http://docs.google.com/viewer?url=http%3A%2F%2Fwww.cse.iitd.ernet.in%2F~mcs093178%2Fmydoc.odt&embedded=true" width="600" height="780" style="border: none;"></iframe>
		//Frame f = new Frame("http://docs.google.com/viewer?url=http://www.cse.iitd.ernet.in/~mcs093178/mydoc.odt&embedded=true");
		docFrame.addStyleName("docFrame");

		hSplitPanel.setSize("1150px", "788px");
		hSplitPanel.setSplitPosition("52.6%");
		hSplitPanel.setLeftWidget(docFrame);
		hSplitPanel.setRightWidget(commentsPanel);

		mainPanel.add(hSplitPanel);

		// Associate the mainPanel with the HTML host page.
		RootPanel.get("docsPanel").add(mainPanel);

		getUserId();

	}

	private void getUserId(){
		final DialogBox dialogBox = new DialogBox();
		VerticalPanel userIdPanel = new VerticalPanel();
		dialogBox.setWidget(userIdPanel);

		Label userIdLabel = new Label("User Id");
		final TextBox userIdTextBox = new TextBox();
		Label docIdLabel = new Label("Document");
		final TextBox docIdTextBox = new TextBox();

		// Add a close button at the bottom of the dialog
		Button sumbmitButton = new Button("Submit", new ClickHandler() {
			public void onClick(ClickEvent event) {

				USER_ID = userIdTextBox.getText().trim();
				DOC_ID = docIdTextBox.getText().trim();
				docFrame.setUrl("http://docs.google.com/viewer?url=http://www.cse.iitd.ernet.in/~mcs093178/" + DOC_ID + "&embedded=true");
				dialogBox.hide();

				// Load the tweetsFlexTable
				loadTweets();
			}
		});

		userIdPanel.add(userIdLabel);
		userIdPanel.add(userIdTextBox);
		userIdPanel.add(docIdLabel);
		userIdPanel.add(docIdTextBox);
		userIdPanel.add(sumbmitButton);

		dialogBox.setGlassEnabled(true);
		dialogBox.setAnimationEnabled(true);
		dialogBox.center();
		dialogBox.show();
	}


	private void getNewComment() {
		final DialogBox dialogBox = new DialogBox(true);
		VerticalPanel commentPanel = new VerticalPanel();
		dialogBox.setWidget(commentPanel);

		Label commentLabel = new Label("Comment:");
		commentPanel.add(commentLabel);
		commentPanel.add(tweetTextArea);
		tweetTextArea.setFocus(true);
		tweetTextArea.addStyleName("tweetText");
		tweetTextArea.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event) {
				if(event.getCharCode() == KeyCodes.KEY_ENTER){
					dialogBox.hide();
					addTweet();
				}
			}
		});


		// Add a close button at the bottom of the dialog
		tweetButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				addTweet();			
				}
		});

		commentPanel.add(tweetButton);
		dialogBox.setGlassEnabled(true);
		dialogBox.setAnimationEnabled(true);
		dialogBox.center();
		dialogBox.show();
	}

	private void loadTweets() {
		// Initialize the service proxy.
		if (myDocsSvc == null) {
			myDocsSvc = GWT.create(MyDocsService.class);
		}

		// Set up the callback object.
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors.
			}

			public void onSuccess(String result) {
				parseTweetList(result);
			}
		};

		// Make the call to the stock price service.
		myDocsSvc.getTweetList(DOC_ID, callback);
	}

	private void parseTweetList(String messageXml){
		try {
			// parse the XML document into a DOM
			Document messageDom = XMLParser.parse(messageXml);

			// find the sender's display name in an attribute of the <from> tag
			NodeList itemNodes = messageDom.getElementsByTagName("items").item(0).getChildNodes();

			for(int i=0; i<itemNodes.getLength(); i++){
				NodeList iList = itemNodes.item(i).getChildNodes();
				String itemnm = iList.item(0).getFirstChild().getNodeValue();
				loadTweet(itemnm);
			}

		} catch (DOMException e) {
			Window.alert("Could not parse XML document.");
		}
	}

	private void loadTweet(final String itemnm){
		// Initialize the service proxy.
		if (myDocsSvc == null) {
			myDocsSvc = GWT.create(MyDocsService.class);
		}

		// Set up the callback object.
		AsyncCallback<Tweet> callback = new AsyncCallback<Tweet>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors.
			}

			public void onSuccess(Tweet tweet) {
				String tweetId = itemnm.substring(itemnm.indexOf('/') + 1); 
				tweets.put(tweetId, tweet);
				FlexTable t = new FlexTable();
				t.setCellSpacing(2);
				t.getColumnFormatter().setWidth(1, "340px");
				commentsTableList.put(tweetId, t);
				drawTweet(tweetId, -1);
			}
		};

		// Make the call to the stock price service.
		myDocsSvc.getTweet(DOC_ID, itemnm, callback);
	}

//	private void drawTweets() {
//		tweetsFlexTable.removeAllRows();
//		int i = 0;
//		for(final String tweetId : tweets.keySet()){
//			drawTweet(tweetId, i);
//			i++;
//		}
//	}

	private void drawTweets(String status) {
		tweetsFlexTable.removeAllRows();
		int i = 0;
		for(final String tweetId : tweets.keySet()){
			if(tweets.get(tweetId).getStatus().equals(status)){
				drawTweet(tweetId, i);
				i++;
			}
		}
	}

	private void drawTweet(final String tweetId, int i) {

		final Tweet tweet = tweets.get(tweetId);

		if(i == -1 && !tweet.getStatus().equals(CURR_VIEW)){
			return ;
		}

		HorizontalPanel tweetHPanel = new HorizontalPanel();
		if(i != -1){
			tweetsFlexTable.setWidget(i, 0, tweetHPanel);
		} else{
			tweetsFlexTable.insertRow(0);
			tweetsFlexTable.setWidget(0, 0, tweetHPanel);
		}

		VerticalPanel tweetVPanel = new VerticalPanel();
		tweetVPanel.addStyleName("tweetVPanel");

		VerticalPanel buttonPanel = new VerticalPanel();

		final ListBox choice = new ListBox();
		if(tweet.getStatus().equals("o")){
			choice.addItem("Resolve");
		} else {
			choice.addItem("Re-open");
		}
		choice.addItem("Delete");
		buttonPanel.add(choice);

		Button tweetButton = new Button("go");
		tweetButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if(choice.getItemText(choice.getSelectedIndex()).equals("Resolve")){
					updateTweetStatus(tweet, "r");
				} else if(choice.getItemText(choice.getSelectedIndex()).equals("Re-open")){
					updateTweetStatus(tweet, "o");
				} else if(choice.getItemText(choice.getSelectedIndex()).equals("Delete")){
					removeTweet(tweet);
				}
			}
		});
		buttonPanel.add(tweetButton);

		if(!USER_ID.equals(tweet.getUserId())){
			choice.removeItem(1);
		}

		tweetHPanel.add(tweetVPanel);
		tweetHPanel.add(buttonPanel);

		VerticalPanel tweetTextPanel = new VerticalPanel();
		tweetTextPanel.addStyleName("tweetTextPanel");


		Label tweetLabel = new Label(tweet.toString());
		Label tweetTimeLabel = new Label(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(new Date(tweet.getTime())));
		tweetTimeLabel.addStyleName("time");
		tweetTextPanel.add(tweetLabel);
		tweetTextPanel.add(tweetTimeLabel);
		tweetVPanel.add(tweetTextPanel);

		final Map<String, Object> comments = tweet.getV();

		final Set<String> commentKeys = comments.keySet();

		final FlexTable commentsTable = commentsTableList.get(tweetId);
		commentsTable.getColumnFormatter().addStyleName(1, "comment");



		commentsTable.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if(event!=null){

					Cell c = commentsTable.getCellForEvent(event);
					if(c.getCellIndex() == 1){
						for(int i=0;i<commentsTable.getRowCount()-1;i++){
							commentsTable.getWidget(i, 1).setStyleName("commentNotSelected");
						}
						int row = c.getRowIndex();
						Widget w = commentsTable.getWidget(row, 1);
						w.addStyleName("commentSelected");					
						String[] commentsArr = commentKeys.toArray(new String[1]);
						String currComment = commentsArr[row+1];
						String edge = tweet.getEdgeFrom(currComment);
						String parentList = edge.substring(edge.indexOf(':')+1);
						Set<String> ancestors = tweet.getAncestors(currComment);
						for(int i=0;i<row;i++){
							if(parentList.indexOf(commentsArr[i+1] + ",") != -1 
									|| parentList.indexOf("," + commentsArr[i+1]) != -1){
								commentsTable.getWidget(i, 1).addStyleName("commentParent");
							} else if (ancestors.contains(commentsArr[i+1])){
								commentsTable.getWidget(i, 1).addStyleName("commentAncestors");
							}
						}
					}

				}
			}
		});

		commentsTable.removeAllRows();

		VerticalPanel commentVPanel = new VerticalPanel();
		commentVPanel.add(commentsTable);

		int j = 0;
		for(final String k :commentKeys){
			if(!k.equals(tweetId)){
				final int index = j;

				CheckBox commentCheckBox = new CheckBox();
				commentCheckBox.setValue(true);	
				commentsTable.setWidget(j, 0, commentCheckBox);

				VerticalPanel commentTextPanel = new VerticalPanel();
				commentTextPanel.setWidth("322px");
				String user = k.substring(k.indexOf('.') + 1);
				Label commentLabel = new Label(user + ": " + (String)comments.get(k));
				Label commentTimeLabel = new Label(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(new Date(Long.parseLong(k.substring(0, k.indexOf('.'))))));
				commentTimeLabel.addStyleName("time");
				commentTextPanel.add(commentLabel);
				commentTextPanel.add(commentTimeLabel);

				commentsTable.setWidget(j, 1, commentTextPanel);
				commentsTable.getCellFormatter().addStyleName(j, 1, "commentList");

				Button removeCommentButton = new Button("x");
				if(USER_ID.equals(user)){
					removeCommentButton.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							removeComment(tweet, k, index);
						}
					});
				} else {
					removeCommentButton.setVisible(false);
				}
				commentsTable.setWidget(j, 2, removeCommentButton);

				j++;
			}
		}
		commentsTable.setWidget(j, 0, new Label(""));

		HorizontalPanel postCommentPanel = new HorizontalPanel();

		final TextArea postCommentTextArea = new TextArea();
		postCommentTextArea.addStyleName("commentText");
		postCommentTextArea.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event) {
				if(event.getCharCode() == KeyCodes.KEY_ENTER){
					addComment(tweetId, postCommentTextArea);
				}
			}
		});
		postCommentPanel.add(postCommentTextArea);

		Button postCommentButton = new Button("Comment");
		postCommentButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				addComment(tweetId, postCommentTextArea);
			}
		});
		postCommentPanel.add(postCommentButton);

		commentVPanel.add(postCommentPanel);

		DisclosurePanel commentDisclosurePanel = new DisclosurePanel("Comments");
		commentDisclosurePanel.setContent(commentVPanel);
		commentDisclosurePanel.setAnimationEnabled(true);
		tweetVPanel.add(commentDisclosurePanel);


	}

	private void updateTweetStatus(Tweet tweet, String status) {

		tweet.setStatus(status);
		Widget w = commentsTableList.get(tweet.getTweetId()).getParent().getParent().getParent().getParent().getParent().getParent();
		w.removeFromParent();

		// Initialize the service proxy.
		if (myDocsSvc == null) {
			myDocsSvc = GWT.create(MyDocsService.class);
		}

		// Set up the callback object.
		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors.
			}

			public void onSuccess(Boolean result) {
				// TODO: Do Something
			}
		};

		// Make the call to the stock price service.
		myDocsSvc.changeTweetStatus(tweet, callback);	
	}

	private void removeTweet(Tweet tweet) {
		// Remove the tweet from the tweets list
		tweets.remove(tweet.getTweetId());
		Widget w = commentsTableList.get(tweet.getTweetId()).getParent().getParent().getParent().getParent().getParent().getParent();
		w.removeFromParent();
		commentsTableList.remove(tweet.getTweetId());
		// Update Server 
		removeTweetServer(tweet);
	}

	private void removeTweetServer(Tweet tweet) {
		// Initialize the service proxy.
		if (myDocsSvc == null) {
			myDocsSvc = GWT.create(MyDocsService.class);
		}

		// Set up the callback object.
		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors.
			}

			public void onSuccess(Boolean result) {
				// TODO: Do Something
			}
		};

		// Make the call to the stock price service.
		myDocsSvc.removeTweet(tweet, callback);		
	}

	private void addTweet() {

		final String tweetText = tweetTextArea.getText().trim();
		if(tweetText.equals("")){
			return;
		}
		tweetTextArea.setFocus(true);
		tweetTextArea.setText("");

		Tweet tweet = new Tweet(DOC_ID, USER_ID, tweetText);

		// Add the tweet to the tweets list
		tweets.put(tweet.getTweetId(), tweet);
		commentsTableList.put(tweet.getTweetId(), new FlexTable());

		// Draw the Tweets
		drawTweet(tweet.getTweetId(), -1);

		// Update Server
		addTweetServer(tweet);

	}

	private void addTweetServer(Tweet tweet){
		// Initialize the service proxy.
		if (myDocsSvc == null) {
			myDocsSvc = GWT.create(MyDocsService.class);
		}

		// Set up the callback object.
		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors.
			}

			public void onSuccess(Boolean result) {
				// TODO: Do Something
			}
		};

		// Make the call to the stock price service.
		myDocsSvc.createTweet(tweet, callback);
	}

	private void addComment(String tweetId, TextArea commentTextArea) {
		String commentString = commentTextArea.getText().trim();
		commentTextArea.setText("");
		commentTextArea.setFocus(true);

		final Tweet tweet = tweets.get(tweetId);

		Map<String, Object> comments = tweet.getV();
		FlexTable commentsTable = commentsTableList.get(tweetId);

		Set<String> commentKeys = comments.keySet();

		Set<String> commentIdList = new TreeSet<String>(commentKeys);

		int i = 0;
		for(String k : comments.keySet()){
			if(!k.equals(tweetId)){
				CheckBox c = (CheckBox)commentsTable.getWidget(i, 0);
				if(c.getValue() == false){
					commentIdList.remove(k);
				}
				i++;
			}
		}

		final String commentId = 	System.currentTimeMillis() + "." + USER_ID;

		CheckBox commentCheckBox = new CheckBox();
		commentCheckBox.setValue(true);	

		final int index = commentsTable.insertRow(commentsTable.getRowCount()-1);
		commentsTable.setWidget(index, 0, commentCheckBox);

		VerticalPanel commentTextPanel = new VerticalPanel();
		commentTextPanel.setWidth("322px");
		Label commentLabel = new Label(USER_ID + ": " + commentString);
		Label commentTimeLabel = new Label(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(new Date(Long.parseLong(commentId.substring(0, commentId.indexOf('.'))))));
		commentTimeLabel.addStyleName("time");
		commentTextPanel.add(commentLabel);
		commentTextPanel.add(commentTimeLabel);
		commentsTable.setWidget(index, 1, commentTextPanel);
		commentsTable.getCellFormatter().addStyleName(index, 1, "commentList");

		Button removeCommentButton = new Button("x");
		removeCommentButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				removeComment(tweet, commentId, index);
			}
		});
		commentsTable.setWidget(index, 2, removeCommentButton);

		tweet.addComment(commentId, commentString);

		SortedSet<String> u = new TreeSet<String>();
		u.add(commentId);
		SortedSet<String> v = new TreeSet<String>();
		v.addAll(commentIdList);
		tweet.addHyperLink(u, v, null);

		// Update the server
		addCommentServer(tweet, commentId);
	}


	private void addCommentServer(Tweet tweet, String commentId){
		// Initialize the service proxy.
		if (myDocsSvc == null) {
			myDocsSvc = GWT.create(MyDocsService.class);
		}

		// Set up the callback object.
		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors.
			}

			public void onSuccess(Boolean result) {
				// TODO: Do Something
			}
		};

		// Make the call to the stock price service.
		myDocsSvc.addComment(tweet, commentId, callback);
	}

	private void removeComment(Tweet tweet, String commentId, int index) {
		// Remove the tweet from the tweets list
		tweet.removeComment(commentId);

		// Draw the Tweets
		commentsTableList.get(tweet.getTweetId()).getRowFormatter().setVisible(index, false);

		// TODO Update Server 
		removeCommentServer(tweet, commentId);
	}

	private void removeCommentServer(Tweet tweet, String commentId) {
		// Initialize the service proxy.
		if (myDocsSvc == null) {
			myDocsSvc = GWT.create(MyDocsService.class);
		}

		// Set up the callback object.
		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors.
			}

			public void onSuccess(Boolean result) {
				// TODO: Do Something
			}
		};

		// Make the call to the stock price service.
		myDocsSvc.removeComment(tweet, commentId, callback);
	}

}
