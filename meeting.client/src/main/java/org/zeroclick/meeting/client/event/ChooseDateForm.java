package org.zeroclick.meeting.client.event;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.comon.user.AppUserHelper;
import org.zeroclick.meeting.client.event.ChooseDateForm.MainBox.CancelButton;
import org.zeroclick.meeting.client.event.ChooseDateForm.MainBox.EventIdField;
import org.zeroclick.meeting.client.event.ChooseDateForm.MainBox.EventSubjectField;
import org.zeroclick.meeting.client.event.ChooseDateForm.MainBox.OkButton;
import org.zeroclick.meeting.client.event.ChooseDateForm.MainBox.ParticpationInfoBox.ProposedEventDateBox.MessageField;
import org.zeroclick.meeting.client.event.ChooseDateForm.MainBox.ParticpationInfoBox.ProposedEventDateBox.ProposedSequenceBox.ProposedEndEventDateField;
import org.zeroclick.meeting.client.event.ChooseDateForm.MainBox.ParticpationInfoBox.ProposedEventDateBox.ProposedSequenceBox.ProposedStartEventField;
import org.zeroclick.meeting.service.CalendarService;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.security.IAccessControlServiceHelper;
import org.zeroclick.ui.form.fields.datefield.AbstractZonedDateField;

public class ChooseDateForm extends AbstractForm {

	protected EventFormData eventData;

	protected Long forUserId;

	public Long getForUserId() {
		return this.forUserId;
	}

	public void setForUserId(final Long forUserId) {
		this.forUserId = forUserId;
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.event.chooseDate");
	}

	public void startModify() {
		this.startInternalExclusive(new ModifyHandler());
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

	public EventIdField getEventIdField() {
		return this.getFieldByClass(EventIdField.class);
	}

	public EventSubjectField getEventSubjectField() {
		return this.getFieldByClass(EventSubjectField.class);
	}

	public ProposedStartEventField getProposedStartEventField() {
		return this.getFieldByClass(ProposedStartEventField.class);
	}

	public ProposedEndEventDateField getProposedEndEventDateField() {
		return this.getFieldByClass(ProposedEndEventDateField.class);
	}

	public MessageField getMessageField() {
		return this.getFieldByClass(MessageField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Override
		protected int getConfiguredGridColumnCount() {
			return 1;
		}

		@Order(1000)
		public class EventIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.id");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return false;
			}

			@Override
			protected Long getConfiguredMinValue() {
				return -999999999999L;
			}

			@Override
			protected Long getConfiguredMaxValue() {
				return 999999999999L;
			}
		}

		@Order(2000)
		public class EventSubjectField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.subject");
			}

			@Override
			protected int getConfiguredGridW() {
				return 2;
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return false;
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}
		}

		@Order(3000)
		public class ParticpationInfoBox extends AbstractGroupBox {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.involevment");
			}

			@Override
			protected boolean getConfiguredLabelVisible() {
				return false;
			}

			@Override
			protected TriState getConfiguredScrollable() {
				return TriState.TRUE;
			}

			@Override
			protected int getConfiguredGridW() {
				return 3;
			}

			@Order(1000)
			public class PreviousEventBox extends AbstractGroupBox {
				@Override
				protected String getConfiguredLabel() {
					return "Avant";
				}

				@Override
				protected int getConfiguredLabelPosition() {
					return LABEL_POSITION_TOP;
				}

				@Override
				protected int getConfiguredGridW() {
					return 1;
				}

				@Override
				protected boolean getConfiguredVisible() {
					return false;
				}

				@Order(1000)
				public class PreviousEventEndField extends AbstractHtmlField {
					@Override
					protected String getConfiguredLabel() {
						return "Se termine à";
					}

					@Override
					protected boolean getConfiguredLabelVisible() {
						return false;
					}

					@Override
					protected int getConfiguredLabelPosition() {
						return LABEL_POSITION_TOP;
					}

					@Override
					protected boolean getConfiguredEnabled() {
						return false;
					}
				}

				@Order(2000)
				public class PreviousEventSubjectField extends AbstractHtmlField {
					@Override
					protected String getConfiguredLabel() {
						return "subject";
					}

					@Override
					protected boolean getConfiguredLabelVisible() {
						return false;
					}

					@Override
					protected int getConfiguredLabelPosition() {
						return LABEL_POSITION_TOP;
					}

					@Override
					protected boolean getConfiguredEnabled() {
						return false;
					}
				}
			}// end of previous Event Box

			@Order(2000)
			public class ProposedEventDateBox extends AbstractGroupBox {
				@Override
				protected String getConfiguredLabel() {
					return "Date suggérée";
				}

				@Override
				protected int getConfiguredGridW() {
					return 1;
				}

				@Order(1000)
				public class NextMenu extends AbstractMenu {
					@Override
					protected String getConfiguredText() {
						return TEXTS.get("zc.meeting.next");
					}

					@Override
					protected void execAction() {
						final CalendarService calendarService = BEANS.get(CalendarService.class);
						final DateHelper dateHelper = BEANS.get(DateHelper.class);
						final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);

						Long currentUserId;

						if (null == ChooseDateForm.this.getForUserId()) {
							final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);
							currentUserId = acsHelper.getZeroClickUserIdOfCurrentSubject();
						} else {
							currentUserId = ChooseDateForm.this.getForUserId();
						}

						final ZonedDateTime newStartDate;

						final ZonedDateTime currentStartDate = ChooseDateForm.this.getProposedStartEventField()
								.getZonedValue();

						if (null != currentStartDate) {
							newStartDate = currentStartDate.plus(Duration.ofMinutes(45));
						} else {
							newStartDate = appUserHelper.getUserNow(currentUserId);
						}

						if (null == ChooseDateForm.this.eventData) {
							throw new VetoException("Event Id required to Choose a date");
						}

						final DateReturn newPossibleDate = calendarService.searchNextDate(
								ChooseDateForm.this.eventData.getEventId(),
								ChooseDateForm.this.eventData.getDuration().getValue(),
								ChooseDateForm.this.eventData.getSlot().getValue(),
								ChooseDateForm.this.eventData.getOrganizer().getValue(), currentUserId,
								dateHelper.getZonedValue(appUserHelper.getCurrentUserTimeZone(),
										ChooseDateForm.this.eventData.getMinimalStartDate().getValue()),
								dateHelper.getZonedValue(appUserHelper.getCurrentUserTimeZone(),
										ChooseDateForm.this.eventData.getMaximalStartDate().getValue()),
								newStartDate, currentStartDate, Boolean.TRUE);

						if (newPossibleDate.isNoAvailableDate()) {
							return;// break new Date search
						} else {
							if (newPossibleDate.isCreated()) {
								// update start and end date
								ChooseDateForm.this.getProposedStartEventField().setValue(newPossibleDate.getStart());
								ChooseDateForm.this.getProposedEndEventDateField().setValue(newPossibleDate.getEnd());

								if (null != newPossibleDate.getMessageKey()) {
									ChooseDateForm.this.getMessageField()
											.setValue(TEXTS.get(newPossibleDate.getMessageKey()));
									ChooseDateForm.this.getMessageField().setVisible(true);
								} else {
									ChooseDateForm.this.getMessageField().setValue(null);
									ChooseDateForm.this.getMessageField().setVisible(false);
								}
							}
						}
					}
				}

				@Order(500)
				public class MessageField extends AbstractHtmlField {
					@Override
					protected String getConfiguredLabel() {
						return null;
					}

					@Override
					protected boolean getConfiguredLabelVisible() {
						return false;
					}
				}

				@Order(1000)
				public class ProposedSequenceBox extends AbstractGroupBox {
					@Override
					protected String getConfiguredLabel() {
						return null;
					}

					@Override
					protected boolean getConfiguredLabelVisible() {
						return false;
					}

					@Override
					protected int getConfiguredGridW() {
						return 1;
					}

					@Order(1000)
					public class ProposedStartEventField extends AbstractZonedDateField {
						@Override
						protected String getConfiguredLabel() {
							return TEXTS.get("zc.meeting.start");
						}

						@Override
						protected int getConfiguredLabelPosition() {
							return LABEL_POSITION_TOP;
						}

						@Override
						protected boolean getConfiguredEnabled() {
							return false;
						}

						@Override
						protected boolean getConfiguredHasTime() {
							return true;
						}
					}

					@Order(2000)
					public class ProposedEndEventDateField extends AbstractZonedDateField {
						@Override
						protected String getConfiguredLabel() {
							return TEXTS.get("zc.meeting.end");
						}

						@Override
						protected int getConfiguredLabelPosition() {
							return LABEL_POSITION_TOP;
						}

						@Override
						protected boolean getConfiguredEnabled() {
							return false;
						}

						@Override
						protected boolean getConfiguredHasTime() {
							return true;
						}
					}
				}// end of proposed sequence box

			}// end of Proposed Event box

			@Order(4000)
			public class NextEventBox extends AbstractGroupBox {
				@Override
				protected String getConfiguredLabel() {
					return "Après";
				}

				@Override
				protected int getConfiguredLabelPosition() {
					return LABEL_POSITION_TOP;
				}

				@Override
				protected int getConfiguredGridW() {
					return 1;
				}

				@Override
				protected boolean getConfiguredVisible() {
					return false;
				}

				@Order(1000)
				public class NextEventField extends AbstractHtmlField {
					@Override
					protected String getConfiguredLabel() {
						return "Commence à";
					}

					@Override
					protected boolean getConfiguredLabelVisible() {
						return false;
					}

					@Override
					protected int getConfiguredLabelPosition() {
						return LABEL_POSITION_TOP;
					}

					@Override
					protected boolean getConfiguredEnabled() {
						return false;
					}
				}

				@Order(2000)
				public class NextEventSubjectField extends AbstractHtmlField {
					@Override
					protected String getConfiguredLabel() {
						return "Sujet";
					}

					@Override
					protected boolean getConfiguredLabelVisible() {
						return false;
					}

					@Override
					protected int getConfiguredLabelPosition() {
						return LABEL_POSITION_TOP;
					}

					@Override
					protected boolean getConfiguredEnabled() {
						return false;
					}
				}

			}// end of Next Event box

		}// end participation box

		@Order(100000)
		public class OkButton extends AbstractOkButton {
		}

		@Order(101000)
		public class CancelButton extends AbstractCancelButton {
		}
	}

	public class ModifyHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
		}

		@Override
		protected void execStore() {
		}
	}

	public class AcceptHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IEventService eventService = BEANS.get(IEventService.class);
			ChooseDateForm.this.eventData = eventService.load(ChooseDateForm.this.getEventIdField().getValue());
		}

		@Override
		protected void execStore() {
			final EventWorkflow eventWorkflow = BEANS.get(EventWorkflow.class);

			ChooseDateForm.this.eventData.getStartDate()
					.setValue(ChooseDateForm.this.getProposedStartEventField().getValue());
			ChooseDateForm.this.eventData.getEndDate()
					.setValue(ChooseDateForm.this.getProposedEndEventDateField().getValue());

			// save new date
			eventWorkflow.acceptEvent(ChooseDateForm.this.eventData, ChooseDateForm.this.getForUserId());
		}
	}
}
