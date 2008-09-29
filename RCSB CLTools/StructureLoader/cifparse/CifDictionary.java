//
// CifDictionary.java,v 1.3 2001/10/27 02:02:47 dsg Exp
//
// Copyright 2001 The Regents of the University of California
// All Rights Reserved
//
// OpenMMS was developed by Dr. Douglas S. Greer at the San Diego
// Supercomputer Center, a research unit of the University of California,
// San Diego.  Support for this effort was provided by NSF through the
// Protein Data Bank (Grant DBI-9814284) and the National Partnership for
// Advanced Computational Infrastructure (Grant ACI-9619020)
//
// Permission to use, copy, modify and distribute any part of OpenMMS for
// educational, research and non-profit purposes, without fee, and
// without a written agreement is hereby granted, provided that the above
// copyright notice, this paragraph and the following paragraphs appear
// in all copies.
//
// Those desiring to incorporate this OpenMMS into commercial products or
// use for commercial purposes should contact the Technology Transfer
// Office, University of California, San Diego, 9500 Gilman Drive, La
// Jolla, CA 92093-0910, Ph: (619) 534-5815, FAX: (619) 534-7345.
//
// In no event shall the University of California be liable to any party
// for direct, indirect, special, incidental, or consequential damages,
// including lost profits, arising out of the use of this OpenMMS, even
// if the University of California has been advised of the possibility of
// such damage.
//
// The OpenMMS provided herein is on an "as is" basis, and the
// University of California has no obligation to provide maintenance,
// support, updates, enhancements, or modifications.  The University of
// California makes no representations and extends no warranties of any
// kind, either implied or express, including, but not limited to, the
// implied warranties of merchantability or fitness for a particular
// purpose, or that the use of the OpenMMS will not infringe any patent,
// trademark or other rights.

package org.rcsb.mbt.structLoader.openmms.cifparse;

import java.util.*;

/**
 * Structure to hold information read from a cif dictionary
 *
 * @author Douglas S. Greer
 * @version 1.3
 */
public class CifDictionary
{
  private DictionaryCategoryList dcl;
  private DictionaryItemList dil;
  private Properties parents;
  
  public CifDictionary()
    {
      this.dcl = new DictionaryCategoryList();
      this.dil = new DictionaryItemList();
      this.parents = new Properties();
    }

  public DictionaryCategoryList getDictionaryCategoryList()
    {
      return this.dcl;
    }

  public DictionaryItemList getDictionaryItemList()
    {
      return this.dil;
    }

  public void checkAllItemsDefined()
    throws CifParseException
    {
      this.dcl.checkAllItemsDefined();
      this.dil.checkAllItemsDefined();
    }

  public boolean isCategory(final String s_)
    {
	  String s = s_;
      // ignore leading "_"
      // Category names do not begin with a "_"; Item names do. 
      if (s.charAt(0) == '_')
	  {
	    s = s.substring(1, s.length());
	  }
      return this.dcl.isAlreadyDefined(s);
    }

  public boolean isItem(final String s_)
    {
	  String s = s_;
      // ignore leading "_"
      // Item names begin with a "_", Category names do not.
      if (s.charAt(0) != '_')
	  {
	    s = "_" + s;
	  }
      return this.dil.isAlreadyDefined(s);
    }

  public void setParent(final String child, final String parent)
    {
      this.parents.setProperty(child, parent);
    }

  public String getParent(final String child)
    {
      return this.parents.getProperty(child);
    }
}
