package com.acme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

/**
 * Taken from: http://struts.apache.org/docs/hello-world-using-struts-2.html
 *
 */
public class MessageStore {
	
	/**
	 * Nasty example of an anonymous class (implementing Runnable). The implementation of Runnable.run() contains
	 * one named inner class, having a method that uses reflection to call vulnerable code (CVE-2012-2098).
	 * 
	 * None of this is detected by static analysis (Wala), while dynamic testing finds the complete path starting from Thread.run().
	 */
	private Thread t = new Thread(new Runnable() {
		public void run() {
			class Bar {
				public void compress() {
					try {
						// Get the constructor
						final String classname = "org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream";
						final Class clazz = Class.forName(classname);
						
						// Create instance
						final File file = Files.createTempFile("dummy",  ".bz2").toFile();
						final FileOutputStream fos = new FileOutputStream(file);
	
						final Constructor cons = clazz.getDeclaredConstructor(OutputStream.class);
						BZip2CompressorOutputStream bzos = (BZip2CompressorOutputStream)cons.newInstance(fos);
						
						// Call some methods
						final Method method_write = clazz.getDeclaredMethod("write", new Class[] { Integer.TYPE } );
						method_write.invoke(bzos, 42);
						
						final Method method_close = clazz.getDeclaredMethod("close");
						method_close.invoke(bzos);

						System.out.println("Wrote to temp. file [" + file.getAbsolutePath() + "]");
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
			};
			
			final Bar b = new Bar();
			b.compress();
		}
	});

	private String message;

	public MessageStore() {
		this.t.start();
		setMessage("Hello Struts User");
	}

	public String getMessage() {
		this.printStacktrace();
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Among the methods in the stacktrace: org.apache.struts2.dispatcher.ng.filter.StrutsPrepareAndExecuteFilter.doFilter

com.acme.MessageStore.printStacktrace(MessageStore.java:24)
com.acme.MessageStore.getMessage(MessageStore.java:14)
sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
java.lang.reflect.Method.invoke(Method.java:497)
ognl.OgnlRuntime.invokeMethod(OgnlRuntime.java:891)
ognl.OgnlRuntime.getMethodValue(OgnlRuntime.java:1454)
ognl.ObjectPropertyAccessor.getPossibleProperty(ObjectPropertyAccessor.java:60)
ognl.ObjectPropertyAccessor.getProperty(ObjectPropertyAccessor.java:147)
com.opensymphony.xwork2.ognl.accessor.ObjectAccessor.getProperty(ObjectAccessor.java:17)
ognl.OgnlRuntime.getProperty(OgnlRuntime.java:2317)
ognl.ASTProperty.getValueBody(ASTProperty.java:114)
ognl.SimpleNode.evaluateGetValueBody(SimpleNode.java:212)
ognl.SimpleNode.getValue(SimpleNode.java:258)
ognl.ASTChain.getValueBody(ASTChain.java:141)
ognl.SimpleNode.evaluateGetValueBody(SimpleNode.java:212)
ognl.SimpleNode.getValue(SimpleNode.java:258)
ognl.Ognl.getValue(Ognl.java:494)
com.opensymphony.xwork2.ognl.OgnlUtil$3.execute(OgnlUtil.java:317)
com.opensymphony.xwork2.ognl.OgnlUtil.compileAndExecute(OgnlUtil.java:340)
com.opensymphony.xwork2.ognl.OgnlUtil.getValue(OgnlUtil.java:315)
com.opensymphony.xwork2.ognl.OgnlValueStack.getValue(OgnlValueStack.java:362)
com.opensymphony.xwork2.ognl.OgnlValueStack.tryFindValue(OgnlValueStack.java:351)
com.opensymphony.xwork2.ognl.OgnlValueStack.tryFindValueWhenExpressionIsNotNull(OgnlValueStack.java:326)
com.opensymphony.xwork2.ognl.OgnlValueStack.findValue(OgnlValueStack.java:312)
org.apache.struts2.components.Property.start(Property.java:159)
org.apache.struts2.views.jsp.ComponentTagSupport.doStartTag(ComponentTagSupport.java:54)
org.apache.jsp.HelloWorld_jsp._jspx_meth_s_property_0(HelloWorld_jsp.java:92)
org.apache.jsp.HelloWorld_jsp._jspService(HelloWorld_jsp.java:65)
org.apache.jasper.runtime.HttpJspBase.service(HttpJspBase.java:111)
javax.servlet.http.HttpServlet.service(HttpServlet.java:848)
org.apache.jasper.servlet.JspServletWrapper.service(JspServletWrapper.java:403)
org.apache.jasper.servlet.JspServlet.serviceJspFile(JspServlet.java:492)
org.apache.jasper.servlet.JspServlet.service(JspServlet.java:378)
javax.servlet.http.HttpServlet.service(HttpServlet.java:848)
org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:684)
org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:503)
org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137)
org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:575)
org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231)
org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1086)
org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:429)
org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193)
org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1020)
org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135)
org.eclipse.jetty.server.Dispatcher.forward(Dispatcher.java:276)
org.eclipse.jetty.server.Dispatcher.forward(Dispatcher.java:103)
org.apache.struts2.dispatcher.ServletDispatcherResult.doExecute(ServletDispatcherResult.java:164)
org.apache.struts2.dispatcher.StrutsResultSupport.execute(StrutsResultSupport.java:188)
com.opensymphony.xwork2.DefaultActionInvocation.executeResult(DefaultActionInvocation.java:369)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:273)
org.apache.struts2.interceptor.DeprecationInterceptor.intercept(DeprecationInterceptor.java:41)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
org.apache.struts2.interceptor.debugging.DebuggingInterceptor.intercept(DebuggingInterceptor.java:256)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
com.opensymphony.xwork2.interceptor.DefaultWorkflowInterceptor.doIntercept(DefaultWorkflowInterceptor.java:167)
com.opensymphony.xwork2.interceptor.MethodFilterInterceptor.intercept(MethodFilterInterceptor.java:98)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
com.opensymphony.xwork2.validator.ValidationInterceptor.doIntercept(ValidationInterceptor.java:265)
org.apache.struts2.interceptor.validation.AnnotationValidationInterceptor.doIntercept(AnnotationValidationInterceptor.java:76)
com.opensymphony.xwork2.interceptor.MethodFilterInterceptor.intercept(MethodFilterInterceptor.java:98)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
com.opensymphony.xwork2.interceptor.ConversionErrorInterceptor.intercept(ConversionErrorInterceptor.java:138)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
com.opensymphony.xwork2.interceptor.ParametersInterceptor.doIntercept(ParametersInterceptor.java:229)
com.opensymphony.xwork2.interceptor.MethodFilterInterceptor.intercept(MethodFilterInterceptor.java:98)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
com.opensymphony.xwork2.interceptor.ParametersInterceptor.doIntercept(ParametersInterceptor.java:229)
com.opensymphony.xwork2.interceptor.MethodFilterInterceptor.intercept(MethodFilterInterceptor.java:98)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
com.opensymphony.xwork2.interceptor.StaticParametersInterceptor.intercept(StaticParametersInterceptor.java:191)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
org.apache.struts2.interceptor.MultiselectInterceptor.intercept(MultiselectInterceptor.java:73)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
org.apache.struts2.interceptor.DateTextFieldInterceptor.intercept(DateTextFieldInterceptor.java:125)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
org.apache.struts2.interceptor.CheckboxInterceptor.intercept(CheckboxInterceptor.java:91)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
org.apache.struts2.interceptor.FileUploadInterceptor.intercept(FileUploadInterceptor.java:253)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
com.opensymphony.xwork2.interceptor.ModelDrivenInterceptor.intercept(ModelDrivenInterceptor.java:100)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
com.opensymphony.xwork2.interceptor.ScopedModelDrivenInterceptor.intercept(ScopedModelDrivenInterceptor.java:141)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
com.opensymphony.xwork2.interceptor.ChainingInterceptor.intercept(ChainingInterceptor.java:145)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
com.opensymphony.xwork2.interceptor.PrepareInterceptor.doIntercept(PrepareInterceptor.java:171)
com.opensymphony.xwork2.interceptor.MethodFilterInterceptor.intercept(MethodFilterInterceptor.java:98)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
com.opensymphony.xwork2.interceptor.I18nInterceptor.intercept(I18nInterceptor.java:139)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
org.apache.struts2.interceptor.ServletConfigInterceptor.intercept(ServletConfigInterceptor.java:164)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
com.opensymphony.xwork2.interceptor.AliasInterceptor.intercept(AliasInterceptor.java:193)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
com.opensymphony.xwork2.interceptor.ExceptionMappingInterceptor.intercept(ExceptionMappingInterceptor.java:189)
com.opensymphony.xwork2.DefaultActionInvocation.invoke(DefaultActionInvocation.java:244)
org.apache.struts2.impl.StrutsActionProxy.execute(StrutsActionProxy.java:54)
org.apache.struts2.dispatcher.Dispatcher.serviceAction(Dispatcher.java:564)
org.apache.struts2.dispatcher.ng.ExecuteOperations.executeAction(ExecuteOperations.java:81)
org.apache.struts2.dispatcher.ng.filter.StrutsPrepareAndExecuteFilter.doFilter(StrutsPrepareAndExecuteFilter.java:99)

	 */
	private void printStacktrace() {
		final StackTraceElement[] st = new Throwable().getStackTrace();
		System.out.println("Stacktrace:");
		for(StackTraceElement e: st) {
			System.out.println(e.toString());
		}
	} 
}