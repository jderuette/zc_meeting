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

/**
 * @author djer
 * @see {@link https://developer.microsoft.com/fr-fr/graph/docs/api-reference/v1.0/resources/responsestatus}
 *
 */
public class ResponseStatus {
	private String response;
	private DateTimeTimeZone time;

	/**
	 * One of : None, Organizer, TentativelyAccepted, Accepted, Declined,
	 * NotResponded
	 *
	 * @return
	 */
	public String getResponse() {
		return this.response;
	}

	public void setResponse(final String response) {
		this.response = response;
	}

	/**
	 * Date et heure auxquelles la réponse a été renvoyée. Utilise le format ISO
	 * 8601, toujours en heure UTC. Par exemple, minuit UTC le 1er janvier 2014
	 * se présente comme suit : '2014-01-01T00:00:00Z'
	 *
	 * @return
	 */
	public DateTimeTimeZone getTime() {
		return this.time;
	}

	public void setTime(final DateTimeTimeZone time) {
		this.time = time;
	}
}
