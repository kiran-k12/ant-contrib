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
package net.sf.antcontrib.logic.condition;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.tools.ant.taskdefs.ConditionTask;
import org.apache.tools.ant.taskdefs.condition.*;
import org.apache.tools.ant.taskdefs.condition.Condition;

/**
 * Extends ConditionBase so I can get access to the condition count and the
 * first condition. This is the class that the BooleanConditionTask is proxy
 * for.
 * <p>Developed for use with Antelope, migrated to ant-contrib Oct 2003.
 *
 * @author     Dale Anson, danson@germane-software.com
 */
public class BooleanConditionBase extends ConditionBase {
   /**
    * Gets the conditionCount attribute of the BooleanConditionBase object
    *
    * @return   The conditionCount value
    */
   public int getConditionCount() {
      return countConditions();
   }


   /**
    * Gets the firstCondition attribute of the BooleanConditionBase object
    *
    * @return   The firstCondition value
    */
   public Condition getFirstCondition() {
      return (Condition)getConditions().nextElement();
   }


   /**
    * Adds a feature to the IsPropertyTrue attribute of the BooleanConditionBase
    * object
    *
    * @param i  The feature to be added to the IsPropertyTrue attribute
    */
   public void addIsPropertyTrue( IsPropertyTrue i ) {
      super.addIsTrue( i );
   }


   /**
    * Adds a feature to the IsPropertyFalse attribute of the
    * BooleanConditionBase object
    *
    * @param i  The feature to be added to the IsPropertyFalse attribute
    */
   public void addIsPropertyFalse( IsPropertyFalse i ) {
      super.addIsFalse( i );
   }
   
   public void addIsGreaterThan( IsGreaterThan i) {
      super.addEquals(i);  
   }
   
   public void addIsLessThan( IsLessThan i) {
      super.addEquals(i);  
   }
}

