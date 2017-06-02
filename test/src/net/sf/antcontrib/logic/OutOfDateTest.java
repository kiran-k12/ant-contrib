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

package net.sf.antcontrib.logic;

import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.BuildFileTest;

/**
 * Testcase for <outofdate>.
 *
 * @author Peter Reilly
 */
public class OutOfDateTest extends BuildFileTest {

    public OutOfDateTest(String name) {
        super(name);
    }

    public void setUp() {
        configureProject("test/resources/logic/outofdate.xml");
    }

    public void tearDown() {
        executeTarget("cleanup");
    }
    
    public void testSimple() {
        executeTarget("simple");
    }
    
    public void testVerbose() {
        executeTarget("verbose");
        assertTrue(getLog().indexOf("outofdate with regard to") > -1);
    }
    
    public void testDelete() {
        executeTarget("delete");
    }
    
    public void testDeleteAll() {
        executeTarget("delete-all");
    }
    
    public void testDeleteQuiet() {
        executeTarget("init");
        executeTarget("delete-quiet");
        assertTrue("No deleting message", getLog().indexOf("Deleting") == -1);
    }
    
    public void testFileset() {
        executeTarget("outofdate.init");
        executeTarget("outofdate.test");
        assertTrue(getLog().indexOf("outofdate triggered") > -1);
        String outofdateSources =
            getProject().getProperty("outofdate.sources");
        // switch \ to / if present
        outofdateSources.replace('\\', '/');
        assertTrue("newer.text empty", outofdateSources.indexOf(
                       "newer.text") > -1);
        assertTrue("file.notdone", outofdateSources.indexOf(
                       "outofdate/source/1/2/file.notdone") > -1);
        assertTrue("file.done", outofdateSources.indexOf(
                       "outofdate/source/1/2/file.done") == -1);
        assertTrue("done.y", outofdateSources.indexOf(
                       "outofdate/source/1/done.y") == -1);
        assertTrue("partial.y", outofdateSources.indexOf(
                       "outofdate/source/1/partial.y") > -1);
        String outofdateTargets =
            getProject().getProperty("outofdate.targets");
        assertTrue(outofdateTargets.indexOf(
                       "outofdate.xml") > -1);
        assertTrue(outofdateTargets.indexOf(
                       "outofdate/gen/1/2/file.notdone") > -1);
        assertTrue(outofdateTargets.indexOf(
                       "outofdate/gen/1/partial.h") > -1);
        assertTrue(outofdateTargets.indexOf(
                       "outofdate/gen/1/partial.c") == -1);
        assertTrue(outofdateTargets.indexOf(
                       "outofdate/gen/1/done.h") == -1);

        Path sourcesPath = (Path) getProject().getReference(
            "outofdate.sources.path");
        assertTrue(sourcesPath != null);
        String[] sources = sourcesPath.list();
        assertTrue(sources.length == 3);
        Path targetsPath = (Path) getProject().getReference(
            "outofdate.targets.path");
        String[] targets = targetsPath.list();
        assertTrue(targetsPath != null);
        assertTrue(targets.length == 3);
    }

    public void testEmptySources() {
        executeTarget("empty-sources");
    }
    
}
