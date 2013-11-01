package com.wds.tools.envers.cli.support.executor;

import static com.wds.tools.envers.cli.utils.PropertyUtils.putProperty;
import static com.wds.tools.envers.cli.utils.ValidateUtils.shouldNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import net.sf.corn.cps.CPScanner;
import net.sf.corn.cps.ClassFilter;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.event.AuditEventListener;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.event.EventSource;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.EntityPersister;

import com.google.common.io.Resources;
import com.javaetmoi.core.persistence.hibernate.LazyLoadingUtil;
import com.wds.tools.envers.cli.support.Executor;
import com.wds.tools.envers.cli.support.command.InstallCommand;
import com.wds.tools.envers.cli.utils.ClassUtils;
import com.wds.tools.envers.cli.utils.ConnectionUrl;
import com.wds.tools.envers.cli.utils.Console;
import com.wds.tools.envers.cli.utils.Consts;
import com.wds.tools.envers.cli.utils.PropertyUtils;
import com.wds.tools.envers.cli.utils.Reflections;
import com.wds.tools.envers.cli.utils.StringUtils;

public class JdbcExecutor implements Executor {
	private static Properties drivers;
	private static Properties dialects;
	{
		drivers = PropertyUtils.loadProperties(Resources.getResource("drivers.properties"));
		dialects = PropertyUtils.loadProperties(Resources.getResource("dialects.properties"));
	}

	public JdbcExecutor(Runnable command, Properties props) {
		this.command = command;
		this.props = props;
	}

	private final Runnable command;
	private final Properties props;

	private InstallCommand install;

	private boolean verbose;

	@Override
	public void install() {
		this.install = (InstallCommand) command;
		this.verbose = this.install.verbose;
		initializeRevisionInfo();
	}

	private void initializeRevisionInfo() {
		Configuration cfg = configure2();
		SessionFactory sessionFactory = cfg.buildSessionFactory();

		AuditEventListener listener = new AuditEventListener();
		listener.initialize(cfg);

		List<Object> entities = getEntityData(sessionFactory);
		if (entities != null && entities.size() > 0) {
			EventSource source = (EventSource) sessionFactory.openSession();
			Transaction tx = source.beginTransaction();
			for (Object entity : entities) {
				EntityPersister persister = source.getEntityPersister(null, entity);
				Object[] state = persister.getPropertyValuesToInsert(entity, null, source);
				ClassMetadata metadata = sessionFactory.getClassMetadata(persister.getEntityName());
				Serializable id = (Serializable) Reflections
						.getFieldValue(entity, metadata.getIdentifierPropertyName());
				PostInsertEvent event = new PostInsertEvent(entity, id, state, persister, source);
				listener.onPostInsert(event);
				verbose("Auditing entity ''{0}'' with id ''{1}''", entity.getClass().getName(), id);
			}
			verbose("");
			tx.commit();
			source.close();
		}
	}

	private List<Object> getEntityData(SessionFactory sessionFactory) {
		List<Object> data = new ArrayList<Object>();
		Session session = sessionFactory.openSession();
		AuditReader reader = AuditReaderFactory.get(session);
		Map<String, ClassMetadata> allMetadata = sessionFactory.getAllClassMetadata();
		for (String key : allMetadata.keySet()) {
			ClassMetadata metadata = allMetadata.get(key);
			Class<?> javaType = metadata.getMappedClass(EntityMode.POJO);
			if (javaType.isAnnotationPresent(Audited.class)) {
				verbose("Retrieving data for entity ''{0}''", javaType.getName());
				Criteria criteria = session.createCriteria(metadata.getEntityName());
				@SuppressWarnings("rawtypes")
				List entities = criteria.list();
				for (Object entity : entities) {
					String idName = metadata.getIdentifierPropertyName();
					Serializable id = (Serializable) Reflections.getFieldValue(entity, idName);
					AuditQuery query = reader.createQuery().forRevisionsOfEntity(javaType, false, true);
					query.addOrder(AuditEntity.revisionNumber().asc());
					query.add(AuditEntity.revisionType().eq(RevisionType.ADD));
					query.add(AuditEntity.id().eq(id));
					query.setMaxResults(1);
					@SuppressWarnings("rawtypes")
					List list = query.getResultList();
					if (list != null && list.size() == 1) {
						verbose("Entity ''{0}'' with id ''{1}'' already audited", javaType.getName(), id);
					} else {
						verbose("Entity ''{0}'' with id ''{1}'' will be audited", javaType.getName(), id);
						entity = LazyLoadingUtil.deepHydrate(session, entity);
						data.add(entity);
					}
				}
				verbose("");
			}
		}
		session.close();
		return data;
	}

	private Configuration configure() {
		ConnectionUrl url = new ConnectionUrl(this.install.url);

		if (url.isJdbc()) {
			shouldNotNull(this.install.basepackage, "Base package should not be null : ''--basepackage'' is required");
		}

		List<Class<?>> entities = CPScanner.scanClasses(new ClassFilter().packageName(this.install.basepackage)
				.annotation(Entity.class).joinAnnotationsWithOr().annotation(MappedSuperclass.class));

		if (this.install.revent != null && this.install.revent != "") {
			Class<?> revent = ClassUtils.forName(this.install.revent);
			entities.add(revent);
		}

		// resolve properties
		String driver = (String) shouldNotNull(
				PropertyUtils.getProperty(drivers, url.getDatabaseType(), this.install.driver),
				StringUtils.replace("Cannot find dirver for ''{0}''", url.getDatabaseType()));
		String dialect = (String) shouldNotNull(
				PropertyUtils.getProperty(dialects, url.getDatabaseType(), this.install.dialect),
				StringUtils.replace("Cannot find dialect for ''{0}''", url.getDatabaseType()));

		// hibernate
		putProperty(this.props, Consts.HIBERNATE_CONNECTION_URL, this.install.url);
		putProperty(this.props, "hibernate.connection.username", this.install.username);
		putProperty(this.props, "hibernate.connection.password", this.install.password);
		putProperty(this.props, "hibernate.connection.driver_class", driver);
		putProperty(this.props, "hibernate.dialect", dialect);
		putProperty(this.props, "hibernate.hbm2ddl.auto", "update");
		putProperty(this.props, "hibernate.ejb.naming_strategy", "org.hibernate.cfg.ImprovedNamingStrategy");

		// envers
		putProperty(this.props, "org.hibernate.envers.audit_strategy",
				"org.hibernate.envers.strategy.ValidityAuditStrategy");
		verbose(this.props);
		verbose("");
		// create configuration
		Configuration cfg = new Configuration();
		cfg.addProperties(this.props);
		verbose("Audited Entities : ");
		for (Class<?> entity : entities) {
			if (entity.isAnnotationPresent(Audited.class)) {
				verbose(entity.getName());
			}
			cfg.addAnnotatedClass(entity);
		}
		verbose("");
		return cfg;
	}

	private Configuration configure2() {
		Configuration cfg = configure();
		cfg.setListener("post-insert", new AuditEventListener());
		cfg.setListener("post-update", new AuditEventListener());
		cfg.setListener("post-delete", new AuditEventListener());
		cfg.setListener("pre-collection-update", new AuditEventListener());
		cfg.setListener("pre-collection-remove", new AuditEventListener());
		cfg.setListener("post-collection-recreate", new AuditEventListener());
		return cfg;
	}

	private void verbose(String message, Object... args) {
		if (this.verbose) {
			Console.info(message, args);
		}
	}

	private void verbose(Properties props) {
		verbose("Configration properties :");
		for (String key : props.stringPropertyNames()) {
			verbose("{0} = {1}", key, props.getProperty(key));
		}
	}
}
