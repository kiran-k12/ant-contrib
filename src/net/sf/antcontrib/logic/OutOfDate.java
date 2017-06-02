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

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Parallel;
import org.apache.tools.ant.taskdefs.Sequential;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;

/**
* Task to help in calling tasks if generated files are older
* than source files.
* Sets a given property or runs an internal task.
*
* Based on
*  org.apache.org.apache.tools.ant.taskdefs.UpToDate
*
* @author peter reilly
*/

public class OutOfDate extends Task implements Condition {


    // attributes and nested elements
    private Task doTask = null;
    private String property;
    private String value                = "true";
    private boolean force               = false;
    private int    verbosity            = Project.MSG_VERBOSE;
    private Vector mappers              = new Vector();
    private Path targetpaths            = null;
    private Path sourcepaths            = null;
    private String outputSources        = null;
    private String outputSourcesPath    = null;
    private String outputTargets        = null;
    private String outputTargetsPath    = null;
    private String allTargets           = null;
    private String allTargetsPath       = null;
    private String separator            = " ";
    private String mapperDir            = null;
    private DeleteTargets deleteTargets = null;

    // variables
    private Hashtable targetSet = new Hashtable();
    private Hashtable sourceSet = new Hashtable();
    private Hashtable allTargetSet = new Hashtable();
    /**
     * Defines the FileNameMapper to use (nested mapper element).
     * @return Mappper to be configured
     */
    public Mapper createMapper() {
        MyMapper mapper = new MyMapper(getProject());
        mappers.addElement(mapper);
        return mapper;
    }

    /**
     * The property to set if any of the target files are outofdate with
     * regard to any of the source files.
     *
     * @param property the name of the property to set if Target is outofdate.
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * The separator to use to separate the files
     * @param separator separator used in outout properties
     */

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    /**
     * The value to set the named property to the target files
     * are outofdate
     *
     * @param value the value to set the property
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * whether to allways be outofdate
     * @param force true means that outofdate is always set, default
     *              false
     */
    public void setForce(boolean force) {
        this.force = force;
    }

    /**
     * whether to have verbose output
     * @param verbose true means that outofdate outputs debug info
     */
    public void setVerbose(boolean verbose) {
        if (verbose) {
            this.verbosity = Project.MSG_INFO;
        } else {
            this.verbosity = Project.MSG_VERBOSE;
        }
    }

    /**
     * Add to the target files
     *
     * @return a path to be configured
     */
    public Path createTargetfiles() {
        if (targetpaths == null) {
            targetpaths = new Path(getProject());
        }
        return targetpaths;
    }

    /**
     * Add to the source files
     *
     * @return a path to be configured
     */
    public Path createSourcefiles() {
        if (sourcepaths == null) {
            sourcepaths = new Path(getProject());
        }
        return sourcepaths;
    }

    /**
     * A property to contain the output source files
     *
     * @param outputSources the name of the property
     */
    public void setOutputSources(String outputSources) {
        this.outputSources = outputSources;
    }

    /**
     * A property to contain the output target files
     *
     * @param outputTargets the name of the property
     */
    public void setOutputTargets(String outputTargets) {
        this.outputTargets = outputTargets;
    }

    /**
     * A reference to contain the path of target files that
     * are outofdate
     *
     * @param outputTargetsPath the name of the reference
     */
    public void setOutputTargetsPath(String outputTargetsPath) {
        this.outputTargetsPath = outputTargetsPath;
    }

    /**
     * A refernce to contain the path of all the targets
     *
     * @param allTargetsPath the name of the reference
     */
    public void setAllTargetsPath(String allTargetsPath) {
        this.allTargetsPath = allTargetsPath;
    }

    /**
     * A property to contain all the target filenames
     *
     * @param allTargets the name of the property
     */
    public void setAllTargets(String allTargets) {
        this.allTargets = allTargets;
    }

    /**
     * A reference to the path containing all the sources files.
     *
     * @param outputSourcesPath the name of the reference
     */
    public void setOutputSourcesPath(String outputSourcesPath) {
        this.outputSourcesPath = outputSourcesPath;
    }

    /**
     * optional nested delete element
     * @return an element to be configured
     */
    public DeleteTargets createDeleteTargets() {
        deleteTargets = new DeleteTargets();
        return deleteTargets;
    }

    /**
     * Embedded do parallel
     * @param doTask the parallel to embed
     */
    public void addParallel(Parallel doTask) {
        if (this.doTask != null) {
            throw new BuildException(
                "You must not nest more that one <parallel> or <sequential>"
                + " into <outofdate>");
        }
        this.doTask = doTask;
    }

    /**
     * Embedded do sequential.
     * @param doTask the sequential to embed
     */
    public void addSequential(Sequential doTask) {
        if (this.doTask != null) {
            throw new BuildException(
                "You must not nest more that one <parallel> or <sequential>"
                + " into <outofdate>");
        }
        this.doTask = doTask;
    }

    /**
     * Evaluate (all) target and source file(s) to
     * see if the target(s) is/are outoutdate.
     * @return true if any of the targets are outofdate
     */
    public boolean eval() {
        boolean ret = false;
        FileUtils fileUtils = FileUtils.newFileUtils();
        if (sourcepaths == null) {
            throw new BuildException(
                "You must specify a <sourcefiles> element.");
        }

        if (targetpaths == null && mappers.size() == 0) {
            throw new BuildException(
                "You must specify a <targetfiles> or <mapper> element.");
        }

        // Source Paths
        String[] spaths = sourcepaths.list();

        for (int i = 0; i < spaths.length; i++) {
            File sourceFile = new File(spaths[i]);
            if (!sourceFile.exists()) {
                throw new BuildException(sourceFile.getAbsolutePath()
                                         + " not found.");
            }
        }

        // Target Paths

        if (targetpaths != null) {
            String[] paths = targetpaths.list();
            for (int i = 0; i < paths.length; ++i) {
                if (targetNeedsGen(paths[i], spaths)) {
                    ret = true;
                }
            }
        }

        // Mapper Paths
        for (Enumeration e = mappers.elements(); e.hasMoreElements();) {
            MyMapper mapper = (MyMapper) e.nextElement();

            File   relativeDir = mapper.getDir();
            File   baseDir = new File(getProject().getProperty("basedir"));
            if (relativeDir == null) {
                relativeDir = baseDir;
            }
            String[] rpaths = new String[spaths.length];
            for (int i = 0; i < spaths.length; ++i) {
                rpaths[i] = fileUtils.removeLeadingPath(relativeDir, new File(spaths[i]));
            }

            FileNameMapper fileNameMapper = mapper.getImplementation();
            Vector mappedFiles = new Vector();
            for (int i = 0; i < spaths.length; ++i) {
                String[] mapped = fileNameMapper.mapFileName(rpaths[i]);
                if (mapped != null) {
                    for (int j = 0; j < mapped.length; ++j) {
                        if (outOfDate(new File(spaths[i]),
                                      fileUtils.resolveFile(
                                          baseDir, mapped[j]))) {
                            ret = true;
                        }
                    }
                }
            }
        }

        if (allTargets != null) {
            this.getProject().setNewProperty(
                allTargets, setToString(allTargetSet));
        }

        if (allTargetsPath != null) {
            this.getProject().addReference(
                allTargetsPath, setToPath(allTargetSet));
        }

        if (outputSources != null) {
            this.getProject().setNewProperty(
                outputSources, setToString(sourceSet));
        }

        if (outputTargets != null) {
            this.getProject().setNewProperty(
                outputTargets, setToString(targetSet));
        }

        if (outputSourcesPath != null) {
            this.getProject().addReference(
                outputSourcesPath, setToPath(sourceSet));
        }

        if (outputTargetsPath != null) {
            this.getProject().addReference(
                outputTargetsPath, setToPath(targetSet));
        }

        if (force) {
            ret = true;
        }

        if (ret && deleteTargets != null) {
            deleteTargets.execute();
        }

        if (ret) {
            if (property != null) {
                this.getProject().setNewProperty(property, value);
            }
        }

        return ret;
    }

    private boolean targetNeedsGen(String target, String[] spaths) {
        boolean ret = false;
        File targetFile = new File(target);
        for (int i = 0; i < spaths.length; i++) {
            if (outOfDate(new File(spaths[i]), targetFile)) {
                ret = true;
            }
        }
        // Special case : there are no source files, make sure the
        //                targets exist
        if (spaths.length == 0) {
            if (outOfDate(null, targetFile)) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Sets property to true and/or executes embedded do
     * if any of the target file(s) do not have a more recent timestamp
     * than (each of) the source file(s).
     */
    public void execute() {
        if (!eval()) {
            return;
        }

        if (doTask != null) {
            doTask.perform();
        }
    }


    private boolean outOfDate(File sourceFile, File targetFile) {
        boolean ret  = false;
        allTargetSet.put(targetFile, targetFile);
        if (!targetFile.exists()) {
            ret = true;
        }
        if ((!ret)  && (sourceFile != null)) {
            ret = sourceFile.lastModified() > targetFile.lastModified();
        }
        if (ret) {
            if ((sourceFile != null && sourceSet.get(sourceFile) == null)
                || targetSet.get(targetFile) == null) {
                log("SourceFile " + sourceFile + " outofdate "
                    + "with regard to " + targetFile, verbosity);
            }
            if (sourceFile != null) {
                sourceSet.put(sourceFile, sourceFile);
            }
            targetSet.put(targetFile, targetFile);
        }
        return ret;
    }

    private String setToString(Hashtable set) {
        StringBuffer b = new StringBuffer();
        for (Enumeration e = set.keys(); e.hasMoreElements();) {
            File v = (File) e.nextElement();
            if (b.length() != 0) {
                b.append(separator);
            }
            String s = v.getAbsolutePath();
            // DOTO: The following needs more work!
            // Handle paths contains sep
            if (s.indexOf(separator) != -1) {
                if (s.indexOf("\"") != -1) {
                    s = "'" + s + "'";
                } else {
                    s = "\"" + s + "\"";
                }
            }
            b.append(s);
        }
        return b.toString();
    }

    private Path setToPath(Hashtable set) {
        Path ret = new Path(getProject());
        for (Enumeration e = set.keys(); e.hasMoreElements();) {
            File v = (File) e.nextElement();
            Path.PathElement el = ret.createPathElement();
            el.setLocation(v);
        }
        return ret;
    }

    /**
     * nested delete targets
     */
    public class DeleteTargets {
        private boolean all         = false;
        private boolean quiet       = false;
        private boolean failOnError = false;

        private int     myLogging   = Project.MSG_INFO;

        /**
         * whether to delete all the targets
         * or just those that are newer than the
         * corresponding sources.
         * @param all true to delete all, default false
         */
        public void setAll(boolean all) {
            this.all = all;
        }
        private boolean getAll() {
            return all;
        }

        /**
         * @param quiet if true suppress messages on deleting files
         */
        public void setQuiet(boolean quiet) {
            this.quiet = quiet;
            myLogging = quiet ? Project.MSG_VERBOSE : Project.MSG_INFO;
        }

        /**
         * @param failOnError if true halt if there is a failure to delete
         */
        public void setFailOnError(boolean failOnError) {
            this.failOnError = failOnError;
        }

        private void execute() {
            if (myLogging != Project.MSG_INFO) {
                myLogging = verbosity;
            }

            // Quiet overrides failOnError
            if (quiet) {
                failOnError = false;
            }

            Path toBeDeleted = null;
            if (all) {
                toBeDeleted = setToPath(allTargetSet);
            } else {
                toBeDeleted = setToPath(targetSet);
            }

            String[] names = toBeDeleted.list();
            for (int i = 0; i < names.length; ++i) {
                File file = new File(names[i]);
                if (!file.exists()) {
                    continue;
                }
                if (file.isDirectory()) {
                    removeDir(file);
                    continue;
                }
                log("Deleting " + file.getAbsolutePath(), myLogging);
                if (!file.delete()) {
                    String message =
                        "Unable to delete file " + file.getAbsolutePath();
                    if (failOnError) {
                        throw new BuildException(message);
                    } else {
                        log(message,  myLogging);
                    }
                }
            }
        }

        private static final int DELETE_RETRY_SLEEP_MILLIS = 10;
        /**
         * Attempt to fix possible race condition when deleting
         * files on WinXP. If the delete does not work,
         * wait a little and try again.
         */
        private boolean delete(File f) {
            if (!f.delete()) {
                try {
                    Thread.sleep(DELETE_RETRY_SLEEP_MILLIS);
                    return f.delete();
                } catch (InterruptedException ex) {
                    return f.delete();
                }
            }
            return true;
        }
        
        private void removeDir(File d) {
            String[] list = d.list();
            if (list == null) {
                list = new String[0];
            }
            for (int i = 0; i < list.length; i++) {
                String s = list[i];
                File f = new File(d, s);
                if (f.isDirectory()) {
                    removeDir(f);
                } else {
                    log("Deleting " + f.getAbsolutePath(), myLogging);
                    if (!f.delete()) {
                        String message = "Unable to delete file "
                            + f.getAbsolutePath();
                        if (failOnError) {
                            throw new BuildException(message);
                        } else {
                            log(message, myLogging);
                        }
                    }
                }
            }
            log("Deleting directory " + d.getAbsolutePath(), myLogging);
            if (!delete(d)) {
                String message = "Unable to delete directory "
                    + d.getAbsolutePath();
                if (failOnError) {
                    throw new BuildException(message);
                } else {
                    log(message, myLogging);
                }
            }
        }
    }

    /**
     *  Wrapper for mapper - includes dir
     */
    public static class MyMapper extends Mapper {
        private File dir = null;
        /**
         * Creates a new <code>MyMapper</code> instance.
         *
         * @param project the current project
         */
        public MyMapper(Project project) {
            super(project);
        }

        /**
         * @param dir the directory that the from files are relative to
         */
        public void setDir(File dir) {
            this.dir = dir;
        }

        /**
         * @return the directory that the from files are relative to
         */
        public File getDir() {
            return dir;
        }
    }
}


