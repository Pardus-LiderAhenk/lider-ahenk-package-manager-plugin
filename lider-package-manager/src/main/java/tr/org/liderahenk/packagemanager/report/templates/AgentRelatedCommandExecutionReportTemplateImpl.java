package tr.org.liderahenk.packagemanager.report.templates;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import tr.org.liderahenk.lider.core.api.persistence.entities.IReportTemplate;
import tr.org.liderahenk.lider.core.api.persistence.entities.IReportTemplateColumn;
import tr.org.liderahenk.lider.core.api.persistence.entities.IReportTemplateParameter;
import tr.org.liderahenk.lider.core.api.persistence.enums.ParameterType;
import tr.org.liderahenk.lider.core.api.plugin.BaseReportTemplate;

/**
 * Default report template for executed tasks.
 * 
 * @author <a href="mailto:cemre.alpsoy@agem.com.tr">Cemre Alpsoy</a>
 *
 */
public class AgentRelatedCommandExecutionReportTemplateImpl extends BaseReportTemplate {

	private static final long serialVersionUID = -8026043224671892836L;
	@Override
	public String getName() {
		return "Kişi Bazlı İşletilmiş Komut Raporu";
	}

	@Override
	public String getDescription() {
		return "İşletilmiş olan komutların kişi bazlı detaylı raporu";
	}

	@Override
	public String getQuery() {
		return "SELECT DISTINCT c.agentId, c.command, SUM(c.processTime), COUNT(c.command) "
				+ "FROM CommandExecutionStatistics c "
				+ "WHERE c.isActive = '0' AND c.command = :command GROUP BY c.agentId, c.command ORDER BY c.agentId, c.command";
	}

	@SuppressWarnings("serial")
	@Override
	public Set<? extends IReportTemplateParameter> getTemplateParams() {
		Set<IReportTemplateParameter> params = new HashSet<IReportTemplateParameter>();
		
		params.add(new IReportTemplateParameter() {

			@Override
			public Date getCreateDate() {
				return new Date();
			}

			@Override
			public boolean isMandatory() {
				return false;
			}

			@Override
			public String getDefaultValue() {
				return null;
			}

			@Override
			public ParameterType getType() {
				return ParameterType.STRING;
			}

			@Override
			public IReportTemplate getTemplate() {
				return getSelf();
			}

			@Override
			public String getLabel() {
				return "Komut";
			}

			@Override
			public String getKey() {
				return "command";
			}

			@Override
			public Long getId() {
				return null;
			}
		});
		return params;
	}

	@SuppressWarnings("serial")
	@Override
	public Set<? extends IReportTemplateColumn> getTemplateColumns() {
		Set<IReportTemplateColumn> columns = new HashSet<IReportTemplateColumn>();
		columns.add(new IReportTemplateColumn() {
			@Override
			public Date getCreateDate() {
				return new Date();
			}

			@Override
			public IReportTemplate getTemplate() {
				return getSelf();
			}

			@Override
			public String getName() {
				return "Ajan Id";
			}

			@Override
			public Long getId() {
				return null;
			}

			@Override
			public Integer getColumnOrder() {
				return 1;
			}
		});
		columns.add(new IReportTemplateColumn() {
			@Override
			public Date getCreateDate() {
				return new Date();
			}

			@Override
			public IReportTemplate getTemplate() {
				return getSelf();
			}

			@Override
			public String getName() {
				return "Komut";
			}

			@Override
			public Long getId() {
				return null;
			}

			@Override
			public Integer getColumnOrder() {
				return 2;
			}
		});
		columns.add(new IReportTemplateColumn() {
			@Override
			public Date getCreateDate() {
				return new Date();
			}

			@Override
			public IReportTemplate getTemplate() {
				return getSelf();
			}

			@Override
			public String getName() {
				return "Toplam İşletme Zamanı";
			}

			@Override
			public Long getId() {
				return null;
			}

			@Override
			public Integer getColumnOrder() {
				return 3;
			}
		});
		columns.add(new IReportTemplateColumn() {
			@Override
			public Date getCreateDate() {
				return new Date();
			}

			@Override
			public IReportTemplate getTemplate() {
				return getSelf();
			}

			@Override
			public String getName() {
				return "İşletilme Sayısı";
			}

			@Override
			public Long getId() {
				return null;
			}

			@Override
			public Integer getColumnOrder() {
				return 4;
			}
		});
		return columns;
	}

	protected AgentRelatedCommandExecutionReportTemplateImpl getSelf() {
		return this;
	}

	@Override
	public String getCode() {
		return "AGENT-RELATED-COMMAND-EXECUTION-REPORT";
	}

}
