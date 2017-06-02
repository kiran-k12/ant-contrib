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

import org.apache.tools.ant.BuildFileTest;

/**
 * Testcase for <if>.
 */
public class IfTaskTest extends BuildFileTest {

    public IfTaskTest(String name) {
        super(name);
    }

    public void setUp() {
        configureProject("test/resources/logic/if.xml");
    }

    public void testNoCondition() {
        expectSpecificBuildException("noCondition", "no condition",
                                     "You must nest a condition into <if>");
    }        
        
    public void testTwoConditions() {
        expectSpecificBuildException("twoConditions", "two conditions",
                                     "You must not nest more than one "
                                     + "condition into <if>");
    }

    public void testNothingToDo() {
        expectLog("nothingToDo", "");
    }

    public void testTwoThens() {
        expectSpecificBuildException("twoThens", "two <then>s",
                                     "You must not nest more than one "
                                     + "<then> into <if>");
    }

    public void testTwoElses() {
        expectSpecificBuildException("twoElses", "two <else>s",
                                     "You must not nest more than one "
                                     + "<else> into <if>");
    }

    public void testNormalOperation() {
        executeTarget("normalOperation");
        assertTrue(getLog().indexOf("In then") > -1);
        assertTrue(getLog().indexOf("some value") > -1);
        assertEquals(-1, getLog().indexOf("${inner}"));
        assertEquals(-1, getLog().indexOf("In else"));
    }

    public void testNormalOperation2() {
        executeTarget("normalOperation2");
        assertTrue(getLog().indexOf("In else") > -1);
        assertEquals(-1, getLog().indexOf("In then"));
    }

    public void testNoConditionInElseif() {
        expectSpecificBuildException("noConditionInElseif", "no condition",
                                     "You must nest a condition into <elseif>");
    }

    public void testTwoConditionInElseif() {
        expectSpecificBuildException("twoConditionsInElseif", "two conditions",
                                     "You must not nest more than one "
                                     + "condition into <elseif>");
    }

    public void testNormalOperationElseif() {
        executeTarget("normalOperationElseif");
        assertTrue(getLog().indexOf("In elseif") > -1);
        assertEquals(-1, getLog().indexOf("In then"));
        assertEquals(-1, getLog().indexOf("In else-branch"));
    }

    public void testNormalOperationElseif2() {
        executeTarget("normalOperationElseif2");
        assertTrue(getLog().indexOf("In else-branch") > -1);
        assertEquals(-1, getLog().indexOf("In then"));
        assertEquals(-1, getLog().indexOf("In elseif"));
    }

}
