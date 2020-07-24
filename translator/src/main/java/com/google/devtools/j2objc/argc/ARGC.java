/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
//  ARC+GC.h
//
//  Created by daehoon.zee on 30/10/2016.
//  https://github.com/zeedh/j2objc.git
//

package com.google.devtools.j2objc.argc;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.StandardLocation;

import com.google.devtools.j2objc.Options;

//import org.eclipse.jdt.core.dom.ITypeBinding;
import com.google.devtools.j2objc.ast.*;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.gen.SourceBuilder;
import com.google.devtools.j2objc.gen.StatementGenerator;
//import com.google.devtools.j2objc.javac.JavacEnvironment;
import com.google.devtools.j2objc.pipeline.ProcessingContext;
//import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.HeaderMap;
import com.google.devtools.j2objc.util.Mappings;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.SourceStore;
import com.google.devtools.j2objc.util.TypeUtil;
import com.strobel.assembler.InputTypeLoader;
import com.strobel.assembler.metadata.ITypeLoader;

public class ARGC {
	private static ArrayList<String> excludedClasses = new ArrayList<String>();
	private static ArrayList<String> excludedPackages = new ArrayList<String>();
	private static HashMap<String, CompilationUnit> units = new HashMap<>();
	private static HashMap<String, AbstractTypeDeclaration> types = new HashMap<>();

	public static void addExcludeRule(String classpath) {
		if (classpath.charAt(0) != '@') {
			if ('.' == classpath.charAt(classpath.length() - 1)) {
				excludedPackages.add(classpath);
			}
			else {
				excludedClasses.add(classpath);
			}
		}
		else {
			File lstf = new File(classpath.substring(1));
			ArrayList<String> files = SourceStore.readPathList(lstf);
			if (files != null) {
				for (String s : files) {
					addExcludeRule(s);
				}
			}
		}
	}

	public static boolean isExcludedPackage(String _package) {
		_package = _package.replace('/', '.') + '.';
		for (String s : excludedPackages) {
			if (_package.equals(s)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isExcludedClass(String filename) {
		filename = filename.replace('/', '.');
		if (filename.endsWith(".java")) {
			filename = filename.substring(0, filename.length() - 5);
		}
		for (String s : excludedClasses) {
			if (filename.equals(s)) {
				return true;
			}
		}
		for (String s : excludedPackages) {
			if (filename.startsWith(s)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasExcludeRule() {
		return excludedClasses.size() > 0;
	}

	public static void preprocessUnit(CompilationUnit unit) {
		for (AbstractTypeDeclaration type : unit.getTypes()) {
			types.put(type.getName().toString(), type);
		}
		preprocessUnreachableImportedClasses(unit, new HashMap<>());
	}

	
	private static HashMap<String, String> preprocessUnreachableImportedClasses(CompilationUnit unit, HashMap<String, String> processed) {
		HashMap<String, String> urMap = unit.getUnreachableImportedClasses();
		if (processed.containsKey(unit.getSourceFilePath())) {
			return urMap;
		}
		String src_f = unit.getSourceFilePath();
		processed.put(src_f, src_f);
		for (AbstractTypeDeclaration _t : unit.getTypes()) {
			TypeElement type = _t.getTypeElement();

			for (TypeMirror inheritedType : TypeUtil.directSupertypes(type.asType())) {
			  String name = inheritedType.toString();
			  int idx = name.indexOf('<');
			  if (idx > 0) {
			    name = name.substring(0, idx);
			  }
			  CompilationUnit superUnit = units.get(name);
			  if (superUnit != null) {
			    urMap.putAll(preprocessUnreachableImportedClasses(superUnit, processed));
			  }
			}
		}
		return urMap;
	}
	
	public static void registerCompilationUnit(CompilationUnit unit) {
		for (AbstractTypeDeclaration _t : unit.getTypes()) {
			TypeElement type = _t.getTypeElement();
			String name = type.getQualifiedName().toString();
	    	units.put(name, unit);
		}
	}

}

