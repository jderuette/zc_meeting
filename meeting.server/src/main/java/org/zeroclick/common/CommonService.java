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
package org.zeroclick.common;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.context.ServerRunContextProducer;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.comon.text.UserHelper;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class CommonService {

	protected UserHelper userHelper;

	@PostConstruct
	public void init() {
		final UserHelper userHelper = BEANS.get(UserHelper.class);
		this.userHelper = userHelper;
	}

	public void throwAuthorizationFailed() throws VetoException {
		throw new VetoException(TEXTS.get("AuthorizationFailed"));
	}

	protected void insertInsideNewTransaction(final String sql, final Object... bindBases) {
		Jobs.schedule(new IRunnable() {

			@Override
			public void run() throws Exception {
				SQL.insert(sql, bindBases);
			}
		}, Jobs.newInput().withRunContext(this.buildNewTransactionRunContext()));
	}

	protected void updateInsideNewTransaction(final String sql, final Object... bindBases) {
		Jobs.schedule(new IRunnable() {

			@Override
			public void run() throws Exception {
				SQL.update(sql, bindBases);
			}
		}, Jobs.newInput().withRunContext(this.buildNewTransactionRunContext()));
	}

	protected void selectIntoInsideNewTransaction(final String sql, final Object... bindBases) {
		Jobs.schedule(new IRunnable() {

			@Override
			public void run() throws Exception {
				SQL.selectInto(sql, bindBases);
			}
		}, Jobs.newInput().withRunContext(this.buildNewTransactionRunContext()));
	}

	private RunContext buildNewTransactionRunContext() {
		return new ServerRunContextProducer().produce(this.userHelper.getCurrentUserSubject());
	}

}
