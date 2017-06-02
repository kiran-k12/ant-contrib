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
package net.sf.antcontrib.walls;

import java.io.File;
import java.io.PrintStream;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.BuildListener;

/**
 * BIG NOTE***************************************************
 * Always expect specific exceptions.  Most of these test cases when
 * first submitted were not and therefore were not testing what they said
 * they were testing.  Exceptions were being caused by other things and the
 * tests were still passing.  Now all tests expect a specific exception
 * so if any other is thrown we will fail the test case.
 * ************************************************************
 * 
 * Testcase for <propertycopy>.
 */
public class CompileWithWallsTest extends BuildFileTest {

    private String baseDir = "test"+File.separator
                            +"resources"+File.separator
                            +"walls"+File.separator;
    private String c = File.separator;
    
    public CompileWithWallsTest(String name) {
        super(name);
    }

    public void setUp() {    
   
        configureProject("test/resources/walls/compilewithwalls.xml");
//        project.addBuildListener(new LogListener()); 
    }
//    protected class LogListener implements BuildListener {
//
//        /* (non-Javadoc)
//         * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
//         */
//        public void buildStarted(BuildEvent event) {
//            // TODO Auto-generated method stub
//            
//        }
//
//        /* (non-Javadoc)
//         * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
//         */
//        public void buildFinished(BuildEvent event) {
//            // TODO Auto-generated method stub
//            
//        }
//
//        /* (non-Javadoc)
//         * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
//         */
//        public void targetStarted(BuildEvent event) {
//            // TODO Auto-generated method stub
//            
//        }
//
//        /* (non-Javadoc)
//         * @see org.apache.tools.ant.BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
//         */
//        public void targetFinished(BuildEvent event) {
//            // TODO Auto-generated method stub
//            
//        }
//
//        /* (non-Javadoc)
//         * @see org.apache.tools.ant.BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
//         */
//        public void taskStarted(BuildEvent event) {
//            // TODO Auto-generated method stub
//            
//        }
//
//        /* (non-Javadoc)
//         * @see org.apache.tools.ant.BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
//         */
//        public void taskFinished(BuildEvent event) {
//            // TODO Auto-generated method stub
//            
//        }
//
//        /* (non-Javadoc)
//         * @see org.apache.tools.ant.BuildListener#messageLogged(org.apache.tools.ant.BuildEvent)
//         */
//        public void messageLogged(BuildEvent event) {
//
//            System.out.println(event.getException());
//            System.out.println("aaa");
//        }    
//        
//    }
    
    public void tearDown() {
        executeTarget("cleanup");

//        System.out.println(getFullLog());
//        System.out.println("std out. from ant build begin--------------");
//        System.out.println(getOutput());
//        System.out.println("std.out. from ant build end----------------");        
//        System.out.println("std err. from ant build begin--------------");
//        System.out.println(getError());
//        System.out.println("std.err. from ant build end----------------");
    }

    public void testTooManyNestedWallElements() {       
        expectSpecificBuildException("testTooManyNestedWallElements"
                , "TooManyNestedWallElements"
                , "compilewithwalls task only supports one nested walls element or one walls attribute");       
    }

    public void testTooManyNestedJavacElements() {
        expectSpecificBuildException("testTooManyNestedJavacElements"
                , "TooManyNestedJavacElements"
                , "compilewithwalls task only supports one nested javac element");
    }

    public void testNoWallElement() {
        expectSpecificBuildException("testNoWallElement"
                , "NoWallElement"
                , "There must be a nested walls element");
    }

    public void testNoJavacElement() {
        expectSpecificBuildException("testNoJavacElement"
                , "NoJavacElement"
                , "There must be a nested javac element");
    }

    public void testMoreThanOneSrcDirInJavac() {
        expectSpecificBuildException("testMoreThanOneSrcDirInJavac"
            ,"MoreThanOneSrcDirInJavac"
            ,"srcdir in javac task must contain one and only one "
            + "source directory with the compilewithwalls task\n"
            + "Your javac srcdir contains "
            + "2"
            + " source directories");
    }

    public void testNoSrcDirInJavac() {
        expectSpecificBuildException("testNoSrcDirInJavac"
                , "NoSrcDirInJavac"
                , "Javac inside compilewithwalls must have a srcdir specified");
    }

    public void testIntermediaryDirAndDestDirSame() {
        expectSpecificBuildException("testIntermediaryDirAndDestDirSame"
                , "IntermediaryDirAndDestDirSame"
                , "intermediaryBuildDir attribute cannot be specified\n"
                    +"to be the same as destdir or inside desdir of the javac task.\n" 
                    +"This is an intermediary build directory only used by the\n"
                    +"compilewithwalls task, not the class file output directory.\n"
                    +"The class file output directory is specified in javac's destdir attribute");
    }
    
    public void testIntermediaryDirInsideDestDir() {
        expectSpecificBuildException("testIntermediaryDirInsideDestDir"
                , "IntermediaryDirInsideDestDir"
                , "intermediaryBuildDir attribute cannot be specified\n"
        +"to be the same as destdir or inside desdir of the javac task.\n" 
        +"This is an intermediary build directory only used by the\n"
        +"compilewithwalls task, not the class file output directory.\n"
        +"The class file output directory is specified in javac's destdir attribute");
    }    
    
    public void testPackageDoesntEndWithStar() {
        expectSpecificBuildException("testPackageDoesntEndWithStar"
                , "PackageDoesntEndWithStar"
                , "The package='biz.xsoftware' must end with "
                    + ".* or .** such as biz.xsoftware.* or "
                    + "biz.xsoftware.**"  );
    }

    public void testPackageDoesntHaveSlash() {
        expectSpecificBuildException("testPackageDoesntHaveSlash"
                , "PackageDoesntHaveSlash"
                ,"A package name cannot contain '\\' or '/' like package="
                    + "biz/xsoftware.*\nIt must look like biz.xsoftware.* for example");
    }

    public void testDependsOnNonExistPackage() {
        expectSpecificBuildException("testDependsOnNonExistPackage"
                , "DependsOnNonExistPackage"
                , "package name=modA did not have modB"
                    + " listed before it and cannot compile without it");
    }

    public void testDependsOnPackageAfter() {
        expectSpecificBuildException("testDependsOnPackageAfter"
                , "DependsOnPackageAfter"
                , "package name=modA did not have modB"
                    + " listed before it and cannot compile without it");
    }

    public void testPackageABreakingWhenAIsCompiledFirst() {
        expectSpecificBuildException("testPackageABreakingWhenAIsCompiledFirst"
                , "PackageABreakingWhenAIsCompiledFirst"
                , "Compile failed; see the compiler error output for details.");
    }


    /**
     * This test case tests when modB depends on modA but it was
     * not specified in the walls so modA is not in modB's path. 
     * The build should then fail until they change the build.xml
     * so modB depends on modA in the walls element.
     */
    public void testPackageBBreakingWhenAIsCompiledFirst() {

        expectSpecificBuildException("testPackageBBreakingWhenAIsCompiledFirst"
                , "PackageBBreakingWhenAIsCompiledFirst"
                , "Compile failed; see the compiler error output for details.");

        //modA should have been compiled successfully, it is only modB that
        //fails.  It is very important we make sure A got compiled otherwise
        //we are not testing the correct behavior and the test would be wrong.        
        ensureClassFileExists("testB"+c+"mod"+c+"modA"+c+"ModuleA.class", true);     
        ensureClassFileExists("testB"+c+"mod"+c+"modB"+c+"ModuleB.class", false);            
    }

    public void testCompileOfAllUsingDepends() {
        ensureClassFileExists("testC"+c+"mod"+c+"Module.class", false);
        //make sure we are testing the correct thing and Module.java exists!
        ensureJavaFileExists("testC"+c+"mod"+c+"Module.java", true); 
              
        executeTarget("testCompileOfAllUsingDepends");

        //must test class files were actually created afterwards.
        //The build might pass with no class files if the task is
        //messed up.
        ensureClassFileExists("testC"+c+"mod"+c+"Module.class", true);
      
    }
//---------------------------------------------------------
//
//  The following tests are all just repeats of some of the above tests
//  except the below tests use External walls file and the above tests
//  don't.
//
//---------------------------------------------------------    

    public void testDependsOnPackageAfterExternalWalls() {
        expectSpecificBuildException("testDependsOnPackageAfterExternalWalls"
            , "DependsOnPackageAfterExternalWalls"
            , "package name=modA did not have modB"
              + " listed before it and cannot compile without it");
    }
  
    /**
     * This test case tests when modB depends on modA but it was
     * not specified in the walls so modA is not in modB's path. 
     * The build should then fail until they change the build.xml
     * so modB depends on modA in the walls element.
     */
    public void testPackageBBreakingWhenAIsCompiledFirstExternalWalls() {
        ensureClassFileExists("testB"+c+"mod"+c+"modA"+c+"ModuleA.class", false);
        ensureJavaFileExists("testB"+c+"mod"+c+"modB"+c+"ModuleB.java", true);
        
        expectSpecificBuildException("testPackageBBreakingWhenAIsCompiledFirst"
                , "PackageBBreakingWhenAIsCompiledFirst"
                , "Compile failed; see the compiler error output for details.");

        //modA should have been compiled successfully, it is only modB that
        //fails.  It is very important we make sure A got compiled otherwise
        //we are not testing the correct behavior and the test would be wrong.        
        ensureClassFileExists("testB"+c+"mod"+c+"modA"+c+"ModuleA.class", true);     
        ensureClassFileExists("testB"+c+"mod"+c+"modB"+c+"ModuleB.class", false);            
    }
        
    public void testCompileOfAllUsingDependsExternalWalls() {    
        ensureClassFileExists("testC"+c+"mod"+c+"Module.class", false);
        ensureJavaFileExists("testC"+c+"mod"+c+"Module.java", true);                       
        executeTarget("testCompileOfAllUsingDependsExternalWalls");
        //must test class files were actually created afterwards.
        //The build might pass with no class files if the task is
        //messed up.
        ensureClassFileExists("testC"+c+"mod"+c+"Module.class", true);
    }

    private void ensureJavaFileExists(String file, boolean shouldExist) {
        
        //must test that it is testing the correct directory.
        //It wasn't before.
        String javaFile = baseDir+file;
        File f1 = new File(javaFile);
        if(shouldExist)
            assertTrue("The java file="+f1.getAbsolutePath()+" didn't exist, we can't run this test.  It will pass with false results",
                    f1.exists());        
        else
            assertTrue("The java file="+f1.getAbsolutePath()+" exists and shouldn't, we can't run this test.  It will pass with false results",
                    !f1.exists());
    }
    
    private void ensureClassFileExists(String file, boolean shouldExist) {
        
        String classFile = baseDir
                            +"compilewithwalls"+File.separator
                            +"classes"+File.separator
                            +file;
                               
        File f1 = new File(classFile);
        if(shouldExist)
            assertTrue("The class file="+f1.getAbsolutePath()+" didn't get created, No build exception\nwas thrown, but the build failed because a class\nfile should have been created",
                    f1.exists());                               
        else
            assertTrue("The class file="+f1.getAbsolutePath()+" exists and shouldn't\nTest may be inaccurate if this file already exists...correct the test",
                        !f1.exists()); 
    }
 
    public static void main(String[] args) {
        TestSuite suite = new TestSuite(CompileWithWallsTest.class);
        TestRunner.run(suite);
    }
}
