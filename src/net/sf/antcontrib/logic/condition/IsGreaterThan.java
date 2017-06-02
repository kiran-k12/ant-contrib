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
//package ise.antelope.tasks;
package net.sf.antcontrib.logic.condition;

import org.apache.tools.ant.taskdefs.condition.Equals;
import org.apache.tools.ant.BuildException;

/**
 * Extends Equals condition to test if the first argument is greater than the
 * second argument. Will deal with base 10 integer and decimal numbers, otherwise,
 * treats arguments as Strings.
 * <p>Developed for use with Antelope, migrated to ant-contrib Oct 2003.
 *
 * @author     Dale Anson, danson@germane-software.com
 * @version $Revision: 1.3 $
 */
public class IsGreaterThan extends Equals {

    private String arg1, arg2;
    private boolean trim = false;
    private boolean caseSensitive = true;

    public void setArg1(String a1) {
        arg1 = a1;
    }

    public void setArg2(String a2) {
        arg2 = a2;
    }

    /**
     * Should we want to trim the arguments before comparing them?
     *
     * @since Revision: 1.3, Ant 1.5
     */
    public void setTrim(boolean b) {
        trim = b;
    }

    /**
     * Should the comparison be case sensitive?
     *
     * @since Revision: 1.3, Ant 1.5
     */
    public void setCasesensitive(boolean b) {
        caseSensitive = b;
    }

    public boolean eval() throws BuildException {
        if (arg1 == null || arg2 == null) {
            throw new BuildException("both arg1 and arg2 are required in "
                                     + "greater than");
        }

        if (trim) {
            arg1 = arg1.trim();
            arg2 = arg2.trim();
        }
        
        // check if args are numbers
        try {
            double num1 = Double.parseDouble(arg1);
            double num2 = Double.parseDouble(arg2);
            return num1 > num2;
        }
        catch(NumberFormatException nfe) {
            // ignored, fall thru to string comparision       
        }
        
        return caseSensitive ? arg1.compareTo(arg2) > 0 : arg1.compareToIgnoreCase(arg2) > 0;
    }

}
