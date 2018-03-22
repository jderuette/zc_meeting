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

/**
 * @author djer
 *
 */
public class HtmlResponseHelper {

	StringBuilder content;

	public HtmlResponseHelper() {
		this.content = new StringBuilder(250);
		this.addPageBegin();
	}

	public void addSuccessMessage(final String message) {
		this.content.append("<b>").append(message).append("</b><br/>");
	}

	public void addErrorMessage(final String message) {
		this.content.append("<b style='font-color:red'>").append(message).append("</b><br/>");
	}

	public String getPageContent() {
		this.addPageEnd();
		return this.content.toString();
	}

	private void addPageBegin() {
		this.content.append("<html><head></head><body>");
	}

	private void addPageEnd() {
		this.content.append("</body></html>");
	}

}
