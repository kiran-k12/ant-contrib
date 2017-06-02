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


import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.util.FileUtils;
import java.lang.StringBuffer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *  A generic front-end for passing "shell lines" to any application which can
 * accept a filename containing script input (bash, perl, csh, tcsh, etc.).
 * see antcontrib doc for useage
 *
 * @author stephan beal
 *@author peter reilly
 */

public class ShellScriptTask extends ExecTask {

    private StringBuffer script = new StringBuffer();
    private String shell = null;
    private File     tmpFile;
    private String tmpSuffix = null;

    /**
     *  Adds s to the lines of script code.
     */
    public void addText(String s) {
        script.append(getProject().replaceProperties(s));
    }

    /**
     *  Sets script code to s.
     */
    public void setInputString(String s) {
        script.append(s);
    }

    /**
     *  Sets the shell used to run the script.
     * @param shell the shell to use (bash is default)
     */
    public void setShell(String shell) {
        this.shell = shell;
    }

    /**
     *  Sets the shell used to run the script.
     * @param shell the shell to use (bash is default)
     */
    public void setExecutable(String shell) {
        this.shell = shell;
    }

    /**
     * Disallow the command attribute of parent class ExecTask.
     * ant.attribute ignore="true"
     * @param notUsed not used
     * @throws BuildException if called
     */
    public void setCommand(Commandline notUsed) {
        throw new BuildException("Attribute command is not supported");
    }
     
    
    /**
     * Sets the suffix for the tmp file used to
     * contain the script.
     * This is useful for cmd.exe as one can
     * use cmd /c call x.bat
     * @param tmpSuffix the suffix to use
     */

    public void setTmpSuffix(String tmpSuffix) {
        this.tmpSuffix = tmpSuffix;
    }
    
    /**
     * execute the task
     */
    public void execute() throws BuildException {
        // Remove per peter's comments.  Makes sense.
        /*
         if (shell == null)
         {
             // Get the default shell
             shell = Platform.getDefaultShell();

             // Get the default shell arguments
             String args[] = Platform.getDefaultShellArguments();
             for (int i=args.length-1;i>=0;i--)
                 this.cmdl.createArgument(true).setValue(args[i]);

             // Get the default script suffix
             if (tmpSuffix == null)
                 tmpSuffix = Platform.getDefaultScriptSuffix();
                 
         }
         */
        if (shell == null)
            throw new BuildException("You must specify a shell to run.");

        try {
            /* // The following may be used when ant 1.6 is used.
              if (tmpSuffix == null)
              super.setInputString(script.toString());
              else
            */
            {
                writeScript();
                super.createArg().setValue(tmpFile.getAbsolutePath());
            }
            super.setExecutable(shell);
            super.execute();
        }
        finally {
            if (tmpFile != null) {
                if (! tmpFile.delete()) {
                    log("Non-fatal error: could not delete temporary file " +
                        tmpFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     *  Writes the script lines to a temp file.
     */
    protected void writeScript() throws BuildException {
        FileOutputStream os = null;
        try {
            FileUtils fileUtils = FileUtils.newFileUtils();
            // NB: use File.io.createTempFile whenever jdk 1.2 is allowed
            tmpFile = fileUtils.createTempFile("script", tmpSuffix, null);
            os = new java.io.FileOutputStream(tmpFile);
            String string = script.toString();
            os.write(string.getBytes(), 0, string.length());
            os.close();
        }
        catch (Exception e) {
            throw new BuildException(e);
        }
        finally {
            try {os.close();} catch (Throwable t) {}
        }
    }

}

