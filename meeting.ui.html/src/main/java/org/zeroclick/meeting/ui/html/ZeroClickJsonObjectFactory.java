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
package org.zeroclick.meeting.ui.html;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.date.UTCDate;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.JsonObjectFactory;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.zeroclick.meeting.ui.html.json.form.fields.JsonZeroClickUUTCDateField;
import org.zeroclick.ui.form.fields.datefield.IZeroClickUTCDateField;

/**
 * @author djer
 *
 */
@Bean
@Order(5600)
public class ZeroClickJsonObjectFactory extends JsonObjectFactory {

	@Override
	public IJsonAdapter<?> createJsonAdapter(final Object model, final IUiSession session, final String id,
			final IJsonAdapter<?> parent) {
		IJsonAdapter<?> adapter = super.createJsonAdapter(model, session, id, parent);

		if (null == adapter) {
			if (model instanceof IZeroClickUTCDateField) {
				adapter = new JsonZeroClickUUTCDateField<>((IZeroClickUTCDateField) model, session, id, parent);
			}
		}
		return adapter;
	}

	@Override
	public IJsonObject createJsonObject(final Object object) {
		IJsonObject jsonObject = super.createJsonObject(object);

		if (null == jsonObject) {
			if (object instanceof UTCDate) {
				jsonObject = new JsonDate((UTCDate) object);
			}
		}

		return jsonObject;
	}
}
