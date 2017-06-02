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
 package net.sf.antcontrib.antserver.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.ByteArrayInputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import net.sf.antcontrib.antserver.Response;
import net.sf.antcontrib.antserver.Command;
import net.sf.antcontrib.antserver.commands.RunAntCommand;
import net.sf.antcontrib.antserver.commands.RunTargetCommand;
import net.sf.antcontrib.antserver.commands.ShutdownCommand;
import net.sf.antcontrib.antserver.commands.SendFileCommand;

/****************************************************************************
 * Place class description here.
 *
 * @author		inger
 * @author		<additional author>
 *
 * @since
 *
 ****************************************************************************/


public class ClientTask
        extends Task
{
    private String machine = "localhost";
    private int port = 17000;
    private Vector commands;
    private boolean persistant = false;
    private boolean failOnError = true;

    public ClientTask()
    {
        super();
        this.commands = new Vector();
    }


    public void setMachine(String machine)
    {
        this.machine = machine;
    }


    public void setPort(int port)
    {
        this.port = port;
    }


    public void setPersistant(boolean persistant)
    {
        this.persistant = persistant;
    }


    public void setFailOnError(boolean failOnError)
    {
        this.failOnError = failOnError;
    }


    public void addConfiguredShutdown(ShutdownCommand cmd)
    {
        commands.add(cmd);
    }

    public void addConfiguredRunTarget(RunTargetCommand cmd)
    {
        commands.add(cmd);
    }

    public void addConfiguredRunAnt(RunAntCommand cmd)
    {
        commands.add(cmd);
    }

    public void addConfiguredSendFile(SendFileCommand cmd)
    {
        commands.add(cmd);
    }


    public void execute()
    {
        Enumeration e = commands.elements();
        Command c = null;
        while (e.hasMoreElements())
        {
            c = (Command)e.nextElement();
            c.validate();
        }

        Client client = new Client(getProject(), machine, port);

        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            try
            {
                int failCount = 0;

                client.connect();

                e = commands.elements();
                c = null;
                Response r = null;
                Document d = null;
                boolean keepGoing = true;
                while (e.hasMoreElements() && keepGoing)
                {
                    c = (Command)e.nextElement();
                    r = client.sendCommand(c);
                    if (! r.isSucceeded())
                    {
                        failCount++;
                        log("Command caused a build failure:" + c,
                                Project.MSG_ERR);
                        if (! persistant)
                            keepGoing = false;
                    }

                    try
                    {
                        ByteArrayInputStream bais =
                                new ByteArrayInputStream(r.getResultsXml().getBytes());
                        d = db.parse(bais);
                        NodeList nl = d.getElementsByTagName("target");
                        int len = nl.getLength();
                        Element element = null;
                        for (int i=0;i<len;i++)
                        {
                            element = (Element)nl.item(i);
                            getProject().log("[" + element.getAttribute("name") + "]",
                                    Project.MSG_INFO);
                        }
                    }
                    catch (SAXException se)
                    {

                    }
                }

                if (failCount > 0 && failOnError)
                    throw new BuildException("One or more commands failed.");
            }
            finally
            {
                if (client != null)
                    client.disconnect();
            }
        }
        catch (ParserConfigurationException ex)
        {
            throw new BuildException(ex);
        }
        catch (IOException ex)
        {
            throw new BuildException(ex);
        }
    }
}
