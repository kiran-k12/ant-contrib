/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 Ant-Contrib project.  All rights reserved.
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

import org.apache.tools.ant.BuildException;

/***
 * Task definition for the propertycopy task, which copies the value of a
 * named property to another property.  This is useful when you need to
 * plug in the value of another property in order to get a property name
 * and then want to get the value of that property name.
 *
 * <pre>
 * Usage:
 *
 *   Task declaration in the project:
 *   <code>
 *     &lt;taskdef name="propertycopy" classname="net.sf.antcontrib.property.PropertyCopy" /&gt;
 *   </code>
 *
 *   Call Syntax:
 *   <code>
 *     &lt;propertycopy name="propname" from="copyfrom" (silent="true|false")? /&gt;
 *   </code>
 *
 *   Attributes:
 *     name      --&gt; The name of the property you wish to set with the value
 *     from      --&gt; The name of the property you wish to copy the value from
 *     silent    --&gt; Do you want to suppress the error if the "from" property
 *                   does not exist, and just not set the property "name".  Default
 *                   is false.
 *
 *   Example:
 *     &lt;property name="org" value="MyOrg" /&gt;
 *     &lt;property name="org.MyOrg.DisplayName" value="My Organiziation" /&gt;
 *     &lt;propertycopy name="displayName" from="org.${org}.DisplayName" /&gt;
 *     &lt;echo message="${displayName}" /&gt;
 * </pre>
 *
 * @author <a href="mailto:mattinger@mindless.com">Matthew Inger</a>
 */
public class PropertyCopy
        extends AbstractPropertySetterTask
{
    private String from;
    private boolean silent;

    /***
     * Default Constructor
     */
    public PropertyCopy()
    {
        super();
        this.from = null;
        this.silent = false;
    }

    public void setName(String name)
    {
        setProperty(name);
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public void setSilent(boolean silent)
    {
        this.silent = silent;
    }

    protected void validate()
    {
        super.validate();
        if (from == null)
            throw new BuildException("Missing the 'from' attribute.");
    }

    public void execute()
        throws BuildException
    {
        validate();

        String value = getProject().getProperty(from);

        if (value == null && ! silent)
            throw new BuildException("Property '" + from + "' is not defined.");

        if (value != null)
            setPropertyValue(value);
    }

}


