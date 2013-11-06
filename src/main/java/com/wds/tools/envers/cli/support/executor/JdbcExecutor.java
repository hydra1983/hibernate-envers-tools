package com.wds.tools.envers.cli.support.executor;

import static com.wds.tools.envers.cli.utils.PropertyUtils.getProperty;
import static com.wds.tools.envers.cli.utils.PropertyUtils.putProperty;
import static com.wds.tools.envers.cli.utils.ValidateUtils.shouldNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.Audited;
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
import com.wds.tools.envers.cli.utils.Consts;
import com.wds.tools.envers.cli.utils.EntityUtils;
import com.wds.tools.envers.cli.utils.EnversUtils;
import com.wds.tools.envers.cli.utils.Logger;
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
		this.args = new CommandArgs(command);
		this.props = props;
	}

	private final CommandArgs args;
	private final Properties props;

	@Override
	public void install() {
		Configuration cfg = configure();
		SessionFactory sessionFactory = cfg.buildSessionFactory();
		List<Object> entities = getEntityData(sessionFactory);

		if (entities != null && entities.size() > 0) {
			verbose("Auditing entities...");
			AuditEventListener listener = EnversUtils.getAuditEventListener(sessionFactory);
			EventSource source = (EventSource) sessionFactory.openSession();
			Transaction tx = source.beginTransaction();
			for (Object entity : entities) {
				EntityPersister persister = source.getEntityPersister(null, entity);
				Object[] state = persister.getPropertyValuesToInsert(entity, null, source);
				ClassMetadata metadata = sessionFactory.getClassMetadata(persister.getEntityName());
				Serializable id = (Serializable) Reflections.getValue(entity, metadata.getIdentifierPropertyName());
				PostInsertEvent event = new PostInsertEvent(entity, id, state, persister, source);
				listener.onPostInsert(event);
				verbose("Auditing entity ''{0}'' with id ''{1}''", entity.getClass().getName(), id);
			}
			tx.commit();
			source.close();
		}
	}

	private List<Object> getEntityData(SessionFactory sessionFactory) {
		List<Object> data = new ArrayList<Object>();
		Session session = sessionFactory.openSession();
		AuditReader reader = AuditReaderFactory.get(session);
		Map<String, ClassMetadata> allMetadata = sessionFactory.getAllClassMetadata();
		verbose("Retrieving data...");
		for (String key : allMetadata.keySet()) {
			ClassMetadata metadata = allMetadata.get(key);
			Class<?> javaType = metadata.getMappedClass(EntityMode.POJO);
			if (javaType.isAnnotationPresent(Audited.class)) {
				Criteria criteria = session.createCriteria(metadata.getEntityName());
				@SuppressWarnings("rawtypes")
				List entities = criteria.list();
				for (Object entity : entities) {
					String idName = metadata.getIdentifierPropertyName();
					Serializable id = (Serializable) Reflections.getValue(entity, idName);
					AuditQuery query = reader.createQuery().forRevisionsOfEntity(javaType, false, true);
					query.addOrder(AuditEntity.revisionNumber().asc());
					query.add(AuditEntity.id().eq(id));
					query.setMaxResults(1);
					@SuppressWarnings("rawtypes")
					List list = query.getResultList();
					if (list != null && list.size() > 0) {
						verbose("Entity ''{0}'' with id ''{1}'' already Audited", javaType.getName(), id);
					} else {
						verbose("Entity ''{0}'' with id ''{1}'' will be audited", javaType.getName(), id);
						entity = LazyLoadingUtil.deepHydrate(session, entity);
						data.add(entity);
					}
				}
			}
		}
		session.close();
		return data;
	}

	private Configuration configure() {
		ConnectionUrl url = new ConnectionUrl(this.args.url);

		if (url.isJdbc()) {
			shouldNotNull(this.args.basepackages, "Base package should not be null : ''--basepackage'' is required");
		}

		Set<Class<?>> entities = EntityUtils.findEntities(this.args.basepackages);

		if (this.args.revent != null && this.args.revent != "") {
			Class<?> revent = ClassUtils.forName(this.args.revent);
			entities.add(revent);
		}

		// resolve properties
		String driver = (String) shouldNotNull(
				PropertyUtils.getProperty(drivers, url.getDatabaseType(), this.args.driver),
				StringUtils.replace("Cannot find dirver for ''{0}''", url.getDatabaseType()));
		String dialect = (String) shouldNotNull(
				PropertyUtils.getProperty(dialects, url.getDatabaseType(), this.args.dialect),
				StringUtils.replace("Cannot find dialect for ''{0}''", url.getDatabaseType()));

		// hibernate props
		putProperty(this.props, Consts.HIBERNATE_CONNECTION_URL, this.args.url);
		putProperty(this.props, "hibernate.connection.username", this.args.username);
		putProperty(this.props, "hibernate.connection.password", this.args.password);
		putProperty(this.props, "hibernate.connection.driver_class", driver);
		putProperty(this.props, "hibernate.dialect", dialect);
		putProperty(this.props, "hibernate.hbm2ddl.auto", "update");

		// envers props
		putProperty(this.props, "org.hibernate.envers.audit_strategy",
				"org.hibernate.envers.strategy.ValidityAuditStrategy");

		// create configuration
		Configuration cfg = new Configuration();

		// properties
		verbose("Configration properties :");
		cfg.addProperties(this.props);
		for (String key : props.stringPropertyNames()) {
			verbose("{0} = {1}", key, props.getProperty(key));
		}

		// naming strategy
		String namingStrategyClassName = getProperty(this.props, "hibernate.ejb.naming_strategy",
				"org.hibernate.cfg.ImprovedNamingStrategy");
		cfg.setNamingStrategy((NamingStrategy) ClassUtils.newInstance(namingStrategyClassName));

		verbose("Naming Strategy : {0}", namingStrategyClassName);

		// entities
		verbose("Audited Entities : ");
		for (Class<?> entity : entities) {
			if (entity.isAnnotationPresent(Audited.class)) {
				verbose(entity.getName());
			}
			cfg.addAnnotatedClass(entity);
		}

		// listeners
		cfg.setListener("post-insert", new AuditEventListener());
		cfg.setListener("post-update", new AuditEventListener());
		cfg.setListener("post-delete", new AuditEventListener());
		cfg.setListener("pre-collection-update", new AuditEventListener());
		cfg.setListener("pre-collection-remove", new AuditEventListener());
		cfg.setListener("post-collection-recreate", new AuditEventListener());

		return cfg;
	}

	private void verbose(String message, Object... args) {
		if (this.args.verbose) {
			Logger.info(0, message, args);
		}
	}

	private static class CommandArgs {
		public boolean verbose;
		public String dialect;
		public String driver;
		public String password;
		public String username;
		public String revent;
		public String basepackages;
		public String url;

		public CommandArgs(Runnable command) {
			initialize(command);
		}

		private void initialize(Runnable command) {
			if (command instanceof InstallCommand) {
				InstallCommand install = (InstallCommand) command;
				this.verbose = install.verbose;
				this.dialect = install.dialect;
				this.driver = install.driver;
				this.password = install.password;
				this.username = install.username;
				this.revent = install.revent;
				this.basepackages = install.basepackages;
				this.url = install.url;
			}

			if (verbose) {
				Logger.info("Command args : ");
				Logger.info(1, "verbose : ''{0}''", this.verbose);
				Logger.info(1, "dialect : ''{0}''", this.dialect);
				Logger.info(1, "driver : ''{0}''", this.driver);
				Logger.info(1, "password : ''{0}''", this.password);
				Logger.info(1, "username : ''{0}''", this.username);
				Logger.info(1, "revent : ''{0}''", this.revent);
				Logger.info(1, "basepackages : ''{0}''", this.basepackages);
				Logger.info(1, "url : ''{0}''", this.url);
			}
		}
	}
}
