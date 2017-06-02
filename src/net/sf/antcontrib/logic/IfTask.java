/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 Ant-Contrib project.  All rights reserved.
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

import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Sequential;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;

/**
 * Perform some tasks based on whether a given condition holds true or
 * not.
 *
 * <p>This task is heavily based on the Condition framework that can
 * be found in Ant 1.4 and later, therefore it cannot be used in
 * conjunction with versions of Ant prior to 1.4.</p>
 *
 * <p>This task doesn't have any attributes, the condition to test is
 * specified by a nested element - see the documentation of your
 * <code>&lt;condition&gt;</code> task (see
 * <a href="http://jakarta.apache.org/ant/manual/CoreTasks/condition.html">the
 * online documentation</a> for example) for a complete list of nested
 * elements.</p>
 *
 * <p>Just like the <code>&lt;condition&gt;</code> task, only a single
 * condition can be specified - you combine them using
 * <code>&lt;and&gt;</code> or <code>&lt;or&gt;</code> conditions.</p>
 *
 * <p>In addition to the condition, you can specify three different
 * child elements, <code>&lt;elseif&gt;</code>, <code>&lt;then&gt;</code> and
 * <code>&lt;else&gt;</code>.  All three subelements are optional.
 *
 * Both <code>&lt;then&gt;</code> and <code>&lt;else&gt;</code> must not be
 * used more than once inside the if task.  Both are
 * containers for Ant tasks, just like Ant's
 * <code>&lt;parallel&gt;</code> and <code>&lt;sequential&gt;</code>
 * tasks - in fact they are implemented using the same class as Ant's
 * <code>&lt;sequential&gt;</code> task.</p>
 *
 *  The <code>&lt;elseif&gt;</code> behaves exactly like an <code>&lt;if&gt;</code>
 * except that it cannot contain the <code>&lt;else&gt;</code> element
 * inside of it.  You may specify as may of these as you like, and the
 * order they are specified is the order they are evaluated in.  If the
 * condition on the <code>&lt;if&gt;</code> is false, then the first
 * <code>&lt;elseif&gt;</code> who's conditional evaluates to true
 * will be executed.  The <code>&lt;else&gt;</code> will be executed
 * only if the <code>&lt;if&gt;</code> and all <code>&lt;elseif&gt;</code>
 * conditions are false.
 *
 * <p>Use the following task to define the <code>&lt;if&gt;</code>
 * task before you use it the first time:</p>
 *
 * <pre><code>
 *   &lt;taskdef name="if" classname="net.sf.antcontrib.logic.IfTask" /&gt;
 * </code></pre>
 *
 * <h3>Crude Example</h3>
 *
 * <pre><code>
 * &lt;if&gt;
 *  &lt;equals arg1=&quot;${foo}&quot; arg2=&quot;bar&quot; /&gt;
 *  &lt;then&gt;
 *    &lt;echo message=&quot;The value of property foo is bar&quot; /&gt;
 *  &lt;/then&gt;
 *  &lt;else&gt;
 *    &lt;echo message=&quot;The value of property foo is not bar&quot; /&gt;
 *  &lt;/else&gt;
 * &lt;/if&gt;
 * </code>
 *
 * <code>
 * &lt;if&gt;
 *  &lt;equals arg1=&quot;${foo}&quot; arg2=&quot;bar&quot; /&gt;
 *  &lt;then&gt;
 *   &lt;echo message=&quot;The value of property foo is 'bar'&quot; /&gt;
 *  &lt;/then&gt;
 *
 *  &lt;elseif&gt;
 *   &lt;equals arg1=&quot;${foo}&quot; arg2=&quot;foo&quot; /&gt;
 *   &lt;then&gt;
 *    &lt;echo message=&quot;The value of property foo is 'foo'&quot; /&gt;
 *   &lt;/then&gt;
 *  &lt;/elseif&gt;
 *
 *  &lt;else&gt;
 *   &lt;echo message=&quot;The value of property foo is not 'foo' or 'bar'&quot; /&gt;
 *  &lt;/else&gt;
 * &lt;/if&gt;
 * </code></pre>
 *
 * @author <a href="mailto:stefan.bodewig@freenet.de">Stefan Bodewig</a>
 */
public class IfTask extends ConditionBase {

    public static final class ElseIf
        extends ConditionBase
    {
        private Sequential thenTasks = null;

        public void addThen(Sequential t)
        {
            if (thenTasks != null)
            {
                throw new BuildException("You must not nest more than one <then> into <elseif>");
            }
            thenTasks = t;
        }

        public boolean eval()
            throws BuildException
        {
            if (countConditions() > 1) {
                throw new BuildException("You must not nest more than one condition into <elseif>");
            }
            if (countConditions() < 1) {
                throw new BuildException("You must nest a condition into <elseif>");
            }
            Condition c = (Condition) getConditions().nextElement();

            return c.eval();
        }

        public void execute()
            throws BuildException
        {
            if (thenTasks != null)
            {
                thenTasks.execute();
            }
        }
    }

    private Sequential thenTasks = null;
    private Vector     elseIfTasks = new Vector();
    private Sequential elseTasks = null;

    /***
     * A nested Else if task
     */
    public void addElseIf(ElseIf ei)
    {
        elseIfTasks.addElement(ei);
    }

    /**
     * A nested &lt;then&gt; element - a container of tasks that will
     * be run if the condition holds true.
     *
     * <p>Not required.</p>
     */
    public void addThen(Sequential t) {
        if (thenTasks != null) {
            throw new BuildException("You must not nest more than one <then> into <if>");
        }
        thenTasks = t;
    }

    /**
     * A nested &lt;else&gt; element - a container of tasks that will
     * be run if the condition doesn't hold true.
     *
     * <p>Not required.</p>
     */
    public void addElse(Sequential e) {
        if (elseTasks != null) {
            throw new BuildException("You must not nest more than one <else> into <if>");
        }
        elseTasks = e;
    }

    public void execute() throws BuildException {
        if (countConditions() > 1) {
            throw new BuildException("You must not nest more than one condition into <if>");
        }
        if (countConditions() < 1) {
            throw new BuildException("You must nest a condition into <if>");
        }
        Condition c = (Condition) getConditions().nextElement();
        if (c.eval()) {
            if (thenTasks != null) {
                thenTasks.execute();
            }
        }
        else
        {
            boolean done = false;
            int sz = elseIfTasks.size();
            for (int i=0;i<sz;i++)
            {

                ElseIf ei = (ElseIf)(elseIfTasks.elementAt(i));
                if (ei.eval())
                {
                    done = true;
                    ei.execute();
                }
            }

            if (!done && elseTasks != null)
            {
                elseTasks.execute();
            }
        }
    }
}
