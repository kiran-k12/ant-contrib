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
package net.sf.antcontrib.antserver;

import net.sf.antcontrib.BuildFileTestBase;


/****************************************************************************
 * Place class description here.
 *
 * @author		inger
 * @author		<additional author>
 *
 * @since
 *
 ****************************************************************************/


public class AntServerTest
        extends BuildFileTestBase
{
    public AntServerTest(String name)
    {
        super(name);
    }


    public void setUp()
    {
        configureProject("test/resources/antserver/antservertest.xml");
    }

    public void tearDown()
    {
        executeTarget("cleanup");
    }

    public void test1()
    {
        String expected[] = new String[]
        {
            "Test1 Successfully Called",
            "[test1_remote]"
        };

        expectLogContaining("test1", expected);
    }

    public void test2()
    {
        String expected[] = new String[]
        {
            "Test2 Successfully Called",
            "[test2_remote]"
        };

        expectLogContaining("test2", expected);
    }

    public void test3()
    {
        String expected[] = new String[]
        {
            "Test3 Successfully Called",
            "[test3_remote]"
        };

        expectLogContaining("test3", expected);
    }

    public void test4()
    {
        String expected[] = new String[]
        {
            "Test4 Successfully Called",
            "[test4_remote]"
        };

        expectLogContaining("test4", expected);
    }

    public void test5()
    {
        this.executeTarget("test5");
    }

    /**
     * Assert that the given message has been logged with a priority
     * &gt;= INFO when running the given target.
     */
    protected void expectLogContaining(String target,
                                       String logs[])
    {
        executeTarget(target);
        String realLog = getLog();

        int cnt = 0;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < logs.length; i++)
        {
            if (realLog.indexOf(logs[i]) >= 0)
                cnt++;
            if (i != 0)
                sb.append(" and ");
            sb.append("\"").append(logs[i]).append("\"");
        }


        assertTrue("expecting log to contain " + sb.toString()
                + " log was \"" + realLog + "\"",
                cnt == logs.length);
    }

}
