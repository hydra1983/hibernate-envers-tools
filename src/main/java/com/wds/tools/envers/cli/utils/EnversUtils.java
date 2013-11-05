package com.wds.tools.envers.cli.utils;

import static org.hibernate.envers.tools.ArraysTools.arrayIncludesInstanceOf;

import org.hibernate.SessionFactory;
import org.hibernate.envers.configuration.AuditConfiguration;
import org.hibernate.envers.event.AuditEventListener;
import org.hibernate.event.EventListeners;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.impl.SessionFactoryImpl;

public class EnversUtils {
	public static AuditEventListener getAuditEventListener(SessionFactory sessionFactory) {
		AuditEventListener eventListener = null;
		if (sessionFactory instanceof SessionFactoryImpl) {
			SessionFactoryImpl impl = (SessionFactoryImpl) sessionFactory;
			EventListeners listeners = impl.getEventListeners();

			for (PostInsertEventListener listener : listeners.getPostInsertEventListeners()) {
				if (listener instanceof AuditEventListener) {
					if (arrayIncludesInstanceOf(listeners.getPostUpdateEventListeners(), AuditEventListener.class)
							&& arrayIncludesInstanceOf(listeners.getPostDeleteEventListeners(),
									AuditEventListener.class)) {
						eventListener = (AuditEventListener) listener;
					}
				}
			}
		} else {
			throw Exceptions.runtime("Cannot resolve ''{0}'' as ''sessionFactory'' is not an instanceof ''{1}''",
					AuditConfiguration.class.getSimpleName(), SessionFactoryImpl.class.getName());
		}
		
		if (eventListener == null) {
			throw Exceptions.runtime("You need to install the org.hibernate.envers.event.AuditEventListener "
					+ "class as post insert, update and delete event listener.");
		}
		
		return eventListener;
	}
}
