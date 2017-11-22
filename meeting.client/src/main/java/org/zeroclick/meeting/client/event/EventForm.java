package org.zeroclick.meeting.client.event;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractProposalColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.common.email.IMailSender;
import org.zeroclick.common.email.MailException;
import org.zeroclick.comon.text.TextsHelper;
import org.zeroclick.comon.user.AppUserHelper;
import org.zeroclick.configuration.client.user.UserForm;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper.SubscriptionHelperData;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.configuration.shared.venue.VenueLookupCall;
import org.zeroclick.meeting.client.NotificationHelper;
import org.zeroclick.meeting.client.common.DurationLookupCall;
import org.zeroclick.meeting.client.common.EventStateLookupCall;
import org.zeroclick.meeting.client.common.SlotHelper;
import org.zeroclick.meeting.client.common.SlotLookupCall;
import org.zeroclick.meeting.client.event.EventForm.MainBox.CancelButton;
import org.zeroclick.meeting.client.event.EventForm.MainBox.CreatedDateField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.DurationField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.EmailChooserBox;
import org.zeroclick.meeting.client.event.EventForm.MainBox.EmailChooserBox.MultipleEmailBox;
import org.zeroclick.meeting.client.event.EventForm.MainBox.EmailChooserBox.MultipleEmailBox.EmailsField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.EmailChooserBox.OneEmailBox;
import org.zeroclick.meeting.client.event.EventForm.MainBox.EmailChooserBox.OneEmailBox.EmailField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.EndDateField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.GuestIdField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.OkButton;
import org.zeroclick.meeting.client.event.EventForm.MainBox.OrganizerEmailField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.OrganizerField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.PeriodeBox;
import org.zeroclick.meeting.client.event.EventForm.MainBox.PeriodeBox.MaximalStartDateField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.PeriodeBox.MinimalStartDateField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.PeriodeBox.SlotSequenceBox;
import org.zeroclick.meeting.client.event.EventForm.MainBox.PeriodeBox.SlotSequenceBox.AdvancePeriodButton;
import org.zeroclick.meeting.client.event.EventForm.MainBox.PeriodeBox.SlotSequenceBox.SlotField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.ReasonField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.StartDateField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.StateField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.SubjectField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.VenueField;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.KnowEmailLookupCall;
import org.zeroclick.meeting.shared.event.UpdateEventPermission;
import org.zeroclick.meeting.shared.security.AccessControlService;

@FormData(value = EventFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class EventForm extends AbstractForm {

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	// represents the Event primary key
	private Long eventId;

	private String externalIdOrganizer;
	private String externalIdRecipient;
	/** to know who performed the last action (for notification) **/
	private Long lastModifier;
	private String previousState;

	private EventMessageHelper eventMessageHelper;

	@FormData
	public Long getEventId() {
		return this.eventId;
	}

	@FormData
	public void setEventId(final Long eventId) {
		this.eventId = eventId;
	}

	@FormData
	public String getExternalIdOrganizer() {
		return this.externalIdOrganizer;
	}

	@FormData
	public void setExternalIdOrganizer(final String eventExternalIdOrganizer) {
		this.externalIdOrganizer = eventExternalIdOrganizer;
	}

	@FormData
	public String getExternalIdRecipient() {
		return this.externalIdRecipient;
	}

	@FormData
	public void setExternalIdRecipient(final String externalIdRecipient) {
		this.externalIdRecipient = externalIdRecipient;
	}

	@FormData
	public Long getLastModifier() {
		return this.lastModifier;
	}

	@FormData
	public void setLastModifier(final Long lastModifier) {
		this.lastModifier = lastModifier;
	}

	@FormData
	public String getPreviousState() {
		return this.previousState;
	}

	@FormData
	public void setPreviousState(final String previousState) {
		this.previousState = previousState;
	}

	@Override
	public Object computeExclusiveKey() {
		return this.getEventId();
	}

	@Override
	protected int getConfiguredDisplayHint() {
		return IForm.DISPLAY_HINT_VIEW;
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.event");
	}

	@Override
	protected void execInitForm() {
		final IUserService userService = BEANS.get(IUserService.class);
		final UserFormData userDetails = userService.getCurrentUserDetails();
		this.getOrganizerEmailField().setValue(userDetails.getEmail().getValue());
	}

	public void startModify() {
		this.startInternalExclusive(new ModifyHandler());
	}

	public void startNew() {
		this.startInternal(new NewHandler());
	}

	public Integer getNbEmails() {
		Integer nbEmails = 0;
		if (EventForm.this.getEmailsField().getTable().getRowCount() > 0) {
			nbEmails = EventForm.this.getEmailsField().getTable().getRowCount();
		} else {
			if (null != EventForm.this.getEmailField().getValue()
					&& !EventForm.this.getEmailField().getValue().isEmpty()) {
				nbEmails = 1;
			}
		}
		return nbEmails;
	}

	protected void updateNbEmail() {
		EventForm.this.getMultipleEmailBox().updateLabel();
		EventForm.this.getOkButton().updateLabel();
	}

	public void startAccept() {
		this.startInternal(new AcceptHandler());
	}

	public CancelButton getCancelButton() {
		return this.getFieldByClass(CancelButton.class);
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public EmailField getEmailField() {
		return this.getFieldByClass(EmailField.class);
	}

	public SlotField getSlotField() {
		return this.getFieldByClass(SlotField.class);
	}

	public DurationField getDurationField() {
		return this.getFieldByClass(DurationField.class);
	}

	public StateField getStateField() {
		return this.getFieldByClass(StateField.class);
	}

	public StartDateField getStartDateField() {
		return this.getFieldByClass(StartDateField.class);
	}

	public EndDateField getMyDateField() {
		return this.getFieldByClass(EndDateField.class);
	}

	public OrganizerField getOrganizerField() {
		return this.getFieldByClass(OrganizerField.class);
	}

	public OrganizerEmailField getOrganizerEmailField() {
		return this.getFieldByClass(OrganizerEmailField.class);
	}

	public GuestIdField getGuestIdField() {
		return this.getFieldByClass(GuestIdField.class);
	}

	public SubjectField getSubjectField() {
		return this.getFieldByClass(SubjectField.class);
	}

	public ReasonField getReasonField() {
		return this.getFieldByClass(ReasonField.class);
	}

	public VenueField getVenueField() {
		return this.getFieldByClass(VenueField.class);
	}

	public CreatedDateField getCreatedDateField() {
		return this.getFieldByClass(CreatedDateField.class);
	}

	public EmailsField getEmailsField() {
		return this.getFieldByClass(EmailsField.class);
	}

	public EmailChooserBox getEmailChooserBox() {
		return this.getFieldByClass(EmailChooserBox.class);
	}

	public OneEmailBox getOneEmailBox() {
		return this.getFieldByClass(OneEmailBox.class);
	}

	public MultipleEmailBox getMultipleEmailBox() {
		return this.getFieldByClass(MultipleEmailBox.class);
	}

	public PeriodeBox getPeriodeBox() {
		return this.getFieldByClass(PeriodeBox.class);
	}

	public SlotSequenceBox getSlotBox() {
		return this.getFieldByClass(SlotSequenceBox.class);
	}

	public AdvancePeriodButton getAdvancePeriodButton() {
		return this.getFieldByClass(AdvancePeriodButton.class);
	}

	public MinimalStartDateField getMinimalStartDateField() {
		return this.getFieldByClass(MinimalStartDateField.class);
	}

	public MaximalStartDateField getMaximalStartDateField() {
		return this.getFieldByClass(MaximalStartDateField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Override
		protected int getConfiguredGridW() {
			return 1;
		}

		@Override
		protected int getConfiguredGridColumnCount() {
			return 1;
		}

		@Order(1000)
		public class OrganizerField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.host");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}
		}

		@Order(2000)
		public class OrganizerEmailField extends org.zeroclick.ui.form.fields.emailfield.EmailField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.host");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}

		}

		@Order(3000)
		public class SubjectField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.subject");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 256;
			}

			@Override
			protected String execValidateValue(final String rawValue) {
				if (null != rawValue && rawValue.length() > 150) {
					throw new VetoException(TEXTS.get("zc.meeting.event.subject.tooLong"));
				}
				return rawValue;
			}
		}

		@Order(4000)
		public class EmailChooserBox extends AbstractTabBox {

			@Order(1000)
			public class OneEmailBox extends AbstractGroupBox {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.OneEmail");
				}

				@Override
				protected boolean getConfiguredBorderVisible() {
					return Boolean.TRUE;
				}

				@Override
				protected String getConfiguredBorderDecoration() {
					return BORDER_DECORATION_LINE;
				}

				@Override
				protected boolean getConfiguredExpandable() {
					return Boolean.TRUE;
				}

				@Order(1000)
				public class EmailField extends AbstractProposalField<String> {
					@Override
					protected String getConfiguredLabel() {
						return TEXTS.get("zc.meeting.attendeeEmail");
					}

					@Override
					protected boolean getConfiguredMandatory() {
						return Boolean.TRUE;
					}

					@Override
					protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
						return KnowEmailLookupCall.class;
					}

					@Override
					protected void execChangedValue() {
						// clear the attendee "list"
						EventForm.this.getEmailsField().getTable().deleteAllRows();
						EventForm.this.updateNbEmail();
					}

					// some validation logic done on save due to limitation in
					// proposalField (No maxLeng, execvalidate final
				}
			}

			@Order(2000)
			public class MultipleEmailBox extends AbstractGroupBox {

				public void updateLabel() {
					this.setLabel(this.buildLabel());
				}

				private String buildLabel() {
					final Integer nbEmails = EventForm.this.getEmailsField().getTable().getRowCount();
					return this.buildLabel(nbEmails);
				}

				private String buildLabel(final Integer nbAttendee) {
					String text = TEXTS.get("zc.meeting.multipleEmails");
					if (nbAttendee > 0) {
						text = TEXTS.get("zc.meeting.multipleEmails") + " (" + nbAttendee + ")";
					}
					return text;
				}

				@Override
				protected String getConfiguredLabel() {
					return this.buildLabel(0);
				}

				@Override
				protected String getConfiguredBorderDecoration() {
					return BORDER_DECORATION_LINE;
				}

				@Override
				protected boolean getConfiguredBorderVisible() {
					return Boolean.TRUE;
				}

				@Override
				protected boolean getConfiguredExpandable() {
					return Boolean.TRUE;
				}

				@Order(1100)
				public class EmailsField extends AbstractTableField<EmailsField.Table> {

					@Override
					protected String getConfiguredLabel() {
						return TEXTS.get("zc.meeting.attendeeEmail");
					}

					@Override
					protected boolean getConfiguredLabelVisible() {
						return Boolean.FALSE;
					}

					@Override
					protected int getConfiguredGridH() {
						return 4;
					}

					public class Table extends AbstractTable {

						@Override
						protected boolean getConfiguredTableStatusVisible() {
							return Boolean.FALSE;
						}

						@Override
						protected boolean getConfiguredHeaderEnabled() {
							return Boolean.FALSE;
						}

						@Override
						protected void execRowAction(final ITableRow row) {
							final EditEmailMenu editEmailMenu = new EditEmailMenu();
							editEmailMenu.doAction();
						}

						@Order(1000)
						public class AddEmailMenu extends AbstractMenu {
							@Override
							protected String getConfiguredText() {
								return TEXTS.get("zc.meeting.attendeeEmail.add");
							}

							@Override
							protected String getConfiguredIconId() {
								return Icons.Plus;
							}

							@Override
							protected Set<? extends IMenuType> getConfiguredMenuTypes() {
								return CollectionUtility.hashSet(TableMenuType.SingleSelection,
										TableMenuType.EmptySpace);
							}

							@Override
							protected String getConfiguredKeyStroke() {
								return combineKeyStrokes(IKeyStroke.SHIFT, "n");
							}

							@Override
							protected void execAction() {
								final ITableRow row = Table.this.createRow();
								Table.this.addRow(row);
								row.getCellForUpdate(Table.this.getEmailColumn()).setEditable(Boolean.TRUE);
								EmailsField.this.getTable().selectLastRow();
								EmailsField.this.getTable().scrollToSelection();
								// clear the "one Email" field
								EventForm.this.getEmailField().setMandatory(Boolean.FALSE);
								EventForm.this.getEmailField().resetValue();

							}
						}

						@Order(1500)
						public class ImportEmailsMenu extends AbstractMenu {
							@Override
							protected String getConfiguredText() {
								return TEXTS.get("zc.meeting.attendeeEmail.import");
							}

							@Override
							protected Set<? extends IMenuType> getConfiguredMenuTypes() {
								return CollectionUtility.hashSet(TableMenuType.SingleSelection,
										TableMenuType.EmptySpace);
							}

							@Override
							protected String getConfiguredIconId() {
								return Icons.List;
							}

							@Override
							protected String getConfiguredKeyStroke() {
								return combineKeyStrokes(IKeyStroke.SHIFT, "i");
							}

							@Override
							protected void execAction() {
								final ImportEmailsForm form = new ImportEmailsForm();
								form.getValueSeparatorField().setValue(",");

								form.startNew();

								form.waitFor();

								if (form.isFormStored()) {
									if (form.getImportedEmailPreviewField().getTable().getRowCount() > 0) {
										for (final ITableRow importedRow : form.getImportedEmailPreviewField()
												.getTable().getRows()) {
											final String importedEmail = (String) importedRow
													.getCell(form.getImportedEmailPreviewField().getTable()
															.getEmailsColumn().getColumnIndex())
													.getValue();
											if (!EventForm.this.existEmail(importedEmail)) {
												final ITableRow newRow = EventForm.this.getEmailsField().getTable()
														.createRow();
												final Cell cell = new Cell();
												cell.setValue(importedEmail);
												newRow.setCell(Table.this.getEmailColumn().getColumnIndex(), cell);
												EventForm.this.getEmailsField().getTable().addRow(newRow);
											}
										}
									}
									// when done in "mass" emailsFiled.execValue
									// don't count the "current" row
									EventForm.this.updateNbEmail();
								}
							}
						}

						@Order(2000)
						public class EditEmailMenu extends AbstractMenu {
							@Override
							protected String getConfiguredText() {
								return TEXTS.get("zc.meeting.attendeeEmail.edit");
							}

							@Override
							protected Set<? extends IMenuType> getConfiguredMenuTypes() {
								return CollectionUtility.hashSet(TableMenuType.SingleSelection);
							}

							@Override
							protected String getConfiguredIconId() {
								return Icons.Pencil;
							}

							@Override
							protected String getConfiguredKeyStroke() {
								return combineKeyStrokes(IKeyStroke.SHIFT, "i");
							}

							@Override
							protected void execAction() {
								EmailsField.this.getTable().getSelectedRow()
										.getCellForUpdate(Table.this.getEmailColumn()).setEditable(Boolean.TRUE);
							}
						}

						@Order(3000)
						public class RemoveEmailMenu extends AbstractMenu {
							@Override
							protected String getConfiguredText() {
								return TEXTS.get("zc.meeting.attendeeEmail.remove");
							}

							@Override
							protected Set<? extends IMenuType> getConfiguredMenuTypes() {
								return CollectionUtility.hashSet(TableMenuType.SingleSelection,
										TableMenuType.MultiSelection);
							}

							@Override
							protected String getConfiguredIconId() {
								return Icons.Remove;
							}

							@Override
							protected String getConfiguredKeyStroke() {
								return combineKeyStrokes(IKeyStroke.SHIFT, IKeyStroke.DELETE);
							}

							@Override
							protected void execAction() {
								final List<ITableRow> rows = Table.this.getSelectedRows();
								for (final ITableRow row : rows) {
									row.delete();
								}

								if (Table.this.getRowCount() == 0) {
									EventForm.this.getEmailField().setMandatory(Boolean.TRUE);
								}
								EventForm.this.updateNbEmail();
							}
						}

						public EmailColumn getEmailColumn() {
							return this.getColumnSet().getColumnByClass(EmailColumn.class);
						}

						@Order(1000)
						public class EmailColumn extends AbstractProposalColumn<String> {
							@Override
							protected String getConfiguredHeaderText() {
								return TEXTS.get("zc.meeting.attendeeEmail");
							}

							@Override
							protected boolean getConfiguredMandatory() {
								return Boolean.TRUE;
							}

							@Override
							protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
								return KnowEmailLookupCall.class;
							}

							@Override
							protected String execValidateValue(final ITableRow row, final String rawValue) {
								String validateRawValue = rawValue;
								if (null != validateRawValue && !validateRawValue.isEmpty()) {
									validateRawValue = rawValue.toLowerCase();
									EventForm.this.checkAttendeeEmail(validateRawValue);
									this.checkEmailAlreadyAttendee(validateRawValue);
									// if not veto thrown, continue
									EventForm.this.updateNbEmail();
									row.getCellForUpdate(Table.this.getEmailColumn()).setEditable(Boolean.FALSE);
								} else {
									row.getCellForUpdate(Table.this.getEmailColumn()).setEditable(Boolean.TRUE);
								}
								return validateRawValue;
							}

							private void checkEmailAlreadyAttendee(final String rawValue) {
								if (null != rawValue && EventForm.this.existEmail(rawValue.toLowerCase())) {
									throw new VetoException(
											TEXTS.get("zc.meeting.attendeeAlreadyInInvitedList", rawValue));
								}
							}

							@Override
							protected int getConfiguredWidth() {
								return 250;
							}
						}
					}
				}
			}
		}

		@Order(5000)
		public class GuestIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.attendeeId");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}
		}

		@Order(6000)
		public class PeriodeBox extends AbstractGroupBox {
			@Override
			protected String getConfiguredLabel() {
				return null;
			}

			@Override
			protected boolean getConfiguredLabelVisible() {
				return Boolean.FALSE;
			}

			@Order(1000)
			public class SlotSequenceBox extends AbstractSequenceBox {
				@Override
				protected String getConfiguredLabel() {
					return null;
				}

				@Override
				protected boolean getConfiguredLabelVisible() {
					return Boolean.FALSE;
				}

				@Override
				protected boolean getConfiguredAutoCheckFromTo() {
					return false;
				}

				@Order(1000)
				public class AdvancePeriodButton extends AbstractButton {
					private static final String ICON_DISABLE = Icons.CaretRight;
					private static final String ICON_ENABLED = Icons.CaretDown;

					@Override
					protected String getConfiguredLabel() {
						return null;
					}

					@Override
					protected int getConfiguredDisplayStyle() {
						return DISPLAY_STYLE_TOGGLE;
					}

					@Override
					protected boolean getConfiguredLabelVisible() {
						return Boolean.FALSE;
					}

					@Override
					protected String getConfiguredIconId() {
						return ICON_DISABLE;
					}

					@Override
					protected boolean getConfiguredProcessButton() {
						return Boolean.FALSE;
					}

					@Override
					protected void execSelectionChanged(final boolean selection) {
						if (this.isSelected()) {
							this.show();
						} else {
							this.hide();
						}
					}

					// @Override
					// protected void execClickAction() {
					// if (this.isSelected()) {
					// this.show();
					// } else {
					// this.hide();
					// }
					// }

					private void show() {
						this.setIconId(ICON_ENABLED);
						EventForm.this.getMinimalStartDateField().setVisible(Boolean.TRUE);
						EventForm.this.getMaximalStartDateField().setVisible(Boolean.TRUE);
					}

					private void hide() {
						this.setIconId(ICON_DISABLE);
						EventForm.this.getMinimalStartDateField().setVisible(Boolean.FALSE);
						EventForm.this.getMinimalStartDateField().setValue(null);
						EventForm.this.getMaximalStartDateField().setVisible(Boolean.FALSE);
						EventForm.this.getMaximalStartDateField().setValue(null);
					}
				}

				@Order(2000)
				public class SlotField extends AbstractSmartField<Integer> {
					@Override
					protected String getConfiguredLabel() {
						return TEXTS.get("zc.meeting.slot");
					}

					@Override
					protected boolean getConfiguredMandatory() {
						return Boolean.TRUE;
					}

					@Override
					protected Class<? extends ILookupCall<Integer>> getConfiguredLookupCall() {
						return SlotLookupCall.class;
					}

					@Override
					protected void execChangedValue() {
						// TODO Djer is their a better way to validate days
						// available in range and display errors on min/max date
						// ?
						EventForm.this.getMinimalStartDateField()
								.execValidateValue(EventForm.this.getMinimalStartDateField().getValue());
						EventForm.this.getMaximalStartDateField()
								.execValidateValue(EventForm.this.getMaximalStartDateField().getValue());
					}
				}
			}

			@Order(3000)
			public class MinimalStartDateField extends AbstractDateField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.minimalStartDate");
				}

				@Override
				protected boolean getConfiguredVisible() {
					return Boolean.FALSE;
				}

				@Override
				protected Date execValidateValue(final Date rawValue) {
					if (null != rawValue && null != EventForm.this.getMaximalStartDateField().getValue()
							&& rawValue.after(EventForm.this.getMaximalStartDateField().getValue())) {
						throw new VetoException(TEXTS.get("zc.meeting.minimalStartDate.afterMaximalDate"));
					}
					PeriodeBox.this.checkAvailableDaysInSlot(rawValue,
							EventForm.this.getMaximalStartDateField().getValue());
					return super.execValidateValue(rawValue);
				}
			}

			@Order(4000)
			public class MaximalStartDateField extends AbstractDateField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.maximalStartDate");
				}

				@Override
				protected Date execValidateValue(final Date rawValue) {
					if (null != rawValue && null != EventForm.this.getMinimalStartDateField().getValue()
							&& rawValue.before(EventForm.this.getMinimalStartDateField().getValue())) {
						throw new VetoException(TEXTS.get("zc.meeting.maximalStartDate.afterMinimalDate"));
					}
					PeriodeBox.this.checkAvailableDaysInSlot(EventForm.this.getMinimalStartDateField().getValue(),
							rawValue);
					return super.execValidateValue(rawValue);
				}

				@Override
				protected boolean getConfiguredVisible() {
					return Boolean.FALSE;
				}
			}

			private void checkAvailableDaysInSlot(final Date minimalDate, final Date maximalDate) {
				final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);
				final Integer slot = EventForm.this.getSlotField().getValue();

				if (null != minimalDate && null != maximalDate && !SlotHelper.get().hasMatchingDays(minimalDate,
						maximalDate, slot, appUserHelper.getCurrentUserId())) {
					throw new VetoException(TEXTS.get("zc.meeting.slot.noSlotWithMinimalAndMaximalDates"));
				}
			}
		}

		@Order(7000)
		public class DurationField extends AbstractSmartField<Integer> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.duration");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected Class<? extends ILookupCall<Integer>> getConfiguredLookupCall() {
				return DurationLookupCall.class;
			}
		}

		@Order(8000)
		public class VenueField extends AbstractProposalField<String> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.venue");
			}

			@Override
			protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
				return VenueLookupCall.class;
			}
		}

		@Order(9000)
		public class StateField extends AbstractSmartField<String> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.state");
			}

			@Override
			protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
				return EventStateLookupCall.class;
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}
		}

		@Order(10000)
		public class StartDateField extends AbstractDateField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.start");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}
		}

		@Order(11000)
		public class EndDateField extends AbstractDateField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.end");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}
		}

		@Order(12000)
		public class ReasonField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.rejectReason");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}
		}

		@Order(13000)
		public class CreatedDateField extends AbstractDateField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.createdDate");
			}

			@Override
			protected boolean getConfiguredHasTime() {
				return Boolean.TRUE;
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}
		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {

			public void updateLabel() {
				final int nbEmail = EventForm.this.getNbEmails();
				this.setLabel(this.buildLabel(nbEmail));
			}

			private String buildLabel(final Integer nbAttendee) {
				return TEXTS.get("zc.meeting.scheduleMeeting", String.valueOf(nbAttendee));
			}

			@Override
			public String getLabel() {
				return this.buildLabel(0);
			}
		}

		@Order(101000)
		public class CancelButton extends AbstractCancelButton {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("Cancel");
			}
		}
	}

	public class ModifyHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IEventService service = BEANS.get(IEventService.class);
			EventFormData formData = new EventFormData();
			EventForm.this.exportFormData(formData);
			formData = service.load(formData);
			EventForm.this.importFormData(formData);

			EventForm.this.setEnabledPermission(new UpdateEventPermission(formData.getEventId()));

			this.getForm().setSubTitle(EventForm.this.calculateSubTitle());
		}

		@Override
		protected boolean execValidate() {
			final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
			notificationHelper.addProcessingNotification("zc.meeting.notification.modifyingEvent");
			return Boolean.TRUE;
		}

		@Override
		protected void execStore() {
			final IEventService service = BEANS.get(IEventService.class);

			final EventFormData formData = new EventFormData();
			EventForm.this.exportFormData(formData);
			service.store(formData);
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IEventService service = BEANS.get(IEventService.class);
			EventFormData formData = new EventFormData();
			EventForm.this.exportFormData(formData);
			formData = service.prepareCreate(formData);
			EventForm.this.importFormData(formData);

			this.getForm().setDisplayHint(IForm.DISPLAY_HINT_DIALOG);

			final SubscriptionHelper subHelper = BEANS.get(SubscriptionHelper.class);

			final SubscriptionHelperData subscriptionData = subHelper.canCreateEvent();
			EventForm.this.setVisibleGranted(subscriptionData.isAccessAllowed());
			EventForm.this.setEnabledGranted(subscriptionData.isAccessAllowed());
		}

		@Override
		protected void execStore() {
			final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
			notificationHelper.addProcessingNotification("zc.meeting.notification.creatingEvent");
			if (null == EventForm.this.getEmailField().getValue()
					|| EventForm.this.getEmailField().getValue().isEmpty()) {
				// mass event creation
				final List<ITableRow> emails = EventForm.this.getEmailsField().getTable().getRows();
				for (final ITableRow row : emails) {
					final String email = row
							.getCell(EventForm.this.getEmailsField().getTable().getEmailColumn().getColumnIndex())
							.getText();
					if (null != email && !email.isEmpty()) {
						this.createEvent(email);
					}
				}
			} else {
				this.createEvent(EventForm.this.getEmailField().getValue());
			}

		}

		private void createEvent(final String eventGuestEmail) {
			Jobs.schedule(new IRunnable() {

				@Override
				public void run() {
					final AccessControlService acs = BEANS.get(AccessControlService.class);
					final IEventService eventService = BEANS.get(IEventService.class);
					final IUserService userService = BEANS.get(IUserService.class);

					final EventFormData formData = new EventFormData();
					EventForm.this.exportFormData(formData);

					EventForm.this.checkAttendeeEmail(eventGuestEmail);
					formData.getEmail().setValue(eventGuestEmail);
					final String eventHeldEmail = formData.getOrganizerEmail().getValue();
					final Long eventGuest = userService.getUserIdByEmail(eventGuestEmail);
					final String meetingSubject = formData.getSubject().getValue();
					final String venue = formData.getVenue().getValue();

					formData.getGuestId().setValue(eventGuest);

					// required *before* save (for sending email)
					formData.setLastModifier(acs.getZeroClickUserIdOfCurrentSubject());

					final String venueKey = TextsHelper.getKeyByText(venue);
					if (null != venueKey) {
						formData.getVenue().setValue(venueKey);
					}

					if (null == eventGuest) {
						final UserForm userForm = new UserForm();
						userForm.autoFillInviteUser(eventGuestEmail, eventHeldEmail, meetingSubject);
						// eventGuest = form.getUserIdField().getValue();
						formData.getGuestId().setValue(userForm.getUserIdField().getValue());
					} else {
						NewHandler.this.sendNewEventEmail(formData);
					}

					eventService.create(formData);
				}
			}, Jobs.newInput()
					.withName("Creating event for {0} By {1}", eventGuestEmail,
							EventForm.this.getOrganizerEmailField().getValue())
					.withRunContext(ClientRunContexts.copyCurrent()).withThreadName("Creating event"));

		}

		private void sendNewEventEmail(final EventFormData formData) {
			final IMailSender mailSender = BEANS.get(IMailSender.class);
			final String recipient = formData.getEmail().getValue();

			final String[] values = EventForm.this.getEventMessageHelper().buildValuesForLocaleMessages(formData,
					formData.getGuestId().getValue());

			final String subject = TextsHelper.get(formData.getGuestId().getValue(),
					"zc.meeting.email.event.new.subject", values);

			final String content = TextsHelper.get(formData.getGuestId().getValue(), "zc.meeting.email.event.new.html",
					values);

			try {
				mailSender.sendEmail(recipient, subject, content);
			} catch (final MailException e) {
				throw new VetoException(TEXTS.get("zc.common.cannotSendEmail"));
			}
		}
	}

	private Boolean existEmail(final String checkedEmail) {
		final List<ITableRow> existingRows = this.getEmailsField().getTable().getRows();

		for (final ITableRow row : existingRows) {
			final Object rowEmail = row.getCell(this.getEmailsField().getTable().getEmailColumn().getColumnIndex())
					.getValue();
			if (null != rowEmail && rowEmail.equals(checkedEmail.toLowerCase())) {
				// earlyBreak
				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}

	public class AcceptHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IEventService service = BEANS.get(IEventService.class);
			EventFormData formData = new EventFormData();
			EventForm.this.exportFormData(formData);
			formData = service.load(formData);
			EventForm.this.importFormData(formData);

			EventForm.this.setEnabledPermission(new UpdateEventPermission(formData.getEventId()));
		}

		@Override
		protected boolean execValidate() {
			final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
			notificationHelper.addProcessingNotification("zc.meeting.notification.acceptingEvent");
			return Boolean.TRUE;
		}

		@Override
		protected void execStore() {
			final IEventService service = BEANS.get(IEventService.class);
			final EventFormData formData = new EventFormData();
			EventForm.this.exportFormData(formData);
			formData.getState().setValue("ACCEPTED");
			service.store(formData);
		}
	}

	private String calculateSubTitle() {
		final DurationLookupCall durationLookupCall = BEANS.get(DurationLookupCall.class);
		final SlotLookupCall slotLookupCall = BEANS.get(SlotLookupCall.class);

		final String durationText = durationLookupCall.getText(this.getDurationField().getValue());
		final String slotText = slotLookupCall.getText(this.getSlotField().getValue());

		return StringUtility.join(" ", durationText, slotText, "\r\n", TEXTS.get("zc.common.with"),
				this.getEmailField().getValue());
	}

	protected void checkAttendeeEmail(final String email) {
		// force to lowerCase
		final String rawValue = email.toLowerCase();

		if (rawValue != null) {
			if (rawValue.equals(EventForm.this.getOrganizerEmailField().getValue())) {
				throw new VetoException(TEXTS.get("zc.meeting.youCannotInviteYourself"));
			}
			this.validateEmail(rawValue);
		}
	}

	protected void validateEmail(final String email) {
		final Integer maxCaracters = 128;
		if (null != email) {
			if (!Pattern.matches(EMAIL_PATTERN, email)) {
				throw new VetoException(TEXTS.get("zc.common.badEmailAddress"));
			}
			if (email.length() > maxCaracters) {
				throw new VetoException(TEXTS.get("zc.common.tooLong", TEXTS.get("zc.meeting.attendeeEmail"),
						String.valueOf(maxCaracters)));
			}
		}
	}

	protected EventMessageHelper getEventMessageHelper() {
		if (null == this.eventMessageHelper) {
			this.eventMessageHelper = BEANS.get(EventMessageHelper.class);
		}
		return this.eventMessageHelper;
	}

}
