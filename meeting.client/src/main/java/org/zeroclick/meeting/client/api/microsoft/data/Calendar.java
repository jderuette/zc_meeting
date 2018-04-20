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
package org.zeroclick.meeting.client.api.microsoft.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author djer
 * @see https://developer.microsoft.com/fr-fr/graph/docs/api-reference/v1.0/resources/calendar
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Calendar {

	private String id;
	private String name;
	private String color;
	private String changeKey;
	private Boolean canViewPrivateItems;
	private Boolean canShare;
	private Boolean canEdit;

	private Owner owner;

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getColor() {
		return this.color;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	public String getChangeKey() {
		return this.changeKey;
	}

	public void setChangeKey(final String changeKey) {
		this.changeKey = changeKey;
	}

	public Boolean getCanViewPrivateItems() {
		return this.canViewPrivateItems;
	}

	public void setCanViewPrivateItems(final Boolean canViewPrivateItems) {
		this.canViewPrivateItems = canViewPrivateItems;
	}

	public Boolean getCanShare() {
		return this.canShare;
	}

	public void setCanShare(final Boolean canShare) {
		this.canShare = canShare;
	}

	public Boolean getCanEdit() {
		return this.canEdit;
	}

	public void setCanEdit(final Boolean canEdit) {
		this.canEdit = canEdit;
	}

	public Owner getOwner() {
		return this.owner;
	}

	public void setOwner(final Owner owner) {
		this.owner = owner;
	}

}
