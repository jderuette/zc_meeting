package org.zeroclick.meeting.client.event;

import java.util.Set;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.AbstractSplitBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.common.ui.form.IPageForm;
import org.zeroclick.meeting.client.event.EventInvitedPageForm.MainBox.CloseButton;
import org.zeroclick.meeting.client.event.EventInvitedPageForm.MainBox.SplitHorizontalField.EventDetailBox;
import org.zeroclick.meeting.client.event.EventInvitedPageForm.MainBox.SplitHorizontalField.EventDetailBox.LeftDetailBox;
import org.zeroclick.meeting.client.event.EventInvitedPageForm.MainBox.SplitHorizontalField.EventDetailBox.LeftDetailBox.DescriptionField;
import org.zeroclick.meeting.client.event.EventInvitedPageForm.MainBox.SplitHorizontalField.EventDetailBox.LeftDetailBox.MaximalStartDateField;
import org.zeroclick.meeting.client.event.EventInvitedPageForm.MainBox.SplitHorizontalField.EventDetailBox.LeftDetailBox.MinimalStartDateField;
import org.zeroclick.meeting.client.event.EventInvitedPageForm.MainBox.SplitHorizontalField.EventDetailBox.ParticipantsField;
import org.zeroclick.meeting.client.event.EventInvitedPageForm.MainBox.SplitHorizontalField.EventsBox;
import org.zeroclick.meeting.client.event.EventInvitedPageForm.MainBox.SplitHorizontalField.EventsBox.AskedEventTableField;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.EventInvitedPageFormData;
import org.zeroclick.meeting.shared.event.EventStateCodeType;
import org.zeroclick.meeting.shared.event.EventTablePageData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.involevment.EventRoleCodeType;
import org.zeroclick.meeting.shared.event.involevment.IInvolvementService;
import org.zeroclick.meeting.shared.event.involevment.InvolvementFormData;
import org.zeroclick.meeting.shared.event.involevment.InvolvementTablePageData;
import org.zeroclick.meeting.shared.security.IAccessControlServiceHelper;
import org.zeroclick.ui.form.columns.event.AbstractCreatedDateColumn;
import org.zeroclick.ui.form.columns.event.AbstractEventIdColumn;
import org.zeroclick.ui.form.columns.event.AbstractMaximalStartDateColumn;
import org.zeroclick.ui.form.columns.event.AbstractMinimalStartDateColumn;
import org.zeroclick.ui.form.columns.event.AbstractSlotColumn;
import org.zeroclick.ui.form.columns.event.AbstractStateColumn;
import org.zeroclick.ui.form.columns.event.AbstractSubjectColumn;
import org.zeroclick.ui.form.columns.event.AbstractVenueColumn;
import org.zeroclick.ui.form.columns.userid.AbstractUserIdColumn;
import org.zeroclick.ui.form.columns.zoneddatecolumn.AbstractZonedDateColumn;
import org.zeroclick.ui.form.fields.event.AbstractMaximalStartDateField;
import org.zeroclick.ui.form.fields.event.AbstractMinimalStartDateField;

@FormData(value = EventInvitedPageFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class EventInvitedPageForm extends AbstractForm implements IPageForm {

	private Integer nbEventToProcess = 0;

	protected Integer getNbEventToProcess() {
		return this.nbEventToProcess;
	}

	protected void setNbEventToProcess(final Integer nbEventToProcess) {
		this.nbEventToProcess = nbEventToProcess;
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.events",
				null == this.getNbEventToProcess() ? "0" : this.getNbEventToProcess().toString());
	}

	@Override
	protected boolean getConfiguredAskIfNeedSave() {
		return false;
	}

	@Override
	public void startPageForm() {
		this.startInternal(new PageFormHandler());
	}

	public AskedEventTableField getAskedEventTableField() {
		return this.getFieldByClass(AskedEventTableField.class);
	}

	public EventsBox getEventsBox() {
		return this.getFieldByClass(EventsBox.class);
	}

	public EventDetailBox getEventDetailBox() {
		return this.getFieldByClass(EventDetailBox.class);
	}

	public DescriptionField getDescriptionField() {
		return this.getFieldByClass(DescriptionField.class);
	}

	public LeftDetailBox getLeftDetailBox() {
		return this.getFieldByClass(LeftDetailBox.class);
	}

	public MinimalStartDateField getMinimalStartDateField() {
		return this.getFieldByClass(MinimalStartDateField.class);
	}

	public MaximalStartDateField getMaximalStartDateField() {
		return this.getFieldByClass(MaximalStartDateField.class);
	}

	public ParticipantsField getParticipantsField() {
		return this.getFieldByClass(ParticipantsField.class);
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	@Override
	public AbstractCloseButton getCloseButton() {
		return this.getFieldByClass(CloseButton.class);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(1000)
		public class NewEventMenu extends AbstractCreatEventMenu {
		}

		@Order(2000)
		public class SeparatorMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return "";
			}

			@Override
			protected boolean getConfiguredSeparator() {
				return Boolean.TRUE;
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
			}
		}

		@Order(3000)
		public class AcceptEventInvolvementBoxMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.meeting.Accept");
			}

			@Override
			protected void execAction() {
				final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);

				final ChooseDateForm form = new ChooseDateForm();
				form.getEventIdField().setValue(EventInvitedPageForm.this.getAskedEventTableField().getTable()
						.getEventIdColumn().getSelectedValue());
				form.getEventSubjectField().setValue(EventInvitedPageForm.this.getAskedEventTableField().getTable()
						.getSubjectColumn().getSelectedValue());
				form.setForUserId(acsHelper.getZeroClickUserIdOfCurrentSubject());

				form.startAccept();
			}
		}

		@Order(4000)
		public class RefuseEventInvolvmentBoxMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.meeting.refuse");
			}

			@Override
			protected void execAction() {
				final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);

				final RejectEventForm form = new RejectEventForm();
				form.setEventId(EventInvitedPageForm.this.getAskedEventTableField().getTable().getEventIdColumn()
						.getSelectedValue());
				form.setForUserId(acsHelper.getZeroClickUserIdOfCurrentSubject());
				form.startReject(false);
			}
		}

		@Order(1000)
		public class SplitHorizontalField extends AbstractSplitBox {

			@Override
			protected boolean getConfiguredSplitHorizontal() {
				return false;
			}

			@Override
			protected double getConfiguredSplitterPosition() {
				return 0.5;
			}

			@Order(1000)
			public class EventsBox extends AbstractGroupBox {
				@Override
				protected String getConfiguredLabel() {
					return null;
				}

				@Override
				protected boolean getConfiguredLabelVisible() {
					return false;
				}

				@Order(1000)
				public class AskedEventTableField extends AbstractTableField<AskedEventTableField.Table> {
					@Override
					protected String getConfiguredLabel() {
						return null;
					}

					@Override
					protected boolean getConfiguredLabelVisible() {
						return false;
					}

					@Override
					protected int getConfiguredGridH() {
						return 4;
					}

					@Order(1000)
					public class RefreshEventsKeyStroke extends AbstractKeyStroke {
						@Override
						protected String getConfiguredKeyStroke() {
							return IKeyStroke.F5;
						}

						@Override
						protected void execAction() {
							AskedEventTableField.this.getTable().refresh();
						}
					}

					public class Table extends AbstractTable {
						@Override
						protected boolean getConfiguredAutoResizeColumns() {
							return true;
						}

						@Override
						protected void execInitTable() {
							this.loadData();
						}

						private void loadData() {
							final SearchFilter filter = new SearchFilter();
							final EventFormData eventFormDataFilter = new EventFormData();
							eventFormDataFilter.getState().setValue(EventStateCodeType.WaitingCode.ID);
							filter.setFormData(eventFormDataFilter);

							final EventTablePageData pageData = BEANS.get(IEventService.class)
									.getEventTableData(filter);
							this.importFromTableBeanData(pageData);
						}

						public void refresh() {
							this.loadData();
							this.refreshLinkedData(this.getSelectedRow());
						}

						@Override
						protected void execRowAction(final ITableRow row) {
							this.refreshLinkedData(row);

							super.execRowAction(row);
						}

						private void refreshLinkedData(final ITableRow row) {
							if (null != row) {
								final Long eventId = this.getEventIdColumn().getValue(row.getRowIndex());
								EventInvitedPageForm.this.getEventDetailBox().reload(eventId);
							}
						}

						@Override
						protected void execRowClick(final ITableRow row, final MouseButton mouseButton) {
							if (mouseButton == MouseButton.Left) {
								this.refreshLinkedData(row);
							}
							super.execRowClick(row, mouseButton);
						}

						public MinimalStartDateColumn getMinimalStartDateColumn() {
							return this.getColumnSet().getColumnByClass(MinimalStartDateColumn.class);
						}

						public MaximalStartDateColumn getMaximalStartDateColumn() {
							return this.getColumnSet().getColumnByClass(MaximalStartDateColumn.class);
						}

						public StateColumn getStateColumn() {
							return this.getColumnSet().getColumnByClass(StateColumn.class);
						}

						public DurationColumn getDurationColumn() {
							return this.getColumnSet().getColumnByClass(DurationColumn.class);
						}

						public EventIdColumn getEventIdColumn() {
							return this.getColumnSet().getColumnByClass(EventIdColumn.class);
						}

						public VenueColumn getVenueColumn() {
							return this.getColumnSet().getColumnByClass(VenueColumn.class);
						}

						public SlotColumn getSlotColumn() {
							return this.getColumnSet().getColumnByClass(SlotColumn.class);
						}

						public SubjectColumn getSubjectColumn() {
							return this.getColumnSet().getColumnByClass(SubjectColumn.class);
						}

						public AbstractCreatedDateColumn getCreatedDateColumn() {
							return this.getColumnSet().getColumnByClass(CreatedDateColumn.class);
						}

						@Order(1000)
						public class EventIdColumn extends AbstractEventIdColumn {
						}

						@Order(2000)
						public class CreatedDateColumn extends AbstractCreatedDateColumn {
						}

						@Order(3000)
						public class SubjectColumn extends AbstractSubjectColumn {
						}

						@Order(4000)
						public class SlotColumn extends AbstractSlotColumn {
						}

						@Order(5000)
						public class DurationColumn extends AbstractDurationColumn {
						}

						@Order(6000)
						public class VenueColumn extends AbstractVenueColumn {
						}

						@Order(7000)
						public class StartDateColumn extends AbstractZonedDateColumn {
							@Override
							protected String getConfiguredHeaderText() {
								return TEXTS.get("zc.meeting.start");
							}
						}

						@Order(8000)
						public class EndDateColumn extends AbstractZonedDateColumn {
							@Override
							protected String getConfiguredHeaderText() {
								return TEXTS.get("zc.meeting.end");
							}
						}

						@Order(8000)
						public class MinimalStartDateColumn extends AbstractMinimalStartDateColumn {
							@Override
							protected boolean getConfiguredVisible() {
								return false;
							}
						}

						@Order(9000)
						public class MaximalStartDateColumn extends AbstractMaximalStartDateColumn {
							@Override
							protected boolean getConfiguredVisible() {
								return false;
							}
						}

						@Order(9000)
						public class StateColumn extends AbstractStateColumn {
						}
					}
				} // end of event Table
			}

			@Order(2000)
			public class EventDetailBox extends AbstractGroupBox {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.event.details");
				}

				@Override
				protected int getConfiguredGridColumnCount() {
					return 3;
				}

				public void reload(final Long eventId) {
					final IEventService eventService = BEANS.get(IEventService.class);

					EventInvitedPageForm.this.getDescriptionField().setValue(eventService.loadDescription(eventId));
					EventInvitedPageForm.this.getParticipantsField().getTable().loadData(eventId);

					// TODO Djer avoid dependencies on Table structure
					final int rowIndex = EventInvitedPageForm.this.getAskedEventTableField().getTable().getSelectedRow()
							.getRowIndex();

					EventInvitedPageForm.this.getMinimalStartDateField().setValue(EventInvitedPageForm.this
							.getAskedEventTableField().getTable().getMinimalStartDateColumn().getValue(rowIndex));
					EventInvitedPageForm.this.getMaximalStartDateField().setValue(EventInvitedPageForm.this
							.getAskedEventTableField().getTable().getMaximalStartDateColumn().getValue(rowIndex));
				}

				@Order(1000)
				public class LeftDetailBox extends AbstractGroupBox {
					@Override
					protected String getConfiguredLabel() {
						return null;
					}

					@Override
					protected boolean getConfiguredLabelVisible() {
						return false;
					}

					@Override
					protected int getConfiguredGridColumnCount() {
						return 1;
					}

					@Override
					protected int getConfiguredGridW() {
						return 1;
					}

					@Order(1000)
					public class MinimalStartDateField extends AbstractMinimalStartDateField {
						@Override
						protected boolean getConfiguredEnabled() {
							return false;
						}
					}

					@Order(2000)
					public class MaximalStartDateField extends AbstractMaximalStartDateField {
						@Override
						protected boolean getConfiguredEnabled() {
							return false;
						}
					}

					@Order(3000)
					public class DescriptionField extends AbstractHtmlField {
						@Override
						protected String getConfiguredLabel() {
							return TEXTS.get("zc.meeting.event.description");
						}

						@Override
						protected boolean getConfiguredScrollBarEnabled() {
							return Boolean.TRUE;
						}

						@Override
						protected int getConfiguredLabelPosition() {
							return LABEL_POSITION_TOP;
						}

						@Override
						protected int getConfiguredGridH() {
							return 3;
						}
					}

				}// end of left detail box

				@Order(2000)
				public class ParticipantsField extends AbstractTableField<ParticipantsField.Table> {

					@Override
					protected String getConfiguredLabel() {
						return TEXTS.get("zc.meeting.event.guests");
					}

					@Override
					protected int getConfiguredGridW() {
						return 2;
					}

					@Override
					protected int getConfiguredLabelPosition() {
						return LABEL_POSITION_TOP;
					}

					@Override
					protected int getConfiguredGridH() {
						return 3;
					}

					public class Table extends AbstractTable {
						@Override
						protected boolean getConfiguredAutoResizeColumns() {
							return true;
						}

						public void loadData(final Long eventId) {
							final SearchFilter filter = new SearchFilter();

							final InvolvementFormData formData = new InvolvementFormData();
							formData.getEventId().setValue(eventId);
							filter.setFormData(formData);

							final InvolvementTablePageData pageData = BEANS.get(IInvolvementService.class)
									.getInvolevmentTableData(filter);
							this.importFromTableBeanData(pageData);
						}

						public ExternalEventIdColumn getExternalEventIdColumn() {
							return this.getColumnSet().getColumnByClass(ExternalEventIdColumn.class);
						}

						public InvitedByColumn getInvitedByColumn() {
							return this.getColumnSet().getColumnByClass(InvitedByColumn.class);
						}

						public StateColumn getStateColumn() {
							return this.getColumnSet().getColumnByClass(StateColumn.class);
						}

						public ReasonColumn getReasonColumn() {
							return this.getColumnSet().getColumnByClass(ReasonColumn.class);
						}

						public RoleColumn getRoleColumn() {
							return this.getColumnSet().getColumnByClass(RoleColumn.class);
						}

						public UserIdColumn getUserIdColumn() {
							return this.getColumnSet().getColumnByClass(UserIdColumn.class);
						}

						@Order(1000)
						public class UserIdColumn extends AbstractUserIdColumn {
							@Override
							protected int getConfiguredWidth() {
								return 200;
							}
						}

						@Order(2000)
						public class RoleColumn extends AbstractSmartColumn<String> {
							// In User context, so texts are translated
							final EventRoleCodeType involevmenRoleCodes = new EventRoleCodeType();

							@Override
							protected String getConfiguredHeaderText() {
								return TEXTS.get("zc.meeting.event.involevment.participants");
							}

							@Override
							protected Class<? extends ICodeType<?, String>> getConfiguredCodeType() {
								return EventRoleCodeType.class;
							}

							@Override
							protected void execDecorateCell(final Cell cell, final ITableRow row) {
								super.execDecorateCell(cell, row);

								final Object roleValue = cell.getValue();

								if (null != roleValue) {
									final String roleColumnValue = (String) roleValue;

									final ICode<String> currentStateCode = this.involevmenRoleCodes
											.getCode(roleColumnValue);
									if (null != currentStateCode) {
										cell.setIconId(currentStateCode.getIconId());
										cell.setBackgroundColor(currentStateCode.getBackgroundColor());
										cell.setForegroundColor(currentStateCode.getForegroundColor());
										cell.setText(currentStateCode.getText());
									}
								}
							}

							@Override
							protected int getConfiguredWidth() {
								return 150;
							}
						}

						@Order(3000)
						public class StateColumn extends AbstractStateColumn {
							@Override
							protected boolean getConfiguredVisible() {
								return true;
							}
						}

						@Order(4000)
						public class InvitedByColumn extends AbstractUserIdColumn {
							@Override
							protected String getConfiguredHeaderText() {
								return TEXTS.get("zc.meeting.event.involevment.invitedBy");
							}

							@Override
							protected int getConfiguredWidth() {
								return 200;
							}
						}

						@Order(5000)
						public class ReasonColumn extends AbstractStringColumn {
							@Override
							protected String getConfiguredHeaderText() {
								return TEXTS.get("zc.meeting.event.involevment.rejectReason");
							}

							@Override
							protected boolean getConfiguredVisible() {
								return false;
							}

							@Override
							protected int getConfiguredWidth() {
								return 250;
							}
						}

						@Order(6000)
						public class ExternalEventIdColumn extends AbstractStringColumn {
							@Override
							protected String getConfiguredHeaderText() {
								return TEXTS.get("zc.meeting.event.involevment.externalEventId");
							}

							@Override
							protected boolean getConfiguredVisible() {
								return false;
							}

							@Override
							protected int getConfiguredWidth() {
								return 100;
							}
						}
					}// end of Participants Table
				} // end of Guest Field
			}// end detail Box
		}// end horizontal split box

		@Order(100000)
		public class CloseButton extends AbstractCloseButton {
		}
	}

	public class PageFormHandler extends AbstractFormHandler {
		@Override
		protected void execLoad() {
			// TODO Djer
		}
	}
}
