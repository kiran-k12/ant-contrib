/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 Ant-Contrib project.  All rights reserved.
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
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Sequential;

/**
 * A wrapper that lets you run a set of tasks and optionally run a
 * different set of tasks if the first set fails and yet another set
 * after the first one has finished.
 *
 * <p>This mirrors Java's try/catch/finally.</p>
 *
 * <p>The tasks inside of the required <code>&lt;try&gt;</code>
 * element will be run.  If one of them should throw a {@link
 * org.apache.tools.ant.BuildException BuildException} several things
 * can happen:</p>
 *
 * <ul>
 *   <li>If there is no <code>&lt;catch&gt;</code> block, the
 *   exception will be passed through to Ant.</li>
 *
 *   <li>If the property attribute has been set, a property of the
 *   given name will be set to the message of the exception.</li>
 *
 *   <li>If the reference attribute has been set, a reference of the
 *   given id will be created and point to the exception object.</li>
 *
 *   <li>If there is a <code>&lt;catch&gt;</code> block, the tasks
 *   nested into it will be run.</li>
 * </ul>
 *
 * <p>If a <code>&lt;finally&gt;</code> block is present, the task
 * nested into it will be run, no matter whether the first tasks have
 * thrown an exception or not.</p>
 *
 * <p><strong>Attributes:</strong></p>
 *
 * <table>
 *   <tr>
 *     <td>Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>property</td>
 *     <td>Name of a property that will receive the message of the
 *     exception that has been caught (if any)</td>
 *     <td>No</td>
 *   </tr>
 *   <tr>
 *     <td>reference</td>
 *     <td>Id of a reference that will point to the exception object
 *     that has been caught (if any)</td>
 *     <td>No</td>
 *   </tr>
 * </table>
 *
 * <p>Use the following task to define the <code>&lt;trycatch&gt;</code>
 * task before you use it the first time:</p>
 *
 * <pre><code>
 *   &lt;taskdef name="trycatch" 
 *            classname="net.sf.antcontrib.logic.TryCatchTask" /&gt;
 * </code></pre>
 * 
 * <h3>Crude Example</h3>
 *
 * <pre><code>
 * &lt;trycatch property=&quot;foo&quot; reference=&quot;bar&quot;&gt;
 *   &lt;try&gt;
 *     &lt;fail&gt;Tada!&lt;/fail&gt;
 *   &lt;/try&gt;
 *
 *   &lt;catch&gt;
 *     &lt;echo&gt;In &amp;lt;catch&amp;gt;.&lt;/echo&gt;
 *   &lt;/catch&gt;
 *
 *   &lt;finally&gt;
 *     &lt;echo&gt;In &amp;lt;finally&amp;gt;.&lt;/echo&gt;
 *   &lt;/finally&gt;
 * &lt;/trycatch&gt;
 *
 * &lt;echo&gt;As property: ${foo}&lt;/echo&gt;
 * &lt;property name=&quot;baz&quot; refid=&quot;bar&quot; /&gt;
 * &lt;echo&gt;From reference: ${baz}&lt;/echo&gt;
 * </code></pre>
 *
 * <p>results in</p>
 *
 * <pre><code>
 *   [trycatch] Caught exception: Tada!
 *       [echo] In &lt;catch&gt;.
 *       [echo] In &lt;finally&gt;.
 *       [echo] As property: Tada!
 *       [echo] From reference: Tada!
 * </code></pre>
 *
 * @author <a href="mailto:stefan.bodewig@freenet.de">Stefan Bodewig</a>
 * @author <a href="mailto:RITCHED2@Nationwide.com">Dan Ritchey</a>
 */
public class TryCatchTask extends Task {

    private Sequential tryTasks = null;
    private Sequential catchTasks = null;
    private Sequential finallyTasks = null;
    private String property = null;
    private String reference = null;

    /**
     * Adds a nested &lt;try&gt; block - one is required, more is
     * forbidden.
     */
    public void addTry(Sequential seq) throws BuildException {
        if (tryTasks != null) {
            throw new BuildException("You must not specify more than one <try>");
        }
        
        tryTasks = seq;
    }

    /**
     * Adds a nested &lt;catch&gt; block - at most one is allowed.
     */
    public void addCatch(Sequential seq) throws BuildException {
        if (catchTasks != null) {
            throw new BuildException("You must not specify more than one <catch>");
        }
        
        catchTasks = seq;
    }

    /**
     * Adds a nested &lt;finally&gt; block - at most one is allowed.
     */
    public void addFinally(Sequential seq) throws BuildException {
        if (finallyTasks != null) {
            throw new BuildException("You must not specify more than one <finally>");
        }
        
        finallyTasks = seq;
    }

    /**
     * Sets the property attribute.
     */
    public void setProperty(String p) {
        property = p;
    }

    /**
     * Sets the reference attribute.
     */
    public void setReference(String r) {
        reference = r;
    }

    /**
     * The heart of the task.
     */
    public void execute() throws BuildException {
        if (tryTasks == null) {
            throw new BuildException("A nested <try> element is required");
        }

        try {
            tryTasks.perform();
        } catch (BuildException e) {
            if (property != null) {
                /*
                 * Using setProperty instead of setNewProperty to
                 * be able to compile with Ant < 1.5.
                 */
                project.setProperty(property, e.getMessage());
            }
            
            if (reference != null) {
                project.addReference(reference, e);
            }
            
            if (catchTasks == null) {
                throw e;
            } else {
                log("Caught exception: "+e.getMessage(), Project.MSG_INFO);
                catchTasks.perform();
            }
        } finally {
            if (finallyTasks != null) {
                finallyTasks.perform();
            }
        }
    }

}
