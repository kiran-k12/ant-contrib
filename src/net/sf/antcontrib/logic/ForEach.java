/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 Ant-Contrib project.  All rights reserved.
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

import java.io.File;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

/***
 * Task definition for the foreach task.  The foreach task iterates
 * over a list, a list of filesets, or both.
 *
 * <pre>
 *
 * Usage:
 *
 *   Task declaration in the project:
 *   <code>
 *     &lt;taskdef name="foreach" classname="net.sf.antcontrib.logic.ForEach" /&gt;
 *   </code>
 *
 *   Call Syntax:
 *   <code>
 *     &lt;foreach list="values" target="targ" param="name"
 *                 [parallel="true|false"]
 *                 [delimiter="delim"] /&gt;
 *   </code>
 *
 *   Attributes:
 *         list      --> The list of values to process, with the delimiter character,
 *                       indicated by the "delim" attribute, separating each value
 *         target    --> The target to call for each token, passing the token as the
 *                       parameter with the name indicated by the "param" attribute
 *         param     --> The name of the parameter to pass the tokens in as to the
 *                       target
 *         delimiter --> The delimiter string that separates the values in the "list"
 *                       parameter.  The default is ","
 *         parallel  --> Should all targets execute in parallel.  The default is false.
 *         trim      --> Should we trim the list item before calling the target?
 *
 * </pre>
 * @author <a href="mailto:mattinger@mindless.com">Matthew Inger</a>
 */
public class ForEach extends Task
{
    private String list;
    private String param;
    private String delimiter;
    private String target;
    private boolean inheritAll;
    private boolean inheritRefs;
    private Vector params;
    private Vector references;
    private Path currPath;
    private boolean parallel;
    private boolean trim;
    private int maxThreads;

    /***
     * Default Constructor
     */
    public ForEach()
    {
        super();
        this.list = null;
        this.param = null;
        this.delimiter = ",";
        this.target = null;
        this.inheritAll = false;
        this.inheritRefs = false;
        this.params = new Vector();
        this.references = new Vector();
    	this.parallel = false;
        this.maxThreads = 5;
    }

    private void executeParallelNew(Vector tasks)
        throws NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException
    {
        TaskContainer tc = (TaskContainer) getProject().createTask("parallel");
        Class signature[] = { int.class };
        Object arguments[] = {new Integer(this.maxThreads)};
        Method setThreadsPerProcessor =
                tc.getClass().getMethod("setThreadsPerProcessor",
                        signature);
        setThreadsPerProcessor.invoke(tc, arguments);

        Enumeration e = tasks.elements();
        Task t = null;
        while (e.hasMoreElements())
        {
            t = (Task)e.nextElement();
            tc.addTask(t);
        }

        ((Task)tc).execute();
    }

    private void executeParallelOld(Vector tasks)
    {
        TaskContainer tc = (TaskContainer) getProject().createTask("parallel");
        Enumeration e = tasks.elements();
        Task t = null;
        int cnt = 0;
        while (e.hasMoreElements())
        {
            t = (Task)e.nextElement();
            tc.addTask(t);
            cnt++;
            if (cnt >= maxThreads)
            {
                ((Task)tc).execute();
                tc = (TaskContainer) getProject().createTask("parallel");
                cnt = 0;
            }
        }

        if (cnt > 0)
        {
            ((Task)tc).execute();
        }
    }

    private void executeParallel(Vector tasks)
    {
        boolean useOld = false;
        try
        {
            executeParallelNew(tasks);
        }
        catch (NoSuchMethodException e)
        {
            useOld = true;
        }
        catch (IllegalAccessException e)
        {
            useOld = true;

        }
        catch (InvocationTargetException e)
        {
            useOld = true;
        }

        if (useOld)
        {
            executeParallelOld(tasks);
        }
    }

    private void executeSequential(Vector tasks)
    {
        TaskContainer tc = (TaskContainer) getProject().createTask("sequential");
        Enumeration e = tasks.elements();
        Task t = null;
        while (e.hasMoreElements())
        {
            t = (Task)e.nextElement();
            tc.addTask(t);
        }

        ((Task)tc).execute();
    }

    public void execute()
        throws BuildException
    {
        if (list == null && currPath == null) {
            throw new BuildException("You must have a list or path to iterate through");
        }
        if (param == null)
            throw new BuildException("You must supply a property name to set on each iteration in param");
        if (target == null)
            throw new BuildException("You must supply a target to perform");

        Vector values = new Vector();

        // Take Care of the list attribute
        if (list != null)
        {
            StringTokenizer st = new StringTokenizer(list, delimiter);

            while (st.hasMoreTokens())
            {
                String tok = st.nextToken();
                if (trim) tok = tok.trim();
                values.addElement(tok);
            }
        }

        String[] pathElements = new String[0];
        if (currPath != null) {
            pathElements = currPath.list();
        }

        for (int i=0;i<pathElements.length;i++)
            values.addElement(new File(pathElements[i]));

        Vector tasks = new Vector();

        int sz = values.size();
        CallTarget ct = null;
        Object val = null;
        Property p = null;

        for (int i = 0; i < sz; i++) {
            val = values.elementAt(i);
            ct = createCallTarget();
            p = ct.createParam();
            p.setName(param);

            if (val instanceof File)
                p.setLocation((File)val);
            else
                p.setValue((String)val);

            tasks.addElement(ct);
        }

        if (parallel && maxThreads > 1)
        {
            executeParallel(tasks);
        }
        else
        {
            executeSequential(tasks);
        }
    }

    public void setTrim(boolean trim)
    {
        this.trim = trim;
    }

    public void setList(String list)
    {
        this.list = list;
    }

    public void setDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }

    public void setParam(String param)
    {
        this.param = param;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    public void setParallel(boolean parallel)
    {
	    this.parallel = parallel;
    }

    /**
     * Corresponds to <code>&lt;antcall&gt;</code>'s <code>inheritall</code>
     * attribute.
     */
    public void setInheritall(boolean b) {
        this.inheritAll = b;
    }

    /**
     * Corresponds to <code>&lt;antcall&gt;</code>'s <code>inheritrefs</code>
     * attribute.
     */
    public void setInheritrefs(boolean b) {
        this.inheritRefs = b;
    }


    /***
     * Set the maximum amount of threads we're going to allow
     * at once to execute
     * @param maxThreads
     */
    public void setMaxThreads(int maxThreads)
    {
        this.maxThreads = maxThreads;
    }


    /**
     * Corresponds to <code>&lt;antcall&gt;</code>'s nested
     * <code>&lt;param&gt;</code> element.
     */
    public void addParam(Property p) {
        params.addElement(p);
    }

    /**
     * Corresponds to <code>&lt;antcall&gt;</code>'s nested
     * <code>&lt;reference&gt;</code> element.
     */
    public void addReference(Ant.Reference r) {
        references.addElement(r);
    }

    /**
     * @deprecated Use createPath instead.
     */
    public void addFileset(FileSet set)
    {
        log("The nested fileset element is deprectated, use a nested path "
            + "instead",
            Project.MSG_WARN);
        createPath().addFileset(set);
    }

    public Path createPath() {
        if (currPath == null) {
            currPath = new Path(getProject());
        }
        return currPath;
    }

    private CallTarget createCallTarget() {
        CallTarget ct = (CallTarget) getProject().createTask("antcall");
        ct.setOwningTarget(getOwningTarget());
        ct.init();
        ct.setTarget(target);
        ct.setInheritAll(inheritAll);
        ct.setInheritRefs(inheritRefs);
        Enumeration enum = params.elements();
        while (enum.hasMoreElements()) {
            Property param = (Property) enum.nextElement();
            Property toSet = ct.createParam();
            toSet.setName(param.getName());
            if (param.getValue() != null) {
                toSet.setValue(param.getValue());
            }
            if (param.getFile() != null) {
                toSet.setFile(param.getFile());
            }
            if (param.getResource() != null) {
                toSet.setResource(param.getResource());
            }
            if (param.getPrefix() != null) {
                toSet.setPrefix(param.getPrefix());
            }
            if (param.getRefid() != null) {
                toSet.setRefid(param.getRefid());
            }
            if (param.getEnvironment() != null) {
                toSet.setEnvironment(param.getEnvironment());
            }
            if (param.getClasspath() != null) {
                toSet.setClasspath(param.getClasspath());
            }
        }

        enum = references.elements();
        while (enum.hasMoreElements()) {
            ct.addReference((Ant.Reference) enum.nextElement());
        }

        return ct;
    }

    protected void handleOutput(String line)
    {
        try {
                super.handleOutput(line);
        }
        // This is needed so we can run with 1.5 and 1.5.1
        catch (IllegalAccessError e) {
            super.handleOutput(line);
        }
    }

    protected void handleErrorOutput(String line)
    {
        try {
                super.handleErrorOutput(line);
        }
        // This is needed so we can run with 1.5 and 1.5.1
        catch (IllegalAccessError e) {
            super.handleErrorOutput(line);
        }
    }

}


