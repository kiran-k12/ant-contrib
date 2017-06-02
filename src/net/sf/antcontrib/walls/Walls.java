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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


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
public class Walls {
    
    private List packages = new LinkedList();
    private Map nameToPackage = new HashMap();
    
    public Package getPackage(String name) {
        return (Package)nameToPackage.get(name);        
    }
    
    public void addConfiguredPackage(Package p) {
        
        String pack = p.getPackage();
        if(!pack.endsWith(".*") && !pack.endsWith(".**"))
            p.setFaultReason("The package='"+pack+"' must end with "
                        +".* or .** such as biz.xsoftware.* or "
                        +"biz.xsoftware.**");
        
        String[] depends = p.getDepends();
        if(depends == null) {
            nameToPackage.put(p.getName(), p);
            packages.add(p);
            return;
        } 
        
        //make sure all depends are in Map first
        //circular references then are not a problem because they must
        //put the stuff in order
        for(int i = 0; i < depends.length; i++) {
            Package dependsPackage = (Package)nameToPackage.get(depends[i]);
            
            if(dependsPackage == null) {
                p.setFaultReason("package name="+p.getName()+" did not have "
                        +depends[i]+" listed before it and cannot compile without it");
            }
        }
        
        nameToPackage.put(p.getName(), p);
        packages.add(p);
    }

    public Iterator getPackagesToCompile() {
        //must return the list, as we need to process in order, so unfortunately
        //we cannot pass back an iterator from the hashtable because that would
        //be unordered and would break.
        return packages.iterator();
    }    
}