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
package org.zeroclick.testing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.shared.services.lookup.BatchLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupResultCache;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.IServerBatchLookupService;

/**
 * @author djer Copy of BatchLookupService (used only for UnitTest)
 */
public class BatchLookupServiceClient implements IServerBatchLookupService {
	@Override
	public List<List<ILookupRow<?>>> getBatchDataByKey(final BatchLookupCall batch) {
		final List<ILookupCall<?>> calls = batch.getCallBatch();
		final List<List<ILookupRow<?>>> result = new ArrayList<>();
		final BatchLookupResultCache cache = new BatchLookupResultCache();
		for (final ILookupCall<?> call : calls) {
			result.add(new ArrayList<>(cache.getDataByKey(call)));
		}
		return result;
	}

	@Override
	public List<List<ILookupRow<?>>> getBatchDataByText(final BatchLookupCall batch) {
		final List<ILookupCall<?>> calls = batch.getCallBatch();
		final List<List<ILookupRow<?>>> result = new ArrayList<>();
		final BatchLookupResultCache cache = new BatchLookupResultCache();
		for (final ILookupCall<?> call : calls) {
			result.add(new ArrayList<>(cache.getDataByText(call)));
		}
		return result;
	}

	@Override
	public List<List<ILookupRow<?>>> getBatchDataByAll(final BatchLookupCall batch) {
		final List<ILookupCall<?>> calls = batch.getCallBatch();
		final List<List<ILookupRow<?>>> result = new ArrayList<>();
		final BatchLookupResultCache cache = new BatchLookupResultCache();
		for (final ILookupCall<?> call : calls) {
			result.add(new ArrayList<>(cache.getDataByAll(call)));
		}
		return result;
	}

	@Override
	public List<List<ILookupRow<?>>> getBatchDataByRec(final BatchLookupCall batch) {
		final List<ILookupCall<?>> calls = batch.getCallBatch();
		final List<List<ILookupRow<?>>> result = new ArrayList<>();
		final BatchLookupResultCache cache = new BatchLookupResultCache();
		for (final ILookupCall<?> call : calls) {
			result.add(new ArrayList<>(cache.getDataByRec(call)));
		}
		return result;
	}
}
