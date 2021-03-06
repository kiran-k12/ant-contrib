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
//package ise.antelope.tasks;
package net.sf.antcontrib.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLConnection;

// no longer used directly, added reflection code to use the encoder/decoder
// for compatibility with java 1.2.
//import java.net.URLDecoder;
//import java.net.URLEncoder;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.lang.reflect.Method;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

/**
 * This task does an http post. Name/value pairs for the post can be set in
 * either or both of two ways, by nested Prop elements and/or by a file
 * containing properties. Nested Prop elements are automatically configured by
 * Ant. Properties from a file are configured by code borrowed from Property so
 * all Ant property constructs (like ${somename}) are resolved prior to the
 * post. This means that a file can be set up in advance of running the build
 * and the appropriate property values will be filled in at run time.
 * <p>Developed for use with Antelope, migrated to ant-contrib Oct 2003.
 *
 * @author    Dale Anson, danson@germane-software.com
 * @version   $Revision: 1.3 $
 */
public class PostTask extends Task {

   /** Storage for name/value pairs to send. */
   private Hashtable props = new Hashtable();
   /** URL to send the name/value pairs to. */
   private URL to = null;
   /** File to read name/value pairs from. */
   private File propsFile = null;
   /** storage for Ant properties */
   private String textProps = null;
   /** encoding to use for the name/value pairs */
   private String encoding = "UTF-8";
   /** where to store the server response */
   private File log = null;
   /** append to the log? */
   private boolean append = true;
   /** verbose? */
   private boolean verbose = true;
   /** want to keep the server response? */
   private boolean wantResponse = true;
   /** how long to wait for a response from the server */
   private long maxwait = 180000;   // units for maxwait is milliseconds
   /** fail on error? */
   private boolean failOnError = false;
   /** connection to the server */
   private URLConnection connection = null;
   /** for thread handling */
   private Thread currentRunner = null;


   /**
    * Set the url to post to. Required.
    *
    * @param name  the url to post to.
    */
   public void setTo( URL name ) {
      to = name;
   }


   /**
    * Set the name of a file to read a set of properties from.
    *
    * @param f  the file
    */
   public void setFile( File f ) {
      propsFile = f;
   }


   /**
    * Set the name of a file to save the response to. Optional. Ignored if "want
    * response" is false.
    *
    * @param f  the file
    */
   public void setLogfile( File f ) {
      log = f;
   }


   /**
    * Should the log file be appended to or overwritten? Default is true, append
    * to the file.
    *
    * @param b  append or not
    */
   public void setAppend( boolean b ) {
      append = b;
   }


   /**
    * If true, progress messages and returned data from the post will be
    * displayed. Default is true.
    *
    * @param b  true = verbose
    */
   public void setVerbose( boolean b ) {
      verbose = b;
   }


   /**
    * Default is true, get the response from the post. Can be set to false for
    * "fire and forget" messages.
    *
    * @param b  print/log server response
    */
   public void setWantresponse( boolean b ) {
      wantResponse = b;
   }

   /**
    * Sets the encoding of the outgoing properties, default is UTF-8.
    *
    * @param encoding  The new encoding value
    */
   public void setEncoding( String encoding ) {
      this.encoding = encoding;
   }


   /**
    * How long to wait on the remote server. As a post is generally a two part
    * process (sending and receiving), maxwait is applied separately to each
    * part, that is, if 180 is passed as the wait parameter, this task will
    * spend at most 3 minutes to connect to the remote server and at most
    * another 3 minutes waiting on a response after the post has been sent. This
    * means that the wait period could total as much as 6 minutes (or 360
    * seconds). <p>
    *
    * The default wait period is 3 minutes (180 seconds).
    *
    * @param wait  time to wait in seconds, set to 0 to wait forever.
    */
   public void setMaxwait( int wait ) {
      maxwait = wait * 1000;
   }


   /**
    * Should the build fail if the post fails?
    *
    * @param fail  true = fail the build, default is false
    */
   public void setFailonerror( boolean fail ) {
      failOnError = fail;
   }


   /**
    * Adds a name/value pair to post. Optional.
    *
    * @param p                   A property pair to send as part of the post.
    * @exception BuildException  When name and/or value are missing.
    */
   public void addConfiguredProp( Prop p ) throws BuildException {
      String name = p.getName();
      if ( name == null ) {
         throw new BuildException( "name is null", getLocation() );
      }
      String value = p.getValue();
      if ( value == null ) {
         value = getProject().getProperty( name );
      }
      if ( value == null ) {
         throw new BuildException( "value is null", getLocation() );
      }
      props.put( name, value );
   }


   /**
    * Adds a feature to the Text attribute of the PostTask object
    *
    * @param text  The feature to be added to the Text attribute
    */
   public void addText( String text ) {
      textProps = text;
   }


   /**
    * Do the post.
    *
    * @exception BuildException  On any error.
    */
   public void execute() throws BuildException {
      if ( to == null ) {
         throw new BuildException( "'to' attribute is required", getLocation() );
      }
      final String content = getContent();
      try {
         log( "Opening connection for post to " + to.toString() + "..." );

         // do the POST
         Thread runner =
            new Thread() {
               public void run() {
                  DataOutputStream out = null;
                  try {
                     // set the url connection properties
                     connection = to.openConnection();
                     connection.setDoInput( true );
                     connection.setDoOutput( true );
                     connection.setUseCaches( false );
                     connection.setRequestProperty(
                           "Content-Type",
                           "application/x-www-form-urlencoded" );

                     // do the post
                     if ( verbose ) {
                        log( "Connected, sending data..." );
                     }
                     out = new DataOutputStream( connection.getOutputStream() );
                     if ( verbose ) {
                        log( content );
                     }
                     out.writeBytes( content );
                     out.flush();
                     if ( verbose ) {
                        log( "Data sent." );
                     }
                  }
                  catch ( Exception e ) {
                     if ( failOnError ) {
                        throw new BuildException( e, getLocation() );
                     }
                  }
                  finally {
                     try {
                        out.close();
                     }
                     catch ( Exception e ) {
                        // ignored
                     }
                  }
               }
            };
         runner.run();
         runner.join( maxwait );
         if ( runner.isAlive() ) {
            runner.interrupt();
            if ( failOnError ) {
               throw new BuildException( "maxwait exceeded, unable to send data", getLocation() );
            }
            return;
         }

         // read the response, if any, optionally writing it to a file
         if ( wantResponse ) {
            if ( verbose ) {
               log( "Waiting for response..." );
            }
            runner =
               new Thread() {
                  public void run() {
                     FileWriter fw = null;
                     BufferedReader in = null;
                     try {
                        in = new BufferedReader(
                              new InputStreamReader( connection.getInputStream() ) );
                        if ( log != null ) {
                           fw = new FileWriter( log.getAbsolutePath(), append );
                        }
                        String line;
                        while ( null != ( ( line = in.readLine() ) ) ) {
                           if ( currentRunner != this ) {
                              break;
                           }
                           line = decode( line );
                           if ( verbose ) {
                              log( line );
                           }
                           if ( fw != null ) {
                              fw.write( line );
                           }
                        }
                     }
                     catch ( Exception e ) {
                        if ( failOnError ) {
                           throw new BuildException( e, getLocation() );
                        }
                     }
                     finally {
                        try {
                           in.close();
                        }
                        catch ( Exception e ) {
                           // ignored
                        }
                        try {
                           if ( fw != null ) {
                              fw.flush();
                              fw.close();
                           }
                        }
                        catch ( Exception e ) {
                           // ignored
                        }
                     }
                  }
               };
            currentRunner = runner;
            runner.run();
            runner.join( maxwait );
            if ( runner.isAlive() ) {
               currentRunner = null;
               runner.interrupt();
               if ( failOnError ) {
                  throw new BuildException( "maxwait exceeded, unable to receive data", getLocation() );
               }
            }
         }
         log( "Post complete." );
      }
      catch ( Exception e ) {
         if ( failOnError ) {
            throw new BuildException( e );
         }
      }
   }


   /**
    * Builds and formats the message to send to the server.
    *
    * @return   the message to send to the server, UTF-8 encoded.
    */
   private String getContent() {
      if ( propsFile != null ) {
         loadFile( propsFile );
      }

      if ( textProps != null ) {
         loadTextProps( textProps );
      }

      StringBuffer content = new StringBuffer();
      try {
         Enumeration enum = props.keys();
         while ( enum.hasMoreElements() ) {
            String name = (String)enum.nextElement();
            String value = (String)props.get( name );
            content.append( encode( name ) );
            content.append( "=" );
            content.append( encode( value ) );
            if ( enum.hasMoreElements() ) {
               content.append( "&" );
            }
         }
      }
      catch ( Exception e ) {
         // ignored
      }
      return content.toString();
   }


   /**
    * Borrowed from Property -- load variables from a file
    *
    * @param file                file to load
    * @exception BuildException  Description of the Exception
    */
   private void loadFile( File file ) throws BuildException {
      Properties fileprops = new Properties();
      try {
         if ( file.exists() ) {
            FileInputStream fis = new FileInputStream( file );
            try {
               fileprops.load( fis );
            }
            finally {
               if ( fis != null ) {
                  fis.close();
               }
            }
            addProperties( fileprops );
         }
         else {
            log( "Unable to find property file: " + file.getAbsolutePath(),
                  Project.MSG_VERBOSE );
         }
      }
      catch ( IOException ex ) {
         if ( failOnError ) {
            throw new BuildException( ex, location );
         }
      }
   }


   /**
    * Description of the Method
    *
    * @param tp
    */
   private void loadTextProps( String tp ) {
      Properties p = new Properties();
      Project project = getProject();
      StringTokenizer st = new StringTokenizer( tp, "$" );
      while ( st.hasMoreTokens() ) {
         String token = st.nextToken();
         int start = token.indexOf( "{" );
         int end = token.indexOf( "}" );
         if ( start > -1 && end > -1 && end > start ) {
            String name = token.substring( start + 1, end - start );
            String value = project.getProperty( name );
            if ( value != null )
               p.setProperty( name, value );
         }
      }
      addProperties( p );
   }


   /**
    * Borrowed from Property -- iterate through a set of properties, resolve
    * them, then assign them
    *
    * @param fileprops  The feature to be added to the Properties attribute
    */
   private void addProperties( Properties fileprops ) {
      resolveAllProperties( fileprops );
      Enumeration e = fileprops.keys();
      while ( e.hasMoreElements() ) {
         String name = (String)e.nextElement();
         String value = fileprops.getProperty( name );
         props.put( name, value );
      }
   }


   /**
    * Borrowed from Property -- resolve properties inside a properties hashtable
    *
    * @param fileprops           Description of the Parameter
    * @exception BuildException  Description of the Exception
    */
   private void resolveAllProperties( Properties fileprops ) throws BuildException {
      for ( Enumeration e = fileprops.keys(); e.hasMoreElements();  ) {
         String name = (String)e.nextElement();
         String value = fileprops.getProperty( name );

         boolean resolved = false;
         while ( !resolved ) {
            Vector fragments = new Vector();
            Vector propertyRefs = new Vector();
            ProjectHelper.parsePropertyString( value, fragments,
                  propertyRefs );

            resolved = true;
            if ( propertyRefs.size() != 0 ) {
               StringBuffer sb = new StringBuffer();
               Enumeration i = fragments.elements();
               Enumeration j = propertyRefs.elements();
               while ( i.hasMoreElements() ) {
                  String fragment = (String)i.nextElement();
                  if ( fragment == null ) {
                     String propertyName = (String)j.nextElement();
                     if ( propertyName.equals( name ) ) {
                        throw new BuildException( "Property " + name
                               + " was circularly "
                               + "defined." );
                     }
                     fragment = getProject().getProperty( propertyName );
                     if ( fragment == null ) {
                        if ( fileprops.containsKey( propertyName ) ) {
                           fragment = fileprops.getProperty( propertyName );
                           resolved = false;
                        }
                        else {
                           fragment = "${" + propertyName + "}";
                        }
                     }
                  }
                  sb.append( fragment );
               }
               value = sb.toString();
               fileprops.put( name, value );
            }
         }
      }
   }
   
   public String encode( String toEncode ) throws BuildException {
      return invokeCoder( "encode", toEncode );
   }

   public String decode( String toDecode ) throws BuildException {
      return invokeCoder( "decode", toDecode );
   }

   public String invokeCoder( String methodName, String toEncode ) {
      try {
         Class c = null;
         if ( methodName.equals( "encode" ) )
            c = Class.forName( "java.net.URLEncoder" );
         else if (methodName.equals("decode"))
            c = Class.forName( "java.net.URLDecoder" );
         if ( c != null ) {
            Class[] params = new Class[] {toEncode.getClass(), encoding.getClass() };
            Method m = null;
            try {
               m = c.getMethod( methodName, params );
               return ( String ) m.invoke( c, new String[] {toEncode, encoding} ) ;
            }
            catch ( NoSuchMethodException nsme ) {
               params = new Class[] {toEncode.getClass()};
               try {
                  m = c.getMethod( methodName, params );
                  return ( String ) m.invoke( c, new String[] {toEncode} );
               }
               catch ( NoSuchMethodException e ) {
                  throw new BuildException( e );
               }
            }
         }
         else {
            throw new IllegalArgumentException("Method name must be one of 'encode' or 'decode'.");
         }
      }
      catch ( Exception e ) {
         throw new BuildException( e );
      }
   }
}

