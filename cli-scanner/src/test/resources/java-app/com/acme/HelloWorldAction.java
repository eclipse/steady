package com.acme;

import com.opensymphony.xwork2.ActionSupport;

/** Taken from: http://struts.apache.org/docs/hello-world-using-struts-2.html */
public class HelloWorldAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private MessageStore messageStore;

  public String execute() throws Exception {

    messageStore = new MessageStore();
    return SUCCESS;
  }

  public MessageStore getMessageStore() {
    return messageStore;
  }

  public void setMessageStore(MessageStore messageStore) {
    this.messageStore = messageStore;
  }
}
