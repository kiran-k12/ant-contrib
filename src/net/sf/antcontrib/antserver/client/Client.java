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

import java.io.*;
import java.net.Socket;

import org.apache.tools.ant.Project;

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


public class Client
{
    private String machine;
    private int port;
    private Project project;


    public Client(Project project, String machine, int port)
    {
        super();
        this.machine = machine;
        this.port = port;
        this.project = project;
    }


    private Socket socket;
    private OutputStream os;
    private InputStream is;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean connected;


    public void connect()
            throws IOException
    {
        project.log("Opening connection to " + machine + ":" + port,
                Project.MSG_DEBUG);

        try
        {
            socket = new Socket(machine, port);
            project.log("Got connection to " + machine + ":" + port,
                    Project.MSG_DEBUG);

            os = socket.getOutputStream();
            is = socket.getInputStream();

            oos = new ObjectOutputStream(os);
            ois = new ObjectInputStream(is);

            connected = true;
            try
            {
                // Read the initial response object so that the
                // object stream is initialized
                ois.readObject();
            }
            catch (ClassNotFoundException e)
            {
                ; // gulp
            }
        }
        finally
        {
            // If we were unable to connect, close everything
            if (!connected)
            {

                try
                {
                    if (os != null)
                        os.close();
                    os = null;
                    oos = null;
                }
                catch (IOException e)
                {

                }

                try
                {
                    if (is != null)
                        is.close();
                    is = null;
                    ois = null;
                }
                catch (IOException e)
                {

                }

                try
                {
                    if (socket != null)
                        socket.close();
                    socket = null;
                }
                catch (IOException e)
                {

                }
            }
        }


    }


    public void disconnect()
            throws IOException
    {
        if (!connected)
            return;

        oos.writeObject(DisconnectCommand.DISCONNECT_COMMAND);
        try
        {
            // Read disconnect response
            ois.readObject();
        }
        catch (ClassNotFoundException e)
        {
            ; // gulp
        }


        try
        {
            if (os != null)
                os.close();
        }
        catch (IOException e)
        {
            ; // gulp

        }
        os = null;
        oos = null;

        try
        {
            if (is != null)
                is.close();
        }
        catch (IOException e)
        {
            ; // gulp

        }
        is = null;
        ois = null;

        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            ; // gulp
        }
        socket = null;

        connected = false;

    }


    public Response sendCommand(Command command)
        throws IOException
    {
        project.log("Sending command: " + command,
                Project.MSG_DEBUG);
        oos.writeObject(command);
        command.localExecute(project, oos);

        Response response = null;

        try
        {
            // Read the response object
            response = (Response) ois.readObject();
            project.log("Received Response: " + response,
                    Project.MSG_DEBUG);
        }
        catch (ClassNotFoundException e)
        {
            ; // gulp
        }

        return response;
    }

}
