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
package net.sf.antcontrib;

import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.BuildException;
import junit.framework.TestCase;
import org.apache.tools.ant.Project;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;


/**
 * More methods for BuildFileTest.
 *
 * @author   Dale Anson
 */
public abstract class BuildFileTestBase extends BuildFileTest {

   /**
    * Constructor for the BuildFileTestBase object
    *
    * @param name  string to pass up to TestCase constructor
    */
   public BuildFileTestBase( String name ) {
      super( name );
   }

   /**
    * run a target, expect a build exception
    * 
    * @param target  target to run
    */
   protected void expectBuildException( String target ) {
      expectSpecificBuildException( target, "no specific reason", null );
   }

   /**
    * Assert that the given message has NOT been logged with a priority &gt;= INFO
    * when running the given target.
    * 
    * @param target  Description of the Parameter
    * @param log     Description of the Parameter
    */
   protected void expectLogNotContaining( String target, String log ) {
      executeTarget( target );
      String realLog = getLog();
      assertTrue( "expecting log to NOT contain \"" + log + "\" log was \""
             + realLog + "\"",
            realLog.indexOf( log ) < 0 );
   }

   /**
    * set up to run the named project
    * <p>
    * Overrides BuildFileTest.configureProject to first
    * attempt to make a File out of the filename parameter, if the resulting
    * file does not exists, then attempt to locate the file in the classpath.
    * This way, test xml files can be placed alongside of their corresponding
    * class file and can be easily found.
    *
    * @param filename            name of project file to run
    * @exception BuildException  Description of the Exception
    */
   protected void configureProject( String filename ) throws BuildException {
      // find the build file
      File f = new File( filename );
      if ( !f.exists() ) {
         URL url = getClass().getClassLoader().getResource( filename );
         if ( url == null )
            throw new BuildException( "Can't find " + filename );
         f = new File( url.getPath() );
         if ( !f.exists() )
            throw new BuildException( "Can't find " + filename );
      }
      super.configureProject(f.getAbsolutePath());
   }

   /**
    * run a target, expect an exception string containing the substring we look
    * for (case sensitive match)
    * 
    * @param target    target to run
    * @param cause     information string to reader of report
    * @param contains  substring of the build exception to look for
    */
   protected void expectBuildExceptionStackTraceContaining( String target, String cause, String contains ) {
      try {
         executeTarget( target );
      }
      catch ( org.apache.tools.ant.BuildException ex ) {
         //buildException = ex;  // buildException has private access in super
         StringWriter stacktrace = new StringWriter();
         PrintWriter writer = new PrintWriter( stacktrace, true );
         ex.printStackTrace( writer );
         String trace = stacktrace.toString();
         if ( ( null != contains ) && ( trace.indexOf( contains ) == -1 ) ) {
            fail( "Should throw BuildException because '" + cause + "' with message containing '" + contains + "' (actual message '" + trace + "' instead)" );
         }
         return;
      }
      fail( "Should throw BuildException because: " + cause );
   }
}

