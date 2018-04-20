package org.zeroclick.configuration.client.slot;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.configuration.client.slot.DayDurationForm.MainBox.CancelButton;
import org.zeroclick.configuration.client.slot.DayDurationForm.MainBox.OkButton;
import org.zeroclick.configuration.client.slot.DayDurationForm.MainBox.OrderInSlotField;
import org.zeroclick.configuration.client.slot.DayDurationForm.MainBox.WeekEndDaysBox;
import org.zeroclick.configuration.client.slot.DayDurationForm.MainBox.WeeklyPerpetualField;
import org.zeroclick.configuration.shared.slot.DayDurationFormData;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.slot.UpdateSlotPermission;
import org.zeroclick.meeting.client.NotificationHelper;

@FormData(value = DayDurationFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class DayDurationForm extends AbstractForm {

	private Long dayDurationId;
	private String name;
	private Long slotId;
	private Long userId;
	private String slotCode; // the "parent" slot name

	@FormData
	public Long getDayDurationId() {
		return this.dayDurationId;
	}

	@FormData
	public void setDayDurationId(final Long dayDurationId) {
		this.dayDurationId = dayDurationId;
	}

	@FormData
	public String getName() {
		return this.name;
	}

	@FormData
	public void setName(final String name) {
		this.name = name;
	}

	@FormData
	public Long getSlotId() {
		return this.slotId;
	}

	@FormData
	public void setSlotId(final Long slotId) {
		this.slotId = slotId;
	}

	@FormData
	public Long getUserId() {
		return this.userId;
	}

	@FormData
	public void setUserId(final Long userId) {
		this.userId = userId;
	}

	@FormData
	public String getSlotCode() {
		return this.slotCode;
	}

	@FormData
	public void setSlotCode(final String slotName) {
		this.slotCode = slotName;
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.dayDuration");
	}

	@Override
	public Object computeExclusiveKey() {
		return this.getDayDurationId();
	}

	public void startModify() {
		this.startInternalExclusive(new ModifyHandler());
	}

	public void startNew() {
		this.startInternal(new NewHandler());
	}

	public CancelButton getCancelButton() {
		return this.getFieldByClass(CancelButton.class);
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public WeeklyPerpetualField getWeeklyPerpetualField() {
		return this.getFieldByClass(WeeklyPerpetualField.class);
	}

	public OrderInSlotField getOrderInSlotField() {
		return this.getFieldByClass(OrderInSlotField.class);
	}

	public WeekEndDaysBox getWeekEndDaysBox() {
		return this.getFieldByClass(WeekEndDaysBox.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	protected void preapreStore() {
		// this.getMainBox().getHoursBox().getSlotStartField().prepapreStore();
		// this.getMainBox().getHoursBox().getSlotEndField().prepapreStore();
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		public HoursBox getHoursBox() {
			return this.getFieldByClass(HoursBox.class);
		}

		public WorkDayBox getWorkDayBox() {
			return this.getFieldByClass(WorkDayBox.class);
		}

		public WeekEndDaysBox getWeekEndDaysBox() {
			return this.getFieldByClass(WeekEndDaysBox.class);
		}

		@Order(1500)
		public class HoursBox extends AbstractSequenceBox {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.dayDuration.hour");
			}

			@Override
			protected int getConfiguredLabelPosition() {
				return IFormField.LABEL_POSITION_TOP;
			}

			@Override
			protected boolean getConfiguredAutoCheckFromTo() {
				return false;
			}

			public SlotStartField getSlotStartField() {
				return this.getFieldByClass(SlotStartField.class);
			}

			public SlotEndField getSlotEndField() {
				return this.getFieldByClass(SlotEndField.class);
			}

			@Order(2000)
			public class SlotStartField extends AbstractDateField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.dayDuration.hour.start");
				}

				@Override
				protected int getConfiguredLabelPosition() {
					return IFormField.LABEL_POSITION_ON_FIELD;
				}

				@Override
				protected boolean getConfiguredHasTime() {
					return true;
				}

				@Override
				protected boolean getConfiguredHasDate() {
					return false;
				}
			}

			@Order(3000)
			public class SlotEndField extends AbstractDateField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.dayDuration.hour.end");
				}

				@Override
				protected int getConfiguredLabelPosition() {
					return IFormField.LABEL_POSITION_ON_FIELD;
				}

				@Override
				protected boolean getConfiguredHasTime() {
					return true;
				}

				@Override
				protected boolean getConfiguredHasDate() {
					return false;
				}
			}
		}

		@Order(4000)
		public class WorkDayBox extends AbstractSequenceBox {
			@Override
			protected String getConfiguredLabel() {
				return null;
			}

			@Override
			protected boolean getConfiguredLabelVisible() {
				return false;
			}

			@Override
			protected boolean getConfiguredAutoCheckFromTo() {
				return false;
			}

			public MondayField getMondayField() {
				return this.getFieldByClass(MondayField.class);
			}

			public TuesdayField getTuesdayField() {
				return this.getFieldByClass(TuesdayField.class);
			}

			public WednesdayField getWednesdayField() {
				return this.getFieldByClass(WednesdayField.class);
			}

			public ThursdayField getThursdayField() {
				return this.getFieldByClass(ThursdayField.class);
			}

			public FridayField getFridayField() {
				return this.getFieldByClass(FridayField.class);
			}

			@Order(1000)
			public class MondayField extends AbstractBooleanField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.dayDuration.monday");
				}
			}

			@Order(2000)
			public class TuesdayField extends AbstractBooleanField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.dayDuration.tuesday");
				}
			}

			@Order(3000)
			public class WednesdayField extends AbstractBooleanField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.dayDuration.wednesday");
				}
			}

			@Order(4000)
			public class ThursdayField extends AbstractBooleanField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.dayDuration.thursday");
				}
			}

			@Order(5000)
			public class FridayField extends AbstractBooleanField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.dayDuration.friday");
				}
			}
		}

		@Order(5000)
		public class WeekEndDaysBox extends AbstractSequenceBox {
			@Override
			protected String getConfiguredLabel() {
				return null;
			}

			@Override
			protected boolean getConfiguredLabelVisible() {
				return false;
			}

			@Override
			protected boolean getConfiguredAutoCheckFromTo() {
				return false;
			}

			public SaturdayField getSaturdayField() {
				return this.getFieldByClass(SaturdayField.class);
			}

			public SundayField getSundayField() {
				return this.getFieldByClass(SundayField.class);
			}

			@Order(1000)
			public class SaturdayField extends AbstractBooleanField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.dayDuration.saturday");
				}
			}

			@Order(2000)
			public class SundayField extends AbstractBooleanField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.dayDuration.sunday");
				}
			}
		}

		@Order(6000)
		public class WeeklyPerpetualField extends AbstractBooleanField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.dayDuration.weeklyPerpetual");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}
		}

		@Order(7000)
		public class OrderInSlotField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.dayDuration.orderInSlot");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected Long getConfiguredMinValue() {
				return 0L;
			}

			@Override
			protected Long getConfiguredMaxValue() {
				return 999999999999L;
			}
		}

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
			final ISlotService service = BEANS.get(ISlotService.class);
			DayDurationFormData formData = new DayDurationFormData();
			DayDurationForm.this.exportFormData(formData);
			formData = service.load(formData);
			DayDurationForm.this.importFormData(formData);

			DayDurationForm.this.setEnabledPermission(new UpdateSlotPermission());
		}

		@Override
		protected void execStore() {

			final ISlotService service = BEANS.get(ISlotService.class);
			final DayDurationFormData formData = new DayDurationFormData();
			DayDurationForm.this.preapreStore();
			DayDurationForm.this.exportFormData(formData);
			service.store(formData);

			final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
			notificationHelper.addProccessedNotification("zc.meeting.notification.modifiedDayDuration",
					TEXTS.get(formData.getName()));
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
		}

		@Override
		protected void execStore() {
		}
	}
}
