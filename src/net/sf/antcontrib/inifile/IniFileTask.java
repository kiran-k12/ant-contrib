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
 package net.sf.antcontrib.inifile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.util.Vector;
import java.util.Iterator;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import net.sf.antcontrib.inifile.IniFile;
import net.sf.antcontrib.inifile.IniProperty;
import net.sf.antcontrib.inifile.IniSection;


/****************************************************************************
 * Place class description here.
 *
 * @author		inger
 * @author		<additional author>
 *
 * @since
 *
 ****************************************************************************/


public class IniFileTask
        extends Task
{
    public static abstract class IniOperation
    {
        private String section;
        private String property;
        private String ifCond;
        private String unlessCond;

        public IniOperation()
        {
            super();
        }

        public String getSection()
        {
            return section;
        }


        public void setSection(String section)
        {
            this.section = section;
        }


        public String getProperty()
        {
            return property;
        }


        public void setProperty(String property)
        {
            this.property = property;
        }


        public void setIf(String ifCond)
        {
            this.ifCond = ifCond;
        }

        public void setUnless(String unlessCond)
        {
            this.unlessCond = unlessCond;
        }

        /**
         * Returns true if the define's if and unless conditions
         * (if any) are satisfied.
         */
        public boolean isActive(org.apache.tools.ant.Project p)
        {
            if (ifCond != null && p.getProperty(ifCond) == null)
            {
                return false;
            }
            else if (unlessCond != null && p.getProperty(unlessCond) != null)
            {
                return false;
            }

            return true;
        }

        public void execute(Project project, IniFile iniFile)
        {
            if (isActive(project))
                operate(iniFile);
        }

        protected abstract void operate(IniFile file);
    }

    public static final class Remove
            extends IniOperation
    {
        public Remove()
        {
            super();
        }

        protected void operate(IniFile file)
        {
            String secName = getSection();
            String propName = getProperty();

            if (propName == null)
            {
                file.removeSection(secName);
            }
            else
            {
                IniSection section = file.getSection(secName);
                if (section != null)
                    section.removeProperty(propName);
            }
        }
    }


    public final class Set
            extends IniOperation
    {
        private String value;
        private String operation;

        public Set()
        {
            super();
        }


        public void setValue(String value)
        {
            this.value = value;
        }


        public void setOperation(String operation)
        {
            this.operation = operation;
        }


        protected void operate(IniFile file)
        {
            String secName = getSection();
            String propName = getProperty();

            IniSection section = file.getSection(secName);
            if (section == null)
            {
                section = new IniSection(secName);
                file.setSection(section);
            }

            if (propName != null)
            {
                if (operation != null)
                {
                    if ("+".equals(operation))
                    {
                        IniProperty prop = section.getProperty(propName);
                        value = prop.getValue();
                        int intVal = Integer.parseInt(value) + 1;
                        value = String.valueOf(intVal);
                    }
                    else if ("-".equals(operation))
                    {
                        IniProperty prop = section.getProperty(propName);
                        value = prop.getValue();
                        int intVal = Integer.parseInt(value) - 1;
                        value = String.valueOf(intVal);
                    }
                }
                section.setProperty(new IniProperty(propName, value));
            }
        }

    }

    private File source;
    private File dest;
    private Vector operations;

    public IniFileTask()
    {
        super();
        this.operations = new Vector();
    }

    public Set createSet()
    {
        Set set = new Set();
        operations.add(set);
        return set;
    }

    public Remove createRemove()
    {
        Remove remove = new Remove();
        operations.add(remove);
        return remove;
    }


    public void setSource(File source)
    {
        this.source = source;
    }


    public void setDest(File dest)
    {
        this.dest = dest;
    }


    public void execute()
        throws BuildException
    {
        if (dest == null)
            throw new BuildException("You must supply a dest file to write to.");

        IniFile iniFile = null;

        try
        {
            iniFile = readIniFile(source);
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }

        Iterator it = operations.iterator();
        IniOperation operation = null;
        while (it.hasNext())
        {
            operation = (IniOperation)it.next();
            operation.execute(getProject(), iniFile);
        }

        FileWriter writer = null;

        try
        {
            try
            {
                writer = new FileWriter(dest);
                iniFile.write(writer);
            }
            finally
            {
                try
                {
                    if (writer != null)
                        writer.close();
                }
                catch (IOException e)
                {
                    ; // gulp
                }
            }
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }

    }


    private IniFile readIniFile(File source)
        throws IOException
    {
        FileReader reader = null;
        IniFile iniFile = new IniFile();

        if (source == null)
            return iniFile;

        try
        {
            reader = new FileReader(source);
            iniFile.read(reader);
        }
        finally
        {
            try
            {
                if (reader != null)
                    reader.close();
            }
            catch (IOException e)
            {
                ; // gulp
            }
        }

        return iniFile;
    }
}
