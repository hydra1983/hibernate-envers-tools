package com.wds.tools.envers.cli.utils;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import net.sf.corn.cps.CPScanner;
import net.sf.corn.cps.ClassFilter;

import com.google.common.collect.Sets;

public class EntityUtils {
	public static Set<Class<?>> findEntities(String basepackage) {
		return Sets.newHashSet(CPScanner.scanClasses(new ClassFilter().packageName(basepackage)
				.annotation(Entity.class).joinAnnotationsWithOr().annotation(MappedSuperclass.class)));
	}
}