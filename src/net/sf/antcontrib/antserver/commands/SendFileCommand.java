package net.sf.antcontrib.antserver.commands;

import java.io.*;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;

import net.sf.antcontrib.antserver.Command;

// --------------------------------------------------------------------------
//
// Copyright(c) 2001-2002 Synygy, Inc.
// (Unpublished Work) All Rights Reserved.
//
// This program belongs to Synygy, Inc.  It is considered a TRADE SECRET and
// is not to be divulged or used by parties who have not received written
// authorization from Synygy, Inc.
//
// --------------------------------------------------------------------------
//
// B E G I N   S O U R C E   C O N T R O L   A R E A
//
// $Project$
//
// Original author:
//
// Brief description:
//
// Most recent Revision:
// $Header: /cvsroot/ant-contrib/ant-contrib/src/net/sf/antcontrib/antserver/commands/SendFileCommand.java,v 1.5 2003/11/07 23:09:01 mattinger Exp $
//
// ---- B E G I N    H I S T O R Y -------------------------------------------
//
/* $Log[8]$
*/
//
// ---- E N D    H I S T O R Y -----------------------------------------------
//
// $Nokeywords$
//
//
// E N D   S O U R C E   C O N T R O L   A R E A
//
// --------------------------------------------------------------------------

/****************************************************************************
 * Place class description here.
 *
 * @author		inger
 * @author		<additional author>
 *
 * @since
 *
 ****************************************************************************/


public class SendFileCommand
        implements Command
{
    private File file;
    private String todir;
    private String tofile;

    public File getFile()
    {
        return file;
    }


    public void setFile(File file)
    {
        this.file = file;
    }


    public String getTofile()
    {
        return tofile;
    }


    public void setTofile(String tofile)
    {
        this.tofile = tofile;
    }


    public String getTodir()
    {
        return todir;
    }


    public void setTodir(String todir)
    {
        this.todir = todir;
    }

    public void validate()
    {
        if (file == null)
            throw new BuildException("Missing required attribute 'file'");

        if (tofile == null && todir == null)
            throw new BuildException("Missing both attributes 'tofile' and 'todir'"
                + " at least one must be supplied");
    }

    public boolean execute(Project project, ObjectInputStream stream)
            throws Throwable
    {
        File dest = null;
        if (tofile != null)
        {
            dest = new File(tofile);
            if (! dest.isAbsolute())
                dest = new File(project.getBaseDir(), tofile);
        }
        else
        {
            String fname = file.getName();
            dest = new File(todir);
            if (! dest.isAbsolute())
                dest = new File(project.getBaseDir(), todir);
            dest = new File(dest, fname);

        }

        FileOutputStream fos =  null;
        try
        {
            Long sz = (Long)stream.readObject();
            long size = sz.longValue();

            if (size == -1)
                return false;

            int read = 0;
            long totalread = 0;
            int CHUNK=10*1024;
            byte buf[] = new byte[CHUNK];
            fos = new FileOutputStream(dest);

            while (totalread < size)
            {
                byte b[] = (byte[])stream.readObject();
                read = b.length;
                //read = stream.read(buf, 0, CHUNK);
                totalread += read;
                fos.write(buf, 0, read);
            }
        }
        finally
        {
            try
            {
                if (fos != null)
                    fos.close();
            }
            catch (IOException e)
            {
                ; // gulp;
            }
        }
        return false;
    }

    public void localExecute(Project project, ObjectOutputStream stream)
            throws IOException
    {

        FileInputStream fis = null;

        if (file.exists())
        {
            stream.writeObject(new Long(file.length()));
        }
        else
        {
            stream.writeObject(new Long(-1));
        }

        try
        {

            if (file.length() == 0)
            {
                stream.writeObject(new byte[0]);
            }
            else
            {
                fis = new FileInputStream(file);
                int CHUNK = 10*1024;
                byte buf[] = new byte[CHUNK];
                int read = 0;
                while ((read = fis.read(buf, 0, CHUNK)) != -1)
                {
                    byte b[] = new byte[read];
                    System.arraycopy(buf, 0, b, 0, read);
                    stream.writeObject(b);
                }
            }
        }
        finally
        {
            try
            {
            if (fis != null)
                fis.close();
            }
            catch (IOException e)
            {
                ; //gulp
            }
        }
    }

}
