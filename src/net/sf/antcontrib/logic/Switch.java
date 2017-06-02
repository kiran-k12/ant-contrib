/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 Ant-Contrib project.  All rights reserved.
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
package net.sf.antcontrib.logic;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Sequential;
import java.util.Vector;

/***
 * Task definition for the ANT task to switch on a particular value.
 *
 * <pre>
 *
 * Usage:
 *
 *   Task declaration in the project:
 *   <code>
 *     &lt;taskdef name="switch" classname="net.sf.antcontrib.logic.Switch" /&gt;
 *   </code>
 *
 *   Task calling syntax:
 *    <code>
 *     &lt;switch value="value" [caseinsensitive="true|false"] &gt;
 *       &lt;case value="val"&gt;
 *         &lt;property name="propname" value="propvalue" /&gt; |
 *         &lt;antcall target="targetname" /&gt; |
 *         any other tasks
 *       &lt;/case&gt;
 *      [
 *       &lt;default&gt;
 *         &lt;property name="propname" value="propvalue" /&gt; |
 *         &lt;antcall target="targetname" /&gt; |
 *         any other tasks
 *       &lt;/default&gt; 
 *      ]
 *     &lt;/switch&gt;
 *    </code>
 *
 *
 *   Attributes:
 *       value           -&gt; The value to switch on
 *       caseinsensitive -&gt; Should we do case insensitive comparisons?
 *                          (default is false)
 *
 *   Subitems:
 *       case     --&gt; An individual case to consider, if the value that
 *                    is being switched on matches to value attribute of
 *                    the case, then the nested tasks will be executed.
 *       default  --&gt; The default case for when no match is found.
 *
 * 
 * Crude Example:
 *
 *     <code>
 *     &lt;switch value=&quot;${foo}&quot;&gt;
 *       &lt;case value=&quot;bar&quot;&gt;
 *         &lt;echo message=&quot;The value of property foo is bar&quot; /&gt;
 *       &lt;/case&gt;
 *       &lt;case value=&quot;baz&quot;&gt;
 *         &lt;echo message=&quot;The value of property foo is baz&quot; /&gt;
 *       &lt;/case&gt;
 *       &lt;default&gt;
 *         &lt;echo message=&quot;The value of property foo is not sensible&quot; /&gt;
 *       &lt;/default&gt;
 *     &lt;/switch&gt;
 *     </code>
 *
 * </pre>
 *
 * @author <a href="mailto:mattinger@mindless.com">Matthew Inger</a>
 * @author <a href="mailto:stefan.bodewig@freenet.de">Stefan Bodewig</a>
 */
public class Switch extends Task
{
    private String value;
    private Vector cases;
    private Sequential defaultCase;
    private boolean caseInsensitive;
    
    /***
     * Default Constructor
     */
    public Switch()
    {
        cases = new Vector();
    }

    public void execute()
        throws BuildException
    {
        if (value == null)
            throw new BuildException("Value is missing");
        if (cases.size() == 0 && defaultCase == null)
            throw new BuildException("No cases supplied");

        Sequential selectedCase = defaultCase;

        int sz = cases.size();
        for (int i=0;i<sz;i++)
        {
            Case c = (Case)(cases.elementAt(i));

            String cvalue = c.value;
            if (cvalue == null) {
                throw new BuildException("Value is required for case.");
            }
            String mvalue = value;

            if (caseInsensitive)
            {
                cvalue = cvalue.toUpperCase();
                mvalue = mvalue.toUpperCase();
            }

            if (cvalue.equals(mvalue) && c != defaultCase)
                selectedCase = c;
        }

        if (selectedCase == null) {
            throw new BuildException("No case matched the value " + value
                                     + " and no default has been specified.");
        }
        selectedCase.perform();
    }

    /***
     * Sets the value being switched on
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    public void setCaseInsensitive(boolean c)
    {
        caseInsensitive = c;
    }

    public final class Case extends Sequential
    {
        private String value;

        public Case()
        {
            super();
        }
        
        public void setValue(String value)
        {
            this.value = value;
        }

        public void execute()
            throws BuildException
        {
            super.execute();
        }

        public boolean equals(Object o)
        {
            boolean res = false;
            Case c = (Case)o;
            if (c.value.equals(value))
                res = true;
            return res;                
        }
    }

    /***
     * Creates the &lt;case&gt; tag
     */
    public Switch.Case createCase()
        throws BuildException
    {
        Switch.Case res = new Switch.Case();
        cases.addElement(res);
        return res;
    }

    /***
     * Creates the &lt;default&gt; tag
     */
    public void addDefault(Sequential res)
        throws BuildException
    {
        if (defaultCase != null)
            throw new BuildException("Cannot specify multiple default cases");

        defaultCase = res;
    }

}
