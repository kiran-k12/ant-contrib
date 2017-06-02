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
package net.sf.antcontrib.property;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.regexp.Regexp;
import org.apache.tools.ant.types.RegularExpression;


/****************************************************************************
 * Place class description here.
 *
 * @author		inger
 * @author		<additional author>
 *
 * @since
 *
 ****************************************************************************/


public class PropertySelector
        extends AbstractPropertySetterTask
{
    private RegularExpression match;
    private String select = "\\0";
    private char delim = ',';
    private boolean caseSensitive = true;
    private boolean distinct = false;


    public PropertySelector()
    {
        super();
    }


    public void setMatch(String match)
    {
        this.match = new RegularExpression();
        this.match.setPattern(match);
    }


    public void setSelect(String select)
    {
        this.select = select;
    }


    public void setCaseSensitive(boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }


    public void setDelimiter(char delim)
    {
        this.delim = delim;
    }


    public void setDistinct(boolean distinct)
    {
        this.distinct = distinct;
    }


    protected void validate()
    {
        super.validate();
        if (match == null)
            throw new BuildException("No match expression specified.");
    }


    public void execute()
            throws BuildException
    {
        validate();

        int options = 0;
        if (!caseSensitive)
            options |= Regexp.MATCH_CASE_INSENSITIVE;

        Regexp regex = match.getRegexp(project);
        Hashtable props = project.getProperties();
        Enumeration e = props.keys();
        StringBuffer buf = new StringBuffer();
        int cnt = 0;

        Vector used = new Vector();

        while (e.hasMoreElements())
        {
            String key = (String) (e.nextElement());
            if (regex.matches(key, options))
            {
                String output = select;
                Vector groups = regex.getGroups(key, options);
                int sz = groups.size();
                for (int i = 0; i < sz; i++)
                {
                    String s = (String) (groups.elementAt(i));

                    RegularExpression result = null;
                    result = new RegularExpression();
                    result.setPattern("\\\\" + i);
                    Regexp sregex = result.getRegexp(project);
                    output = sregex.substitute(output, s, Regexp.MATCH_DEFAULT);
                }

                if (!(distinct && used.contains(output)))
                {
                    used.addElement(output);
                    if (cnt != 0) buf.append(delim);
                    buf.append(output);
                    cnt++;
                }
            }
        }

        if (buf.length() > 0)
            setPropertyValue(buf.toString());
    }
}
