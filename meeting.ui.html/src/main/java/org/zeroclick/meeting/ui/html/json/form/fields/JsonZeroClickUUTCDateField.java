/**
 * Copyright 2017 Djer13

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
limitations under the License.
 */
package org.zeroclick.meeting.ui.html.json.form.fields;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IUTCDateField;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.platform.util.date.UTCDate;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONArray;
import org.zeroclick.ui.form.fields.datefield.IZeroClickUTCDateField;

/**
 * @author djer
 *
 */
public class JsonZeroClickUUTCDateField<T extends IZeroClickUTCDateField> extends JsonValueField<T> {
	private static final String PROP_TIMESTAMP = "timestamp";
	private static final String PROP_AUTO_TIMESTAMP = "autoTimestamp";
	// UI events
	private static final String EVENT_TIMESTAMP_CHANGED = "timestampChanged";
	private static final String EVENT_PARSING_ERROR = "parsingError";

	public JsonZeroClickUUTCDateField(final T model, final IUiSession uiSession, final String id,
			final IJsonAdapter<?> parent) {
		super(model, uiSession, id, parent);
	}

	@Override
	public String getObjectType() {
		return "ZeroClickUUTCDateField";
	}

	@Override
	protected void initJsonProperties(final T model) {
		super.initJsonProperties(model);
		this.putJsonProperty(new JsonProperty<T>(PROP_TIMESTAMP, model) {
			@Override
			protected UTCDate modelValue() {
				return this.getModel().getValue();
			}

			@Override
			public Object prepareValueForToJson(final Object value) {
				return JsonZeroClickUUTCDateField.this.dateToJson((UTCDate) value);
			}
		});
		this.putJsonProperty(new JsonProperty<T>(PROP_AUTO_TIMESTAMP, model) {
			@Override
			protected UTCDate modelValue() {
				return this.getModel().getAutoDate();
			}

			@Override
			public Object prepareValueForToJson(final Object value) {
				return JsonZeroClickUUTCDateField.this.dateToJson((UTCDate) value);
			}
		});
		this.putJsonProperty(new JsonProperty<T>(IUTCDateField.PROP_HAS_TIME, model) {
			@Override
			protected Boolean modelValue() {
				return this.getModel().isHasTime();
			}
		});
		this.putJsonProperty(new JsonProperty<T>(IUTCDateField.PROP_HAS_DATE, model) {
			@Override
			protected Boolean modelValue() {
				return this.getModel().isHasDate();
			}
		});
		this.putJsonProperty(new JsonProperty<T>(IUTCDateField.PROP_DATE_FORMAT_PATTERN, model) {
			@Override
			protected String modelValue() {
				return this.getModel().getDateFormatPattern();
			}
		});
		this.putJsonProperty(new JsonProperty<T>(IUTCDateField.PROP_TIME_FORMAT_PATTERN, model) {
			@Override
			protected String modelValue() {
				return this.getModel().getTimeFormatPattern();
			}
		});
		this.putJsonProperty(new JsonProperty<T>(IUTCDateField.PROP_ALLOWED_DATES, model) {
			@Override
			protected List<UTCDate> modelValue() {
				return this.getModel().getAllowedDates();
			}

			@Override
			@SuppressWarnings("unchecked")
			public Object prepareValueForToJson(final Object value) {
				final List<UTCDate> allowedDates = (List<UTCDate>) value;
				if (allowedDates == null || allowedDates.isEmpty()) {
					return null;
				}
				final JSONArray dateArray = new JSONArray();
				for (final UTCDate date : allowedDates) {
					dateArray.put(JsonZeroClickUUTCDateField.this.dateToJson(date));
				}
				return dateArray;
			}
		});
	}

	protected String dateToJson(final UTCDate date) {
		if (date == null) {
			return null;
		}
		return new JsonDate(date).asJsonString(false, this.getModel().isHasDate(), this.getModel().isHasTime());
	}

	@Override
	protected void handleModelPropertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		// Translate "value changed" to "timestamp changed"
		if (IUTCDateField.PROP_VALUE.equals(propertyName)) {
			final PropertyChangeEvent filteredEvent = this.filterPropertyChangeEvent(event);
			if (filteredEvent != null) {
				this.addPropertyChangeEvent(PROP_TIMESTAMP, this.dateToJson((UTCDate) event.getNewValue()));
			}
		}
		// Translate "auto date changed" to "auto timestamp changed"
		else if (IUTCDateField.PROP_AUTO_DATE.equals(propertyName)) {
			final PropertyChangeEvent filteredEvent = this.filterPropertyChangeEvent(event);
			if (filteredEvent != null) {
				this.addPropertyChangeEvent(PROP_AUTO_TIMESTAMP, this.dateToJson((UTCDate) event.getNewValue()));
			}
		} else {
			super.handleModelPropertyChange(event);
		}
	}

	@Override
	public void handleUiEvent(final JsonEvent event) {
		if (EVENT_TIMESTAMP_CHANGED.equals(event.getType())) {
			this.handleUiTimestampChanged(event);
		} else if (EVENT_PARSING_ERROR.equals(event.getType())) {
			this.handleUiParsingError(event);
		} else {
			super.handleUiEvent(event);
		}
	}

	protected void handleUiTimestampChanged(final JsonEvent event) {
		final UTCDate uiValue = TypeCastUtility
				.castValue(new JsonDate(event.getData().optString(PROP_TIMESTAMP, null)).asJavaDate(), UTCDate.class);
		this.addPropertyEventFilterCondition(IValueField.PROP_VALUE, uiValue);
		this.getModel().getUIFacade().removeParseErrorFromUI();
		this.getModel().getUIFacade().setDateTimeFromUI(uiValue);

		// If the model value is changed during validation, it needs to be
		// updated in the GUI again.
		final UTCDate modelValue = this.getModel().getValue();
		if (!DateUtility.equals(uiValue, modelValue)) {
			this.addPropertyChangeEvent(PROP_TIMESTAMP, this.dateToJson(modelValue));
		}

	}

	protected void handleUiParsingError(final JsonEvent event) {
		this.getModel().getUIFacade().removeParseErrorFromUI();
		this.getModel().getUIFacade().setParseErrorFromUI();
	}

	@Override
	protected void handleUiDisplayTextChangedWhileTyping(final String displayText) {
		throw new IllegalStateException("While typing is not supported by the date field.");
	}

	@Override
	protected void handleUiDisplayTextChangedAfterTyping(final String displayText) {
		this.getModel().getUIFacade().setDisplayTextFromUI(displayText);
	}
}
