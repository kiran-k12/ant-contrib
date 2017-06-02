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

package net.sf.antcontrib.antclipse;

import org.apache.tools.ant.BuildFileTest;

/**
 * Basic test for "antclipse" task. For the moment it just launches the test xml script.
 * @author Adrian Spinei aspinei@myrealbox.com
 * @version $Revision: 1.1 $
 */
public class AntclipseTest extends BuildFileTest
{

	/**
	 * Simple overriden constructor
	 * @param arg0
	 */
	public AntclipseTest(String arg0)
	{
		super(arg0);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	public void setUp()
	{
		configureProject("test/resources/antclipse/antclipsetest.xml");
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	public void tearDown()
	{
		//nothing to do
	}

	/**
	 * Launches the "everything" task. Should not throw errors.
	 */
	public void testExecuteDefaultBuild()
	{
		executeTarget("everything");
	}

}