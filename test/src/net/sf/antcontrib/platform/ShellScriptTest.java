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

package net.sf.antcontrib.platform;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;

/**
 * Testcase for <shellscript>
 *
 * @author Peter Reilly
 */
public class ShellScriptTest extends BuildFileTest {
    public ShellScriptTest(String name) {
        super(name);
    }

    public void setUp() {
        configureProject("test/resources/platform/shellscript.xml");
        staticInitialize();
    }

    public void testShHello() {
        if (! hasSh)
            return;
        executeTarget("sh.hello");
        assertTrue(getLog().indexOf("hello world") > -1);
     }

    public void testBashHello() {
        if (! hasBash)
            return;
        executeTarget("bash.hello");
        assertTrue(getLog().indexOf("hello world") > -1);
     }

    public void testShInputString() {
        if (! hasSh)
            return;
        executeTarget("sh.inputstring");
        assertTrue(getLog().indexOf("hello world") > -1);
     }

    public void testShProperty() {
        if (! hasSh)
            return;
        executeTarget("sh.property");
        assertTrue(getLog().indexOf("this is a property") > -1);
     }


    public void testPythonHello() {
        if (! hasPython)
            return;
        executeTarget("python.hello");
        assertTrue(getLog().indexOf("hello world") > -1);
    }

    public void testPerlHello() {
        if (! hasPerl)
            return;
        executeTarget("perl.hello");
        assertTrue(getLog().indexOf("hello world") > -1);
    }

    public void testNoShell() {
        expectBuildExceptionContaining(
            "noshell", "Execute failed", "a shell that should not exist");
    }

    public void testSed() {
        if (! hasSed)
            return;
        executeTarget("sed.test");
        assertTrue(getLog().indexOf("BAR bar bar bar BAR bar") > -1);
    }

    public void testSetProperty() {
        if (! hasSh)
            return;
        executeTarget("sh.set.property");
        assertPropertyEquals("sh.set.property", "hello world");
    }

    public void testTmpSuffix() {
        if (! hasSh)
            return;
        executeTarget("sh.tmp.suffix");
        assertTrue(getLog().indexOf(".bat") > -1);
    }

    public void testCmd() {
        if (! hasCmd)
            return;
        executeTarget("cmd.test");
        assertTrue(getLog().indexOf("hello world") > -1);
    }

    public void testDir() {
        if (! hasBash)
            return;
        executeTarget("dir.test");
        assertTrue(
            getProject().getProperty("dir.test.property")
            .indexOf("subdir") > -1);
    }

    public void testCommand() {
        expectBuildExceptionContaining(
            "command.test", "Attribute failed",
            "Attribute command is not supported");
    }
    
    private static boolean initialized = false;
    private static boolean hasSh       = false;
    private static boolean hasBash     = false;
    private static boolean hasPython   = false;
    private static boolean hasPerl     = false;
    private static boolean hasSed      = false;
    private static boolean hasCmd      = false;
    private static Object staticMonitor = new Object();
    
    /**
     * check if the env contains the shells
     *    sh, bash, python and perl
     *    assume cmd.exe exists for windows
     */
    private void staticInitialize() {
        synchronized (staticMonitor) {
            if (initialized)
                return;
            initialized = true;
            hasSh = hasShell("hassh");
            hasBash = hasShell("hasbash");
            hasPerl = hasShell("hasperl");
            hasPython = hasShell("haspython");
            hasSed = hasShell("hassed");
            hasCmd = hasShell("hascmd");
            
        }
    }

    private boolean hasShell(String target) {
        try {
            executeTarget(target);
            return true;
        }
        catch (Throwable t) {
            return false;
        }
    }
        
}
