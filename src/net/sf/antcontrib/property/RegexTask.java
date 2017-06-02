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

import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.RegularExpression;
import org.apache.tools.ant.types.Substitution;
import org.apache.tools.ant.util.regexp.Regexp;

/****************************************************************************
 * Place class description here.
 *
 * @author		Inger
 * @author		<additional author>
 *
 * @since
 *               
 ****************************************************************************/


public class RegexTask
        extends AbstractPropertySetterTask
{
    private String input;

    private RegularExpression regexp;
    private String select;
    private Substitution replace;
    private String defaultValue;

    private boolean caseSensitive = true;
    private boolean global = true;

    public RegexTask()
    {
        super();
    }

    public void setInput(String input)
    {
        this.input = input;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public void setRegexp(String regex)
    {
        if (this.regexp != null)
            throw new BuildException("Cannot specify more than one regular expression");

        this.regexp = new RegularExpression();
        this.regexp.setPattern(regex);
    }


    public RegularExpression createRegexp()
    {
        if (this.regexp != null)
            throw new BuildException("Cannot specify more than one regular expression");
        regexp = new RegularExpression();
        return regexp;
    }

    public void setReplace(String replace)
    {
        if (this.replace != null)
            throw new BuildException("Cannot specify more than one replace expression");
        if (select != null)
            throw new BuildException("You cannot specify both a select and replace expression");
        this.replace = new Substitution();
        this.replace.setExpression(replace);
    }

    public Substitution createReplace()
    {
        if (replace != null)
            throw new BuildException("Cannot specify more than one replace expression");
        if (select != null)
            throw new BuildException("You cannot specify both a select and replace expression");
        replace = new Substitution();
        return replace;
    }

    public void setSelect(String select)
    {
        if (replace != null)
            throw new BuildException("You cannot specify both a select and replace expression");
        this.select = select;
    }

    public void setCaseSensitive(boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }

    public void setGlobal(boolean global)
    {
        this.global = global;
    }

    protected String doReplace()
        throws BuildException
    {
        if (replace == null)
            throw new BuildException("No replace expression specified.");

        int options = 0;
        if (! caseSensitive)
            options |= Regexp.MATCH_CASE_INSENSITIVE;
        if (global)
            options |= Regexp.REPLACE_ALL;

        Regexp sregex = regexp.getRegexp(project);

        String output = null;

        if (sregex.matches(input, options))
            output = sregex.substitute(input,
                    replace.getExpression(getProject()),
                    options);

        if (output == null)
            output = defaultValue;

        return output;
    }

    protected String doSelect()
        throws BuildException
    {
        int options = 0;
        if (! caseSensitive)
            options |= Regexp.MATCH_CASE_INSENSITIVE;

        Regexp sregex = regexp.getRegexp(project);

        String output = select;
        Vector groups = sregex.getGroups(input, options);

        if (groups != null && groups.size() > 0)
        {
            int sz = groups.size();
            for (int i=0;i<sz;i++)
            {
                String s = (String)(groups.elementAt(i));
                RegularExpression result = null;
                result = new RegularExpression();
                result.setPattern("\\\\" + i);
                output = result.getRegexp(project).substitute(output,
                        s,
                        Regexp.MATCH_DEFAULT);
            }
        }
        else
        {
            output = null;
        }

        return output;
    }


    protected void validate()
    {
        super.validate();
        if (regexp == null)
            throw new BuildException("No match expression specified.");
        if (replace == null && select == null)
            throw new BuildException("You must specify either a replace or select expression");
    }

    public void execute()
        throws BuildException
    {
        validate();

        String output = input;
        if (replace != null)
            output = doReplace();
        else
            output = doSelect();

        if (output != null)
            setPropertyValue(output);
    }
}
