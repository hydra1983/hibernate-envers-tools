package com.wds.tools.envers.cli.support;

import static com.wds.tools.envers.cli.utils.PropertyUtils.getProperty;
import io.airlift.command.Option;
import io.airlift.command.OptionType;

import java.util.List;
import java.util.Properties;

import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import com.wds.tools.envers.cli.support.executor.JdbcExecutor;
import com.wds.tools.envers.cli.utils.ConnectionUrl;
import com.wds.tools.envers.cli.utils.Consts;
import com.wds.tools.envers.cli.utils.Exceptions;

public abstract class AbstractCommand implements Runnable {
	@Option(type = OptionType.GLOBAL, name = { "-v", "--verbose" }, description = "Verbose mode")
	public boolean verbose;

	@Option(name = "--url", description = "Url of target database", required = true)
	public String url;

	@Option(name = { "-D" }, description = "System parameters")
	public List<String> parameters;

	@Override
	public void run() {
	}

	protected CommandLineContext getContext() {
		return CommandLineContext.current();
	}

	protected Properties loadProps() {
		Properties props = new Properties();
		if (this.parameters != null && this.parameters.size() > 0) {
			String propsString = Joiner.on(Consts.LINE_SEPERATOR).join(this.parameters);
			try {
				props.load(CharStreams.newReaderSupplier(propsString).getInput());
			} catch (Exception e) {
				throw Exceptions.runtime(e);
			}
		}
		return props;
	}

	protected Executor getExecutor() {
		Properties props = loadProps();

		Executor executor = null;
		ConnectionUrl url = new ConnectionUrl(getProperty(props, Consts.HIBERNATE_CONNECTION_URL, this.url));
		if (url.isJdbc()) {
			executor = new JdbcExecutor(this, props);
		} else {
			throw Exceptions.illegalArgument("Unknown type ''{0}''", url.getUrlType());
		}
		return executor;
	}
}