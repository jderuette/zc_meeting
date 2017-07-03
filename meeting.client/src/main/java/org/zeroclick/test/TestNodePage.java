package org.zeroclick.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.zeroclick.configuration.client.user.UserTablePage;
import org.zeroclick.meeting.client.event.EventTablePage;

public class TestNodePage extends AbstractTestNodePage {

	@Override
	protected void execCreateChildPages(final List<IPage<?>> pageList) {
		// TODO [djer] Auto-generated method stub.
		final List<IPage<?>> pages = new ArrayList<>();
		pages.add(new EventTablePage());
		pages.add(new UserTablePage());
		super.execCreateChildPages(pages);
	}
}
