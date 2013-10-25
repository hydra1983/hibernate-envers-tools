package com.wds.tools.envers.cli.support.executor;

import static com.wds.tools.envers.cli.utils.PropertyUtils.putProperty;
import static com.wds.tools.envers.cli.utils.ValidateUtils.shouldNotNull;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import net.sf.corn.cps.CPScanner;
import net.sf.corn.cps.ClassFilter;
import net.sf.corn.cps.CombinedFilter;
import net.sf.corn.cps.ResourceFilter;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.envers.Audited;
import org.hibernate.envers.configuration.AuditConfiguration;
import org.hibernate.envers.event.AuditEventListener;
import org.hibernate.event.EventSource;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.EntityPersister;

import com.javaetmoi.core.persistence.hibernate.LazyLoadingUtil;
import com.wds.tools.envers.cli.support.Executor;
import com.wds.tools.envers.cli.support.command.InstallCommand;
import com.wds.tools.envers.cli.utils.ClassUtils;
import com.wds.tools.envers.cli.utils.ConnectionUrl;
import com.wds.tools.envers.cli.utils.Consts;
import com.wds.tools.envers.cli.utils.Exceptions;
import com.wds.tools.envers.cli.utils.StringUtils;

public class JdbcExecutor implements Executor {
	private static Map<String, String> dbDriverMap;
	private static Map<String, String> dbDialectMap;

	{
		dbDriverMap = new HashMap<String, String>();
		dbDriverMap.put("h2", "org.h2.Driver");

		dbDialectMap = new HashMap<String, String>();
		dbDialectMap.put("h2", "org.hibernate.dialect.H2Dialect");
	}

	public JdbcExecutor(Runnable command, Properties props) {
		this.command = command;
		this.props = props;
	}

	private final Runnable command;
	private final Properties props;

	@Override
	public void install() {
		List<Class<?>> initialized = getInitializedEntities();
		initializeRevisionInfo(initialized);
	}

	private List<Class<?>> getInitializedEntities() {
		List<Class<?>> initialized = new ArrayList<Class<?>>();
		Map<String, Class<?>> targetTables = getTargetTables();
		List<String> exsistingTables = getExistingTables();
		for (String targetTable : targetTables.keySet()) {
			if (exsistingTables.contains(targetTable)) {
				initialized.add(targetTables.get(targetTable));
			}
		}
		return initialized;
	}

	private Map<String, Class<?>> getTargetTables() {
		Map<String, Class<?>> tables = new HashMap<String, Class<?>>();

		Configuration cfg = configure();
		AuditConfiguration audCfg = new AuditConfiguration(cfg);
		cfg.buildMappings();

		Iterator<PersistentClass> pci = cfg.getClassMappings();
		while (pci.hasNext()) {
			PersistentClass pc = pci.next();
			Class<?> entity = pc.getMappedClass();
			if (entity.isAnnotationPresent(Audited.class)) {
				String entityName = pc.getEntityName();
				String auditEntityName = audCfg.getAuditEntCfg().getAuditEntityName(entityName);
				String auditTableName = cfg.getNamingStrategy().classToTableName(auditEntityName);
				tables.put(auditTableName, entity);
			}
		}

		return tables;
	}

	private List<String> getExistingTables() {
		List<String> tables = new ArrayList<String>();
		InstallCommand cmd = (InstallCommand) this.command;
		try {
			Connection con = DriverManager.getConnection(cmd.url, cmd.username, cmd.password);
			DatabaseMetaData meta = con.getMetaData();
			ResultSet res = meta.getTables(null, null, null, new String[] { "TABLE" });
			while (res.next()) {
				tables.add(res.getString("TABLE_NAME").toLowerCase());
			}
			res.close();
			con.close();
		} catch (Exception e) {
			throw Exceptions.runtime(e);
		}
		return tables;
	}

	private void initializeRevisionInfo(List<Class<?>> initialized) {
		Configuration cfg = configure2();
		SessionFactory sessionFactory = cfg.buildSessionFactory();

		AuditEventListener listener = new AuditEventListener();
		listener.initialize(cfg);

		List<Object> entities = getEntityData(sessionFactory, initialized);
		if (entities != null && entities.size() > 0) {
			EventSource source = (EventSource) sessionFactory.openSession();
			Transaction tx = source.beginTransaction();
			for (Object entity : entities) {
				EntityPersister persister = source.getEntityPersister(null, entity);
				Object[] state = persister.getPropertyValuesToInsert(entity, null, source);
				ClassMetadata metadata = sessionFactory.getClassMetadata(persister.getEntityName());
				Serializable id = (Serializable) getFieldValue(entity, metadata.getIdentifierPropertyName());
				PostInsertEvent event = new PostInsertEvent(entity, id, state, persister, source);
				listener.onPostInsert(event);
			}
			tx.commit();
			source.close();
		}
	}

	private Object getFieldValue(Object instance, String fieldName) {
		shouldNotNull(instance, "requires ''instance''");
		Object value = null;
		Class<?> javaType = instance.getClass();
		try {
			Field field = javaType.getDeclaredField(fieldName);
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			value = field.get(instance);
			field.setAccessible(accessible);
		} catch (Exception e) {
			throw Exceptions.runtime(e);
		}
		return value;
	}

	private List<Object> getEntityData(SessionFactory sessionFactory, List<Class<?>> initialized) {
		List<Object> data = new ArrayList<Object>();
		Session session = sessionFactory.openSession();
		Map<String, ClassMetadata> allMetadata = sessionFactory.getAllClassMetadata();
		for (String key : allMetadata.keySet()) {
			ClassMetadata metadata = allMetadata.get(key);
			Class<?> javaType = metadata.getMappedClass(EntityMode.POJO);
			if (!initialized.contains(javaType) && javaType.isAnnotationPresent(Audited.class)) {
				Criteria criteria = session.createCriteria(metadata.getEntityName());
				@SuppressWarnings("rawtypes")
				List list = criteria.list();
				for (Object entity : list) {
					entity = LazyLoadingUtil.deepHydrate(session, entity);
					data.add(entity);
				}
			}
		}
		session.close();
		return data;
	}

	private Configuration configure() {
		InstallCommand cmd = (InstallCommand) this.command;
		ConnectionUrl url = new ConnectionUrl(cmd.url);

		CombinedFilter entityOrMappedSuperclass = new CombinedFilter();
		entityOrMappedSuperclass.appendFilter(new ClassFilter().annotation(Entity.class));
		entityOrMappedSuperclass.appendFilter(new ClassFilter().annotation(MappedSuperclass.class));
		entityOrMappedSuperclass.combineWithOr();

		CombinedFilter packageAndAnnotation = new CombinedFilter();
		packageAndAnnotation.appendFilter(new ResourceFilter().packageName(cmd.basepackage));
		packageAndAnnotation.appendFilter(new ClassFilter().annotation(Entity.class));
		packageAndAnnotation.combineWithAnd();

		List<Class<?>> entities = CPScanner.scanClasses(new ClassFilter().packageName(cmd.basepackage)
				.annotation(Entity.class).joinAnnotationsWithOr().annotation(MappedSuperclass.class));
		Class<?> revent = ClassUtils.forName(cmd.revent);
		entities.add(revent);

		// resolve properties
		String driver = (String) shouldNotNull(dbDriverMap.get(url.getDatabaseType()),
				StringUtils.replace("Cannot find dirver for ''{0}''", url.getDatabaseType()));
		String dialect = (String) shouldNotNull(dbDialectMap.get(url.getDatabaseType()),
				StringUtils.replace("Cannot find dialect for ''{0}''", url.getDatabaseType()));

		// hibernate
		putProperty(this.props, Consts.HIBERNATE_CONNECTION_URL, cmd.url);
		putProperty(this.props, "hibernate.connection.username", cmd.username);
		putProperty(this.props, "hibernate.connection.password", cmd.password);
		putProperty(this.props, "hibernate.connection.driver_class", driver);
		putProperty(this.props, "hibernate.dialect", dialect);
		putProperty(this.props, "hibernate.hbm2ddl.auto", "update");
		putProperty(this.props, "hibernate.ejb.naming_strategy", "org.hibernate.cfg.ImprovedNamingStrategy");

		// envers
		putProperty(this.props, "org.hibernate.envers.audit_strategy",
				"org.hibernate.envers.strategy.ValidityAuditStrategy");

		// create configuration
		Configuration cfg = new Configuration();
		cfg.addProperties(this.props);
		for (Class<?> entity : entities) {
			cfg.addAnnotatedClass(entity);
		}
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
}
