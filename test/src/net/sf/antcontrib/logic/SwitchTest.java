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

import org.apache.tools.ant.BuildFileTest;

/**
 * Testcase for <switch>.
 */
public class SwitchTest extends BuildFileTest {

    public SwitchTest(String name) {
        super(name);
    }

    public void setUp() {
        configureProject("test/resources/logic/switch.xml");
    }

    public void testNoValue() {
        expectSpecificBuildException("noValue", "no value",
                                     "Value is missing");
    }        
        
    public void testNoChildren() {
        expectSpecificBuildException("noChildren", "no children",
                                     "No cases supplied");
    }

    public void testTwoDefaults() {
        expectSpecificBuildException("twoDefaults", "two defaults",
                                     "Cannot specify multiple default cases");
    }

    public void testNoMatch() {
        expectSpecificBuildException("noMatch", "no match",
                                     "No case matched the value foo"
                                     + " and no default has been specified.");
    }

    public void testCaseNoValue() {
        expectSpecificBuildException("caseNoValue", "<case> no value",
                                     "Value is required for case.");
    }

    public void testDefault() {
        executeTarget("testDefault");
        assertTrue(getLog().indexOf("In default") > -1);
        assertTrue(getLog().indexOf("baz") > -1);
        assertEquals(-1, getLog().indexOf("${inner}"));
        assertEquals(-1, getLog().indexOf("In case"));
    }

    public void testCase() {
        executeTarget("testCase");
        assertTrue(getLog().indexOf("In case") > -1);
        assertTrue(getLog().indexOf("baz") > -1);
        assertEquals(-1, getLog().indexOf("${inner}"));
        assertEquals(-1, getLog().indexOf("In default"));
    }

    public void testCaseSensitive() {
        executeTarget("testCaseSensitive");
        assertTrue(getLog().indexOf("In default") > -1);
        assertEquals(-1, getLog().indexOf("In case"));
    }

    public void testCaseInSensitive() {
        executeTarget("testCaseInSensitive");
        assertTrue(getLog().indexOf("In case") > -1);
        assertEquals(-1, getLog().indexOf("In default"));
    }

}
