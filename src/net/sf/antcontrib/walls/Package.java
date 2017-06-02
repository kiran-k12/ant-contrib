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
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;


/*
 * Created on Aug 24, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
/**
 * FILL IN JAVADOC HERE
 *
 * @author Dean Hiller(dean@xsoftware.biz)
 */
public class Package {
    
    private String name;
    private String pack;

    //signifies the package did not end with .* or .**
    private boolean badPackage = false;
    private String failureReason = null;
    
    //holds the name attribute of the package element of each
    //package this package depends on.
    private String[] depends;

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
        
    public void setPackage(String pack) {
        this.pack = pack;
    }
    public String getPackage() {
        return pack;
    }
        
    public void setDepends(String d) {
        if(d == null) {
            throw new RuntimeException("depends cannot be set to null");
        }
                
        //parse this first.
        StringTokenizer tok = new StringTokenizer(d, ", \t");
        depends = new String[tok.countTokens()];
        int i = 0;
        while(tok.hasMoreTokens()) {
            depends[i] = tok.nextToken();	
            i++;
        }
    }
    
    public String[] getDepends() {
        return depends;
    }
        
    /**
     * FILL IN JAVADOC HERE
     * 
     */
    public FileSet getJavaCopyFileSet(Project p, Location l) throws BuildException {
        
        if(failureReason != null)
            throw new BuildException(failureReason, l);
        else if(pack.indexOf("/") != -1 || pack.indexOf("\\") != -1)
            throw new BuildException("A package name cannot contain '\\' or '/' like package="+pack
                                +"\nIt must look like biz.xsoftware.* for example", l);
        FileSet set = new FileSet();

        String match = getMatch(p, pack, ".java");
        //log("match="+match+" pack="+pack);               
        //first exclude the compilation module, then exclude all it's
        //dependencies too.
        set.setIncludes(match);

        return set;
    }

    /**
     * FILL IN JAVADOC HERE
     * 
     */
    public FileSet getClassCopyFileSet(Project p, Location l) throws BuildException {        
        FileSet set = new FileSet();
        set.setIncludes("**/*.class");
        return set;
    }
    
    public File getBuildSpace(File baseDir) {
        return new File(baseDir, name);
    }

    /**
     * @param tempBuildDir
     * @return
     */
    public Path getSrcPath(File baseDir, Project p) {
        Path path = new Path(p);
        
        path.setLocation(getBuildSpace(baseDir));
        return path;
    }
    
    /**
     * @return
     */
    public Path getClasspath(File baseDir, Project p) {
        Path path = new Path(p);

        if(depends != null) {
            for(int i = 0; i < depends.length; i++) {
                String buildSpace = (String)depends[i];
                
                File dependsDir = new File(baseDir, buildSpace);
                path.setLocation(dependsDir);
            }
        }
        return path;
    }
        
    private String getMatch(Project p, String pack, String postFix) {
        pack = p.replaceProperties(pack);
        
        
        pack = pack.replace('.', File.separatorChar);

        String match;
        String classMatch;
        if(pack.endsWith("**")) {
            match  = pack + File.separatorChar+"*"+postFix;
        }
        else if(pack.endsWith("*")) {
            match  = pack + postFix;
        }
        else
            throw new RuntimeException("Please report this bug");
            
        return match;
    }

    /**
     * FILL IN JAVADOC HERE
     * @param string
     */
    public void setFaultReason(String r) {
        failureReason = r;
    }
}