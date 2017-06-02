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

import net.sf.antcontrib.BuildFileTestBase;

/**
 * Since AntCallBack is basically a copy and paste of antcall, the only testing
 * done here is on the extra features provided by antcallback. It is assumed
 * that changes to antcall will be propagated to antcallback and that antcall
 * has it's own unit tests (which turns out to have been a bad assumption,
 * I can't find any unit tests for antcall).
 *
 * @author   danson
 */
public class AntCallBackTest extends BuildFileTestBase {

   /**
    * Constructor for the AntCallBackTest object
    *
    * @param name  Description of the Parameter
    */
   public AntCallBackTest( String name ) {
      super( name );
   }


   /** The JUnit setup method */
   public void setUp() {
      configureProject( "test/resources/logic/antcallbacktest.xml" );
   }


   /** A unit test for JUnit */
   public void test1() {
      expectPropertySet( "test1", "prop1", "prop1" );
   }


   /** A unit test for JUnit */
   public void test2() {
      expectPropertySet( "test2", "prop1", "prop1" );
      expectPropertySet( "test2", "prop2", "prop2" );
      expectPropertySet( "test2", "prop3", "prop3" );
   }


   /** A unit test for JUnit */
   public void test3() {
      expectPropertySet( "test3", "prop1", "prop1" );
      expectPropertySet( "test3", "prop2", "prop2" );
      expectPropertySet( "test3", "prop3", "prop3" );
   }


   /** A unit test for JUnit */
   public void test4() {
      expectPropertyUnset( "test4", "prop1" );
      expectPropertySet( "test4", "prop2", "prop2" );
      expectPropertySet( "test4", "prop3", "prop3" );
   }                                     


   /** A unit test for JUnit */
   public void test5() {
      expectPropertySet( "test5", "prop1", "blah" );
      expectPropertySet( "test5", "prop2", "prop2" );
      expectPropertySet( "test5", "prop3", "prop3" );
   }                
}

