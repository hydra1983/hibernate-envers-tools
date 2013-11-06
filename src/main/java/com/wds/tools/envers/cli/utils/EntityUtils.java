package com.wds.tools.envers.cli.utils;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import net.sf.corn.cps.CPScanner;
import net.sf.corn.cps.ClassFilter;

public class EntityUtils {
	public static Collection<Class<?>> findEntities(String basepackage) {
		return CPScanner.scanClasses(new ClassFilter().packageName(basepackage).annotation(Entity.class)
				.joinAnnotationsWithOr().annotation(MappedSuperclass.class));
	}
}