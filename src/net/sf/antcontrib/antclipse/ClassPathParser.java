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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Classic tool firing a SAX parser. Must feed the source file and a handler.
 * Nothing really special about it, only probably some special file handling in nasty cases
 * (Windows files containing strange chars, internationalized filenames,
 * but you shouldn't be doing this, anyway :)).
 * @author Adrian Spinei aspinei@myrealbox.com
 * @version $Revision: 1.1 $
 * @since Ant 1.5
 */
public class ClassPathParser
{
	void parse(File file, HandlerBase handler) throws BuildException
	{
		String fName = file.getName();
		FileInputStream fileInputStream = null;
		InputSource inputSource = null;
		try
		{
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			//go to UFS if we're on win
			String uri = "file:" + fName.replace('\\', '/');
			fileInputStream = new FileInputStream(file);
			inputSource = new InputSource(fileInputStream);
			inputSource.setSystemId(uri);
			saxParser.parse(inputSource, handler);
		}
		catch (ParserConfigurationException pceException)
		{
			throw new BuildException("Parser configuration failed", pceException);
		}
		catch (SAXParseException exc)
		{
			Location location = new Location(fName.toString(), exc.getLineNumber(), exc.getColumnNumber());
			Throwable throwable = exc.getException();
			if ((Object) throwable instanceof BuildException)
			{
				BuildException be = (BuildException) (Object) throwable;
				if (be.getLocation() == Location.UNKNOWN_LOCATION)
					be.setLocation(location);
				throw be;
			}
			throw new BuildException(exc.getMessage(), throwable, location);
		}
		catch (SAXException exc)
		{
			Throwable throwable = exc.getException();
			if ((Object) throwable instanceof BuildException)
				throw (BuildException) (Object) throwable;
			throw new BuildException(exc.getMessage(), throwable);
		}
		catch (FileNotFoundException exc)
		{
			throw new BuildException(exc);
		}
		catch (IOException exc)
		{
			throw new BuildException("Error reading file", exc);
		}
		finally
		{
			if (fileInputStream != null)
			{
				try
				{
					fileInputStream.close();
				}
				catch (IOException ioexception)
				{
					//do nothing, should not appear
				}
			}
		}
	}
}
