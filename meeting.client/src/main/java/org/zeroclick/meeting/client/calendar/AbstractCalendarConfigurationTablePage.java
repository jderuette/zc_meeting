package org.zeroclick.meeting.client.calendar;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData;

@Data(AbstractCalendarConfigurationTablePageData.class)
public abstract class AbstractCalendarConfigurationTablePage<T extends AbstractCalendarConfigurationTablePage<T>.Table>
		extends AbstractPageWithTable<T> {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.calendar.configuration");
	}

	@Override
	protected boolean getConfiguredLeaf() {
		return Boolean.TRUE;
	}

	@Override
	protected String getConfiguredIconId() {
		return Icons.Gear;
	}

	public class Table extends AbstractTable {

		@Override
		protected boolean getConfiguredAutoResizeColumns() {
			return Boolean.TRUE;
		}

		@Override
		protected boolean getConfiguredMultilineText() {
			return Boolean.TRUE;
		}

		public AbstractCalendarConfigurationTablePage<?>.Table.ProcessNotRegistredOnEventColumn getProcessNotRegistredOnEventColumn() {
			return this.getColumnSet().getColumnByClass(ProcessNotRegistredOnEventColumn.class);
		}

		public AbstractCalendarConfigurationTablePage<?>.Table.UserIdColumn getUserIdColumn() {
			return this.getColumnSet().getColumnByClass(UserIdColumn.class);
		}

		public AbstractCalendarConfigurationTablePage<?>.Table.OAuthCredentialIdColumn getOAuthCredentialIdColumn() {
			return this.getColumnSet().getColumnByClass(OAuthCredentialIdColumn.class);
		}

		public AbstractCalendarConfigurationTablePage<?>.Table.ProcessFreeEventColumn getProcessFreeEventColumn() {
			return this.getColumnSet().getColumnByClass(ProcessFreeEventColumn.class);
		}

		public AbstractCalendarConfigurationTablePage<?>.Table.ProcessFullDayEventColumn getProcessFullDayEventColumn() {
			return this.getColumnSet().getColumnByClass(ProcessFullDayEventColumn.class);
		}

		public AbstractCalendarConfigurationTablePage<?>.Table.ExternalIdColumn getExternalIdColumn() {
			return this.getColumnSet().getColumnByClass(ExternalIdColumn.class);
		}

		public AbstractCalendarConfigurationTablePage<?>.Table.CalendarConfigurationIdColumn getCalendarConfigurationIdColumn() {
			return this.getColumnSet().getColumnByClass(CalendarConfigurationIdColumn.class);
		}

		@Order(1000)
		public class CalendarConfigurationIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.calendar.id");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredWidth() {
				return 50;
			}
		}

		@Order(2000)
		public class ExternalIdColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.calendar.externalId");
			}

			@Override
			protected int getConfiguredWidth() {
				return 150;
			}
		}

		@Order(3000)
		public class ProcessFullDayEventColumn extends AbstractBooleanColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.calendar.processFullDayEvent");
			}

			@Override
			protected int getConfiguredWidth() {
				return 150;
			}
		}

		@Order(4000)
		public class ProcessFreeEventColumn extends AbstractBooleanColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.calendar.processFreeEvent");
			}

			@Override
			protected int getConfiguredWidth() {
				return 150;
			}
		}

		@Order(5000)
		public class ProcessNotRegistredOnEventColumn extends AbstractBooleanColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.calendar.processNotRegisteredOnEvent");
			}

			@Override
			protected int getConfiguredWidth() {
				return 150;
			}
		}

		@Order(6000)
		public class OAuthCredentialIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.calendar.OAuthCredentialId");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

		@Order(7000)
		public class UserIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.calendar.userId");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}
	}
}
