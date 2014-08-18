/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hive.ql.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.io.RecordIdentifier;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;

public class VirtualColumn implements Serializable {

  private static final long serialVersionUID = 1L;

  public static VirtualColumn FILENAME = new VirtualColumn("INPUT__FILE__NAME", (PrimitiveTypeInfo)TypeInfoFactory.stringTypeInfo);
  public static VirtualColumn BLOCKOFFSET = new VirtualColumn("BLOCK__OFFSET__INSIDE__FILE", (PrimitiveTypeInfo)TypeInfoFactory.longTypeInfo);
  public static VirtualColumn ROWOFFSET = new VirtualColumn("ROW__OFFSET__INSIDE__BLOCK", (PrimitiveTypeInfo)TypeInfoFactory.longTypeInfo);

  public static VirtualColumn RAWDATASIZE = new VirtualColumn("RAW__DATA__SIZE", (PrimitiveTypeInfo)TypeInfoFactory.longTypeInfo);
  /**
   * {@link org.apache.hadoop.hive.ql.io.RecordIdentifier} 
   */
  public static VirtualColumn ROWID = new VirtualColumn("ROW__ID", RecordIdentifier.StructInfo.typeInfo, true, RecordIdentifier.StructInfo.oi);

  /**
   * GROUPINGID is used with GROUP BY GROUPINGS SETS, ROLLUP and CUBE.
   * It composes a bit vector with the "0" and "1" values for every
   * column which is GROUP BY section. "1" is for a row in the result
   * set if that column has been aggregated in that row. Otherwise the
   * value is "0".  Returns the decimal representation of the bit vector.
   */
  public static VirtualColumn GROUPINGID =
      new VirtualColumn("GROUPING__ID", (PrimitiveTypeInfo) TypeInfoFactory.intTypeInfo);

  public static VirtualColumn[] VIRTUAL_COLUMNS =
      new VirtualColumn[] {FILENAME, BLOCKOFFSET, ROWOFFSET, RAWDATASIZE, GROUPINGID, ROWID};

  private String name;
  private TypeInfo typeInfo;
  private boolean isHidden = true;
  private ObjectInspector oi;

  public VirtualColumn() {
  }

  public VirtualColumn(String name, PrimitiveTypeInfo typeInfo) {
    this(name, typeInfo, true, 
      PrimitiveObjectInspectorFactory.getPrimitiveWritableObjectInspector(typeInfo));
  }

  VirtualColumn(String name, TypeInfo typeInfo, boolean isHidden, ObjectInspector oi) {
    this.name = name;
    this.typeInfo = typeInfo;
    this.isHidden = isHidden;
    this.oi = oi;
  }

  public static List<VirtualColumn> getStatsRegistry(Configuration conf) {
    List<VirtualColumn> l = new ArrayList<VirtualColumn>();
    if (HiveConf.getBoolVar(conf, HiveConf.ConfVars.HIVE_STATS_COLLECT_RAWDATASIZE)) {
      l.add(RAWDATASIZE);
    }
    return l;
  }

  public static List<VirtualColumn> getRegistry(Configuration conf) {
    ArrayList<VirtualColumn> l = new ArrayList<VirtualColumn>();
    l.add(BLOCKOFFSET);
    l.add(FILENAME);
    if (HiveConf.getBoolVar(conf, HiveConf.ConfVars.HIVEROWOFFSET)) {
      l.add(ROWOFFSET);
    }
    l.add(ROWID);

    return l;
  }

  public TypeInfo getTypeInfo() {
    return typeInfo;
  }

  public void setTypeInfo(PrimitiveTypeInfo typeInfo) {
    this.typeInfo = typeInfo;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isHidden() {
    return isHidden;
  }

  public boolean getIsHidden() {
    return isHidden;
  }

  public void setIsHidden(boolean isHidden) {
    this.isHidden = isHidden;
  }
  public ObjectInspector getObjectInspector() {
    return oi;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    VirtualColumn c = (VirtualColumn) o;
    return this.name.equals(c.name)
        && this.typeInfo.getTypeName().equals(c.getTypeInfo().getTypeName());
  }

  public static Collection<String> removeVirtualColumns(final Collection<String> columns) {
    for(VirtualColumn vcol : VIRTUAL_COLUMNS) {
      columns.remove(vcol.getName());
    }
    return columns;
  }

  public static StructObjectInspector getVCSObjectInspector(List<VirtualColumn> vcs) {
    List<String> names = new ArrayList<String>(vcs.size());
    List<ObjectInspector> inspectors = new ArrayList<ObjectInspector>(vcs.size());
    for (VirtualColumn vc : vcs) {
      names.add(vc.getName());
      inspectors.add(vc.oi);
    }
    return ObjectInspectorFactory.getStandardStructObjectInspector(names, inspectors);
  }
}
