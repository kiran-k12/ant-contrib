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

import java.util.*;
import java.io.Writer;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;


/****************************************************************************
 * Place class description here.
 *
 * @author		inger
 * @author		<additional author>
 *
 * @since
 *
 ****************************************************************************/


public class IniFile
{
    private List sections;
    private Map sectionMap;

    public IniFile()
    {
        super();
        this.sections = new ArrayList();
        this.sectionMap = new HashMap();
    }


    public List getSections()
    {
        return sections;
    }


    public IniSection getSection(String name)
    {
        return (IniSection)sectionMap.get(name);
    }


    public void setSection(IniSection section)
    {
        IniSection sec = (IniSection)sectionMap.get(section.getName());
        if (sec != null)
        {
            int idx = sections.indexOf(sec);
            sections.set(idx, section);
        }
        else
        {
            sections.add(section);
        }

        sectionMap.put(section.getName(), section);
    }

    public void removeSection(String name)
    {
        IniSection sec = (IniSection)sectionMap.get(name);
        if (sec != null)
        {
            int idx = sections.indexOf(sec);
            sections.remove(idx);
            sectionMap.remove(name);
        }
    }

    public void write(Writer writer)
        throws IOException
    {
        Iterator it = sections.iterator();
        IniSection section = null;
        while (it.hasNext())
        {
            section = (IniSection)it.next();
            section.write(writer);
            writer.write(System.getProperty("line.separator"));
        }
    }

    public void read(Reader reader)
        throws IOException
    {
        BufferedReader br = new BufferedReader(reader);
        String line = null;

        IniSection currentSection = new IniSection("NONE");

        while ((line = br.readLine()) != null)
        {
            int pos = line.indexOf('#');
            if (pos != -1)
                line = line.substring(0,pos);
            line = line.trim();
            if (line.length() > 0)
            {
                if(line.startsWith("[") && line.endsWith("]"))
                {
                    String secName = line.substring(1, line.length()-1);
                    currentSection = new IniSection(secName);
                    setSection(currentSection);
                }
                else
                {
                    String name = line;
                    String value = "";
                    pos = line.indexOf("=");
                    if (pos != -1)
                    {
                        name = line.substring(0,pos);
                        value = line.substring(pos+1);
                    }

                    currentSection.setProperty(new IniProperty(name,value));
                }
            }


        }
    }
}
