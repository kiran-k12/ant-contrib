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
 package net.sf.antcontrib.antserver.commands;

import java.util.Enumeration;
import java.util.Vector;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.taskdefs.Ant;

import net.sf.antcontrib.antserver.Command;

/****************************************************************************
 * Place class description here.
 *
 * @author		inger
 * @author		<additional author>
 *
 * @since
 *
 ****************************************************************************/


public class RunTargetCommand
        implements Command
{
    private String target;
    private Vector properties;
    private Vector references;
    private boolean inheritall = false;
    private boolean interitrefs = false;

    public RunTargetCommand()
    {
        super();
        this.properties = new Vector();
        this.references = new Vector();
    }


    public String getTarget()
    {
        return target;
    }


    public void setTarget(String target)
    {
        this.target = target;
    }


    public Vector getProperties()
    {
        return properties;
    }


    public void setProperties(Vector properties)
    {
        this.properties = properties;
    }



    public Vector getReferences()
    {
        return references;
    }


    public void setReferences(Vector references)
    {
        this.references = references;
    }


    public boolean isInheritall()
    {
        return inheritall;
    }


    public void setInheritall(boolean inheritall)
    {
        this.inheritall = inheritall;
    }


    public boolean isInteritrefs()
    {
        return interitrefs;
    }


    public void setInteritrefs(boolean interitrefs)
    {
        this.interitrefs = interitrefs;
    }


    public void addConfiguredProperty(PropertyContainer property)
    {
        properties.addElement(property);
    }


    public void addConfiguredReference(ReferenceContainer reference)
    {
        references.addElement(reference);
    }

    public void validate()
    {
    }

    public boolean execute(Project project, ObjectInputStream ois)
            throws Throwable
    {
        CallTarget callTarget = (CallTarget)project.createTask("antcall");
        callTarget.setInheritAll(inheritall);
        callTarget.setInheritRefs(interitrefs);

        String toExecute = target;
        if (toExecute == null)
            toExecute = project.getDefaultTarget();
        callTarget.setTarget(toExecute);

        Enumeration e = properties.elements();
        PropertyContainer pc = null;
        Property p = null;
        while (e.hasMoreElements())
        {
            pc = (PropertyContainer)e.nextElement();
            p = callTarget.createParam();
            p.setName(pc.getName());
            p.setValue(pc.getValue());
        }


        e = references.elements();
        ReferenceContainer rc = null;
        Ant.Reference ref = null;
        while (e.hasMoreElements())
        {
            rc = (ReferenceContainer)e.nextElement();
            ref = new Ant.Reference();
            ref.setRefId(rc.getRefId());
            ref.setToRefid(rc.getToRefId());
            callTarget.addReference(ref);
        }

        callTarget.execute();

        return false;
    }

    public void localExecute(Project project, ObjectOutputStream oos)
        throws IOException
    {
    }
}
