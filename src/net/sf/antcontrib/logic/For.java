/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 Ant-Contrib project.  All rights
 * reserved.
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
import java.util.StringTokenizer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.types.Path;

/***
 * Task definition for the for task.  This is based on
 * the foreach task but takes a sequential element
 * instead of a target and only works for ant >= 1.6Beta3
 * @author Peter Reilly
 */
public class For extends Task  {
    private static boolean antVersionOk = true;
    static {
        try {
            Class c = Class.forName(
                "org.apache.tools.ant.taskdefs.MacroInstance");
            Class c2 = Class.forName(
                "org.apache.tools.ant.taskdefs.MacroDef");
            Method m = c2.getMethod("createSequential", new Class[]{});
        } catch (Throwable t) {
            antVersionOk = false;
        }
    }

    private String     list;
    private String     param;
    private String     delimiter = ",";
    private Path       currPath;
    private boolean    trim;
    private Reflector  macroDef;
    private Object     nestedSequential;

    /**
     * Creates a new <code>For</code> instance.
     * This checks if the ant version is correct to run this task.
     */
    public For() {
        if (!antVersionOk) {
            throw new BuildException(
                "This task only works for ant >= 1.6Beta3");
        }
    }

    /**
     * Set the trim attribute.
     *
     * @param trim if true, trim the value for each iterator.
     */
    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    /**
     * Set the list attribute.
     *
     * @param list a list of delimiter separated tokens.
     */
    public void setList(String list) {
        this.list = list;
    }

    /**
     * Set the delimiter attribute.
     *
     * @param delimiter the delimitier used to separate the tokens in
     *        the list attribute. The default is ",".
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Set the param attribute.
     * This is the name of the macrodef attribute that
     * gets set for each iterator of the sequential element.
     *
     * @param param the name of the macrodef attribute.
     */
    public void setParam(String param) {
        this.param = param;
    }

    /**
     * This is a path that can be used instread of the list
     * attribute to interate over. If this is set, each
     * path element in the path is used for an interator of the
     * sequential element.
     *
     * @return the path to be set by the ant script.
     */
    public Path createPath() {
        if (currPath == null) {
            currPath = new Path(getProject());
        }
        return currPath;
    }

    private static class Reflector {
        private Object obj;
        public Reflector(String name) {
            try {
                Class clazz;
                clazz = Class.forName(name);
                Constructor constructor;
                constructor = clazz.getConstructor(new Class[]{});
                obj = constructor.newInstance(new Object[]{});
            } catch (Throwable t) {
                throw new BuildException(t);
            }
        }
        public Reflector(Object obj) {
            this.obj = obj;
        }
        public Object getObject() {
            return obj;
        }
        public Object call(String methodName) {
            try {
                Method method;
                method = obj.getClass().getMethod(
                    methodName, new Class[] {});
                return method.invoke(obj, new Object[] {});
            } catch (InvocationTargetException t) {
                Throwable t2 = t.getTargetException();
                if (t2 instanceof BuildException) {
                    throw (BuildException) t2;
                }
                throw new BuildException(t2);
            } catch (Throwable t) {
                throw new BuildException(t);
            }
        }

        public Object callExplicit(
            String methodName, String className, Object o) {
            try {
                Method method;
                Class clazz = Class.forName(className);
                method = obj.getClass().getMethod(
                    methodName, new Class[] {clazz});
                return method.invoke(obj, new Object[] {o});
            } catch (InvocationTargetException t) {
                Throwable t2 = t.getTargetException();
                if (t2 instanceof BuildException) {
                    throw (BuildException) t2;
                }
                throw new BuildException(t2);
            } catch (Throwable t) {
                throw new BuildException(t);
            }
        }
        public Object call(String methodName, Object o) {
            try {
                Method method;
                method = obj.getClass().getMethod(
                    methodName, new Class[] {o.getClass()});
                return method.invoke(obj, new Object[] {o});
            } catch (InvocationTargetException t) {
                Throwable t2 = t.getTargetException();
                if (t2 instanceof BuildException) {
                    throw (BuildException) t2;
                }
                throw new BuildException(t2);
            } catch (Throwable t) {
                throw new BuildException(t);
            }
        }
        public Object call(String methodName, Object o1, Object o2) {
            try {
                Method method;
                method = obj.getClass().getMethod(
                    methodName, new Class[] {o1.getClass(), o2.getClass()});
                return method.invoke(obj, new Object[] {o1, o2});
            } catch (InvocationTargetException t) {
                Throwable t2 = t.getTargetException();
                if (t2 instanceof BuildException) {
                    throw (BuildException) t2;
                }
                throw new BuildException(t2);
            } catch (Throwable t) {
                throw new BuildException(t);
            }
        }
    };
    
    /**
     * @return a MacroDef#NestedSequential object to be configured
     */
    public Object createSequential() {
        macroDef = new Reflector("org.apache.tools.ant.taskdefs.MacroDef");
        macroDef.call("setProject", getProject());
        return macroDef.call("createSequential");
    }

    /**
     * Run the for task.
     * This checks the attributes and nested elements, and
     * if there are ok, it calls doTheTasks()
     * which constructes a macrodef task and a
     * for each interation a macrodef instance.
     */
    public void execute() {
        if (list == null && currPath == null) {
            throw new BuildException(
                "You must have a list or path to iterate through");
        }
        if (param == null) {
            throw new BuildException(
                "You must supply a property name to set on"
                + " each iteration in param");
        }
        if (macroDef == null) {
            throw new BuildException(
                "You must supply an embedded sequential "
                + "to perform");
        }
        doTheTasks();
    }


    private void doSequentialIteration(String val) {
        Reflector instance = new Reflector(
            "org.apache.tools.ant.taskdefs.MacroInstance");
        instance.call("setProject", getProject());
        instance.call("setOwningTarget", getOwningTarget());
        instance.call("setMacroDef", macroDef.getObject());
        instance.call("setDynamicAttribute", param, val);
        instance.call("execute");
    }

    private void doTheTasks() {
        // Create a macro attribute
        Reflector attribute = new Reflector(
            "org.apache.tools.ant.taskdefs.MacroDef$Attribute");
        attribute.call("setName", param);
        macroDef.call("addConfiguredAttribute", attribute.getObject());
        // Take Care of the list attribute
        if (list != null) {
            StringTokenizer st = new StringTokenizer(list, delimiter);
            String[] toks = new String[st.countTokens()];
            int i = 0;

            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                if (trim) {
                    tok = tok.trim();
                }
                doSequentialIteration(tok);
            }
        }

        String[] pathElements = new String[0];
        if (currPath != null) {
            pathElements = currPath.list();
        }
        for (int i = 0; i < pathElements.length; i++) {
            File nextFile = new File(pathElements[i]);
            doSequentialIteration(nextFile.getAbsolutePath());
        }
    }

}


