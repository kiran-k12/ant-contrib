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

import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;
import java.io.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Reference;

/****************************************************************************
 * Place class description here.
 *
 * @author		inger
 * @author		<additional author>
 *
 * @since
 *
 ****************************************************************************/


public class SortList
        extends AbstractPropertySetterTask
{
    private String value;
    private Reference ref;
    private boolean casesensitive = true;
    private boolean numeric = false;
    private String delimiter = ",";
    private File orderPropertyFile;
    private String orderPropertyFilePrefix;

    public SortList()
    {
        super();
    }

    public void setNumeric(boolean numeric)
    {
        this.numeric = numeric;
    }

    public void setValue(String value)
    {
        this.value = value;
    }


    public void setRefid(Reference ref)
    {
        this.ref = ref;
    }


    public void setCasesensitive(boolean casesenstive)
    {
        this.casesensitive = casesenstive;
    }

    public void setDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }


    public void setOrderPropertyFile(File orderPropertyFile)
    {
        this.orderPropertyFile = orderPropertyFile;
    }


    public void setOrderPropertyFilePrefix(String orderPropertyFilePrefix)
    {
        this.orderPropertyFilePrefix = orderPropertyFilePrefix;
    }


    private static void mergeSort(String src[],
                                  String dest[],
                                  int low,
                                  int high,
                                  boolean numeric) {
        int length = high - low;

        // Insertion sort on smallest arrays
        if (length < 7) {
            for (int i=low; i<high; i++)
                for (int j=i; j>low &&
                        compare(dest[j-1],dest[j],numeric)>0; j--)
                    swap(dest, j, j-1);
            return;
        }

        // Recursively sort halves of dest into src
        int mid = (low + high)/2;
        mergeSort(dest, src, low, mid, numeric);
        mergeSort(dest, src, mid, high, numeric);

        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (compare(src[mid-1], src[mid], numeric) <= 0) {
            System.arraycopy(src, low, dest, low, length);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for(int i = low, p = low, q = mid; i < high; i++) {
            if (q>=high || p<mid && compare(src[p], src[q], numeric)<=0)
                dest[i] = src[p++];
            else
                dest[i] = src[q++];
        }
    }

    private static int compare(String s1,
                               String s2,
                               boolean numeric)
    {
        int res = 0;

        if (numeric)
        {
            double d1 = new Double(s1).doubleValue();
            double d2 = new Double(s2).doubleValue();
            if (d1 < d2)
                res = -1;
            else if (d1 == d2)
                res = 0;
            else
                res = 1;
        }
        else
        {
            res = s1.compareTo(s2);
        }

        return res;
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(Object x[], int a, int b) {
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
    }


    private Vector sortByOrderPropertyFile(Vector props)
        throws IOException
    {
        FileReader fr = null;
        Vector orderedProps = new Vector();

        try
        {
            fr = new FileReader(orderPropertyFile);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            String pname = "";
            int pos = 0;
            while ((line = br.readLine()) != null)
            {
                pos = line.indexOf('#');
                if (pos != -1)
                    line = line.substring(0, pos).trim();

                if (line.length() > 0)
                {
                    pos = line.indexOf('=');
                    if (pos != -1)
                        pname = line.substring(0,pos).trim();
                    else
                        pname = line.trim();

                    String prefPname = pname;
                    if (orderPropertyFilePrefix != null)
                        prefPname = orderPropertyFilePrefix + "." + prefPname;

                    if (props.contains(prefPname) &&
                        ! orderedProps.contains(prefPname))
                    {
                        orderedProps.addElement(prefPname);
                    }
                }
            }

            Enumeration e = props.elements();
            while (e.hasMoreElements())
            {
                String prop = (String)(e.nextElement());
                if (! orderedProps.contains(prop))
                    orderedProps.addElement(prop);
            }

            return orderedProps;
        }
        finally
        {
            try
            {
                if (fr != null)
                    fr.close();
            }
            catch (IOException e)
            {
                ; // gulp
            }
        }
    }

    protected void validate()
    {
        super.validate();
    }

    public void execute()
    {
        validate();

        String val = value;
        if (val == null && ref != null)
            val = ref.getReferencedObject(project).toString();

        if (val == null)
            throw new BuildException("Either the 'Value' or 'Refid' attribute must be set.");

        StringTokenizer st = new StringTokenizer(val, delimiter);
        Vector vec = new Vector(st.countTokens());
        while (st.hasMoreTokens())
            vec.addElement(st.nextToken());


        String propList[] = null;

        if (orderPropertyFile != null)
        {
            try
            {
                Vector sorted = sortByOrderPropertyFile(vec);
                propList = new String[sorted.size()];
                sorted.copyInto(propList);
            }
            catch (IOException e)
            {
                throw new BuildException(e);
            }
        }
        else
        {
            String s[] = (String[])(vec.toArray(new String[vec.size()]));
            if (! casesensitive)
            {
                for (int i=0;i<s.length;i++)
                    s[i] = s[i].toLowerCase();
            }

            propList = new String[s.length];
            System.arraycopy(s, 0, propList, 0, s.length);
            mergeSort(s, propList, 0, s.length, numeric);
        }

        StringBuffer sb = new StringBuffer();
        for (int i=0;i<propList.length;i++)
        {
            if (i != 0) sb.append(delimiter);
            sb.append(propList[i]);
        }

        setPropertyValue(sb.toString());
    }
}
