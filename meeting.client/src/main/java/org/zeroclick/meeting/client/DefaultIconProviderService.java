package org.zeroclick.meeting.client;

import java.net.URL;

import org.eclipse.scout.rt.client.services.common.icon.AbstractIconProviderService;

/**
 * <h3>{@link DefaultIconProviderService}</h3>
 *
 * @author djer
 */
public class DefaultIconProviderService extends AbstractIconProviderService {
	@Override
	protected URL findResource(final String relativePath) {
		return ResourceBase.class.getResource("icons/" + relativePath);
	}
}
