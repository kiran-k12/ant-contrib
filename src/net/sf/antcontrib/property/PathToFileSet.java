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
package net.sf.antcontrib.property;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Path;

public class PathToFileSet
    extends Task
{
    private File dir;
    private String name;
    private String pathRefId;
    private boolean ignoreNonRelative = false;

    private static FileUtils fileUtils = FileUtils.newFileUtils();

    public void setDir(File dir) {
        this.dir = dir;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPathRefId(String pathRefId) {
        this.pathRefId = pathRefId;
    }

    public void setIgnoreNonRelative(boolean ignoreNonRelative) {
        this.ignoreNonRelative = ignoreNonRelative;
    }

    public void execute() {
        if (dir == null)
            throw new BuildException("missing dir");
        if (name == null)
            throw new BuildException("missing name");
        if (pathRefId == null)
            throw new BuildException("missing pathrefid");

        if (! dir.isDirectory())
            throw new BuildException(
                dir.toString() + " is not a directory");

        Object path =  getProject().getReference(pathRefId);
        if (path == null)
            throw new BuildException("Unknown reference " + pathRefId);
        if (! (path instanceof Path))
            throw new BuildException(pathRefId + " is not a path");
        

        String[] sources = ((Path) path).list();

        FileSet fileSet = new FileSet();
        fileSet.setProject(getProject());
        fileSet.setDir(dir);
        String dirNormal =
            fileUtils.normalize(dir.getAbsolutePath()).getAbsolutePath()
            + File.separator;
        

        boolean atLeastOne = false;
        for (int i = 0; i < sources.length; ++i) {
            File sourceFile = new File(sources[i]);
            if (! sourceFile.exists())
                continue;
            String relativeName = getRelativeName(dirNormal, sourceFile);
            if (relativeName == null && !ignoreNonRelative) {
                throw new BuildException(
                    sources[i] + " is not relative to " + dir.getAbsolutePath());
            }
            if (relativeName == null)
                continue;
            fileSet.createInclude().setName(relativeName);
            atLeastOne = true;
        }

        if (! atLeastOne) {
            // need to make an empty fileset
            fileSet.createInclude().setName("a:b:c:d//THis si &&& not a file  !!! ");
        }
        getProject().addReference(name, fileSet);
    }

    private String getRelativeName(String dirNormal, File file) {
        String fileNormal =
            fileUtils.normalize(file.getAbsolutePath()).getAbsolutePath();
        if (! fileNormal.startsWith(dirNormal))
            return null;
        return fileNormal.substring(dirNormal.length());
    }
}

