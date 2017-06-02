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
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;


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
 *     &lt;taskdef name="latesttimestamp" classname="net.sf.antcontrib.logic.TimestampSelector" /&gt;
 *   </code>
 *
 *   Call Syntax:
 *   <code>
 *     &lt;timestampselector
 *                 [property="prop" | outputsetref="id"]
 *                 [count="num"]
 *                 [age="eldest|youngest"]
 *                 [pathSep=","]
 *                 [pathref="ref"] &gt;
 *       &lt;path&gt;
 *          ...
 *       &lt;/path&gt;
 *     &lt;/latesttimestamp&gt;
 *   </code>
 *
 *   Attributes:
 *         outputsetref --> The reference of the output Path set which will contain the
 *                          files with the latest timestamps.
 *         property  --> The name of the property to set with file having the latest
 *                       timestamp.  If you specify the "count" attribute, you will get
 *                       the lastest N files.  These will be the absolute pathnames
 *         count     --> How many of the latest files do you wish to find
 *         pathSep   --> What to use as the path separator when using the "property"
 *                       attribute, in conjunction with the "count" attribute
 *         pathref   --> The reference of the path which is the input set of files.
 *
 * </pre>
 * @author <a href="mailto:mattinger@mindless.com">Matthew Inger</a>
 */
public class TimestampSelector extends Task
{
    private static final String AGE_ELDEST = "eldest";
    private static final String AGE_YOUNGEST = "youngest";

    private String property;
    private Path path;
    private String outputSetId;
    private int count = 1;
    private char pathSep = ',';
    private String age = AGE_YOUNGEST;


    /***
     * Default Constructor
     */
    public TimestampSelector()
    {
        super();
    }


    public void doFileSetExecute(String paths[])
        throws BuildException
    {

    }

        // Sorts entire array
    public void sort(Vector array)
    {
        sort(array, 0, array.size() - 1);
    }

    // Sorts partial array
    protected void sort(Vector array, int start, int end)
    {
        int p;
        if (end > start)
        {
            p = partition(array, start, end);
            sort(array, start, p-1);
            sort(array, p+1, end);
        }
    }

    protected int compare(File a, File b)
    {
        if (age.equalsIgnoreCase(AGE_ELDEST))
            return new Long(a.lastModified()).compareTo(new Long(b.lastModified()));
        else
            return new Long(b.lastModified()).compareTo(new Long(a.lastModified()));
    }

    protected int partition(Vector array, int start, int end)
    {
        int left, right;
        File partitionElement;

        partitionElement = (File)array.elementAt(end);

        left = start - 1;
        right = end;
        for (;;)
        {
            while (compare(partitionElement, (File)array.elementAt(++left)) == 1)
            {
                if (left == end) break;
            }
            while (compare(partitionElement, (File)array.elementAt(--right)) == -1)
            {
                if (right == start) break;
            }
            if (left >= right) break;
            swap(array, left, right);
        }
        swap(array, left, end);

        return left;
    }

    protected void swap(Vector array, int i, int j)
    {
        Object temp;

        temp = array.elementAt(i);
        array.setElementAt(array.elementAt(j), i);
        array.setElementAt(temp, j);
    }

    public void execute()
            throws BuildException
    {
        if (property == null && outputSetId == null)
            throw new BuildException("Property or OutputSetId must be specified.");
        if (path == null)
            throw new BuildException("A path element or pathref attribute must be specified.");

        // Figure out the list of existing file elements
        // from the designated path
        String s[] = path.list();
        Vector v = new Vector();
        for (int i=0;i<s.length;i++)
        {
            File f = new File(s[i]);
            if (f.exists())
                v.addElement(f);
        }

        // Sort the vector, need to make java 1.1 compliant
        sort(v);

        // Pull off the first N items
        Vector v2 = new Vector();
        int sz = v.size();
        for (int i=0;i<sz && i<count;i++)
            v2.add(v.elementAt(i));




        // Build the resulting Path object
        Path path = new Path(getProject());
        sz = v2.size();
        for (int i=0;i<sz;i++)
        {
            File f = (File)(v.elementAt(i));
            Path p = new Path(getProject(), f.getAbsolutePath());
            path.addExisting(p);
        }


        if (outputSetId != null)
        {
            // Add the reference to the project
            project.addReference(outputSetId, path);
        }
        else
        {
            // Concat the paths, and put them in a property
            // which is separated list of the files, using the
            // "pathSep" attribute as the separator
            String paths[] = path.list();
            StringBuffer sb = new StringBuffer();
            for (int i=0;i<paths.length;i++)
            {
                if (i != 0) sb.append(pathSep);
                sb.append(paths[i]);
            }

            if (paths.length != 0)
                project.setProperty(property, sb.toString());
        }
    }


    public void setProperty(String property)
    {
        if (outputSetId != null)
            throw new BuildException("Cannot set both Property and OutputSetId.");

        this.property = property;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public void setAge(String age)
    {
        if (age.equalsIgnoreCase(AGE_ELDEST) ||
            age.equalsIgnoreCase(AGE_YOUNGEST))
            this.age = age;
        else
            throw new BuildException("Invalid age: " + age);
    }

    public void setPathSep(char pathSep)
    {
        this.pathSep = pathSep;
    }

    public void setOutputSetId(String outputSetId)
    {
        if (property != null)
            throw new BuildException("Cannot set both Property and OutputSetId.");
        this.outputSetId = outputSetId;
    }

    public void setPathRef(Reference ref)
            throws BuildException
    {
        if (path == null)
        {
            path = new Path(getProject());
            path.setRefid(ref);
        }
        else
        {
            throw new BuildException("Path element already specified.");
        }
    }


    public Path createPath()
            throws BuildException
    {
        if (path == null)
            path = new Path(getProject());
        else
            throw new BuildException("Path element already specified.");
        return path;
    }

}


