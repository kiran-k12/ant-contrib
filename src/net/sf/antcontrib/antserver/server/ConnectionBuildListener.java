/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 Ant-Contrib project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Ant-Contrib project (http://sourceforge.net/projects/ant-contrib)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The name Ant-Contrib must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact
 *    ant-contrib-developers@lists.sourceforge.net.
 *
 * 5. Products derived from this software may not be called "Ant-Contrib"
 *    nor may "Ant-Contrib" appear in their names without prior written
 *    permission of the Ant-Contrib project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE ANT-CONTRIB PROJECT OR ITS
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */
 package net.sf.antcontrib.antserver.server;

import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/****************************************************************************
 * Place class description here.
 *
 * @author		inger
 * @author		<additional author>
 *
 * @since
 *
 ****************************************************************************/


public class ConnectionBuildListener
        implements BuildListener
{
    private Document results;
    private Stack elementStack;
    private ThreadGroup group;

    public ConnectionBuildListener()
        throws ParserConfigurationException
    {
        group = Thread.currentThread().getThreadGroup();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        results = builder.newDocument();
        elementStack = new Stack();

        Element rootElement = results.createElement("results");
        elementStack.push(rootElement);
        results.appendChild(rootElement);
    }

    public Document getDocument()
    {
        return results;
    }

    public void buildStarted(BuildEvent event)
    {
    }


    public void buildFinished(BuildEvent event)
    {
    }


    public void targetStarted(BuildEvent event)
    {
        if (Thread.currentThread().getThreadGroup() != group)
            return;

        Element parent = (Element)elementStack.peek();

        Element myElement = results.createElement("target");
        myElement.setAttribute("name", event.getTarget().getName());
        parent.appendChild(myElement);

        elementStack.push(myElement);
    }


    public void targetFinished(BuildEvent event)
    {
        if (Thread.currentThread().getThreadGroup() != group)
            return;

        Element myElement = (Element)elementStack.peek();

        String message = event.getMessage();
        if (message != null)
            myElement.setAttribute("message", message);

        Throwable t = event.getException();
        if (t != null)
        {
            myElement.setAttribute("status", "failure");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            t.printStackTrace(ps);
            ps.flush();
            String errorMessage = t.getMessage();
            String stackTrace = baos.toString();

            Element error = results.createElement("error");
            Element errorMsgElement = results.createElement("message");
            errorMsgElement.appendChild(results.createTextNode(errorMessage));
            Element stackElement = results.createElement("stack");
            stackElement.appendChild(results.createCDATASection(stackTrace));
            error.appendChild(errorMsgElement);
            error.appendChild(stackElement);
            myElement.appendChild(error);
        }
        else
        {
            myElement.setAttribute("status", "success");
        }

        elementStack.pop();
    }


    public void taskStarted(BuildEvent event)
    {

        if (Thread.currentThread().getThreadGroup() != group)
            return;

        Element parent = (Element)elementStack.peek();

        Element myElement = results.createElement("task");
        myElement.setAttribute("name", event.getTask().getTaskName());
        parent.appendChild(myElement);

        elementStack.push(myElement);
    }


    public void taskFinished(BuildEvent event)
    {
        if (Thread.currentThread().getThreadGroup() != group)
            return;

        Element myElement = (Element)elementStack.peek();

        Throwable t = event.getException();
        if (t != null)
        {
            myElement.setAttribute("status", "failure");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            t.printStackTrace(ps);
            ps.flush();
            String errorMessage = t.getMessage();
            String stackTrace = baos.toString();

            Element error = results.createElement("error");
            Element errorMsgElement = results.createElement("message");
            errorMsgElement.appendChild(results.createTextNode(errorMessage));
            Element stackElement = results.createElement("stack");
            stackElement.appendChild(results.createCDATASection(stackTrace));
            error.appendChild(errorMsgElement);
            error.appendChild(stackElement);
            myElement.appendChild(error);
        }
        else
        {
            myElement.setAttribute("status", "success");
        }

        elementStack.pop();
    }


    public void messageLogged(BuildEvent event)
    {
        /*
        if (Thread.currentThread().getThreadGroup() != group)
            return;

        Element parentElement = (Element)elementStack.peek();

        Element messageElement = results.createElement("message");
        messageElement.setAttribute("level", String.valueOf(event.getPriority()));
        messageElement.appendChild(results.createCDATASection(event.getMessage()));
        parentElement.appendChild(messageElement);
        */
    }
}
