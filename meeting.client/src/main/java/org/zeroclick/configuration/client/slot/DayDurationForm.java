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
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.configuration.client.slot.DayDurationForm.MainBox.CancelButton;
import org.zeroclick.configuration.client.slot.DayDurationForm.MainBox.OkButton;
import org.zeroclick.configuration.client.slot.DayDurationForm.MainBox.WeeklyPerpetualField;
import org.zeroclick.configuration.shared.slot.DayDurationFormData;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.slot.UpdateSlotPermission;

@FormData(value = DayDurationFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class DayDurationForm extends AbstractForm {

	private Long dayDurationId;
	private String name;
	private Long slotId;
	private Long userId;

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

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

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
				protected boolean getConfiguredHasDate() {
					return Boolean.FALSE;
				}

				@Override
				protected boolean getConfiguredHasTime() {
					return Boolean.TRUE;
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
				protected boolean getConfiguredHasDate() {
					return Boolean.FALSE;
				}

				@Override
				protected boolean getConfiguredHasTime() {
					return Boolean.TRUE;
				}
			}
		}

		@Order(4000)
		public class DaysBox extends AbstractSequenceBox {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.dayDuration.validDays");
			}

			@Override
			protected boolean getConfiguredAutoCheckFromTo() {
				return false;
			}

			@Override
			protected int getConfiguredLabelPosition() {
				return IFormField.LABEL_POSITION_TOP;
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
			public class ThursdayField extends AbstractBooleanField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.dayDuration.thursday");
				}
			}

			@Order(4000)
			public class WednesdayField extends AbstractBooleanField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.dayDuration.wednesday");
				}
			}

			@Order(5000)
			public class FridayField extends AbstractBooleanField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.dayDuration.friday");
				}
			}

			@Order(6000)
			public class SaturdayField extends AbstractBooleanField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.dayDuration.saturday");
				}
			}

			@Order(7000)
			public class SundayField extends AbstractBooleanField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.meeting.dayDuration.sunday");
				}
			}
		}

		@Order(5000)
		public class WeeklyPerpetualField extends AbstractBooleanField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.dayDuration.weeklyPerpetual");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
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
			DayDurationForm.this.exportFormData(formData);
			service.store(formData);
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
