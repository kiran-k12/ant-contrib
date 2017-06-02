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

import java.io.*;
import java.net.Socket;

import org.apache.tools.ant.Project;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;

import net.sf.antcontrib.antserver.Command;
import net.sf.antcontrib.antserver.Response;
import net.sf.antcontrib.antserver.commands.DisconnectCommand;

/****************************************************************************
 * Place class description here.
 *
 * @author		inger
 * @author		<additional author>
 *
 * @since
 *
 ****************************************************************************/


public class ConnectionHandler
        implements Runnable
{
    private static long nextGroupId = 0;
    private ServerTask task;
    private Socket socket;
    private Thread thread;
    private Throwable thrown;

    public ConnectionHandler(ServerTask task, Socket socket)
    {
        super();
        this.socket = socket;
        this.task = task;
    }

    public void start()
    {
        long gid = nextGroupId;
        if (nextGroupId == Long.MAX_VALUE)
            nextGroupId = 0;
        else
            nextGroupId++;

        ThreadGroup group = new ThreadGroup("server-tg-" + gid);
        thread = new Thread(group, this);
        thread.start();
    }

    public Throwable getThrown()
    {
        return thrown;
    }

    public void run()
    {
        InputStream is = null;
        OutputStream os = null;


        try
        {
            ConnectionBuildListener cbl = null;

            is = socket.getInputStream();
            os = socket.getOutputStream();

            ObjectInputStream ois = new ObjectInputStream(is);
            ObjectOutputStream oos = new ObjectOutputStream(os);

            // Write the initial response object so that the
            // object stream is initialized
            oos.writeObject(new Response());

            boolean disconnect = false;
            Command inputCommand = null;
            Response response = null;

            while (! disconnect)
            {
                task.getProject().log("Reading command object.",
                        Project.MSG_DEBUG);

                inputCommand = (Command) ois.readObject();

                task.getProject().log("Executing command object: " + inputCommand,
                        Project.MSG_DEBUG);

                response = new Response();

                try
                {
                    cbl = new ConnectionBuildListener();
                    task.getProject().addBuildListener(cbl);

                    if(inputCommand.execute(task.getProject(), ois))
                    {
                        disconnect = true;
                        task.getProject().log("Got disconnect command",
                                Project.MSG_WARN);
                    }

                    response.setSucceeded(true);
                }
                catch (Throwable t)
                {
                    response.setSucceeded(false);
                    response.setThrowable(t);
                }
                finally
                {
                    if (cbl != null)
                        task.getProject().removeBuildListener(cbl);
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                XMLSerializer serial = new XMLSerializer();
                OutputFormat fmt = new OutputFormat();
                fmt.setOmitDocumentType(true);
                fmt.setOmitXMLDeclaration(false);
                serial.setOutputFormat(fmt);
                serial.setOutputByteStream(baos);
                serial.serialize(cbl.getDocument());
                response.setResultsXml(baos.toString());

                task.getProject().log("Executed command object: " + inputCommand,
                        Project.MSG_DEBUG);

                task.getProject().log("Sending response: " + response,
                        Project.MSG_DEBUG);

                oos.writeObject(response);

                if (inputCommand instanceof DisconnectCommand)
                {
                    disconnect = true;
                    task.getProject().log("Got shutdown command",
                            Project.MSG_WARN);
                    task.shutdown();
                }

            }

        }
        catch (ClassNotFoundException e)
        {
            thrown = e;
        }
        catch (IOException e)
        {
            thrown = e;
        }
        catch (Throwable t)
        {
            thrown = t;
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {

                }
            }

            if (os != null)
            {
                try
                {
                    os.close();
                }
                catch (IOException e)
                {

                }
            }

            if (socket != null)
            {
                try
                {
                    socket.close();
                }
                catch (IOException e)
                {

                }
            }

        }
    }
}
