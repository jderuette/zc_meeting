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
package org.zeroclick.configuration.shared.subscription;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.document.DocumentFormData;
import org.zeroclick.comon.text.TextsHelper;
import org.zeroclick.configuration.shared.params.IAppParamsService;
import org.zeroclick.configuration.shared.role.IRoleService;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.meeting.shared.event.CreateEventPermission;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.StateCodeType;
import org.zeroclick.meeting.shared.security.AccessControlService;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class SubscriptionHelper {

	private static final Logger LOG = LoggerFactory.getLogger(SubscriptionHelper.class);

	public static final int LEVEL_SUB_FREE = 10;
	public static final int LEVEL_SUB_PRO = 20;
	public static final int LEVEL_SUB_BUSINESS = 30;

	public static final String PARAM_KEY_URL_BASE = "subscription.payment.url.";
	public static final String PARAM_KEY_URL_NAME_BASE = "subscription.payment.url.name.";

	public int getLevelForCurrentUser() {
		return this.collectUserRequirement().getUserRequiredLevel();
	}

	protected Integer getMaxAllowedEvent() {
		final IAppParamsService paramService = BEANS.get(IAppParamsService.class);
		return Integer.valueOf(paramService.getValue("subFreeEventLimit"));
	}

	public SubscriptionHelperData canCreateEvent() {
		return this.collectUserRequirement();
	}

	private SubscriptionHelperData collectUserRequirement() {
		final IEventService eventService = BEANS.get(IEventService.class);
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		Integer requiredLevel = LEVEL_SUB_FREE;
		int nbEventWaiting = 0;
		final Map<Long, Integer> nbEventPendingByUsers = eventService.getNbEventsByUser(StateCodeType.AskedCode.ID,
				Boolean.TRUE);
		if (null != nbEventPendingByUsers && nbEventPendingByUsers.size() > 0) {
			final Iterator<Integer> itNbEvents = nbEventPendingByUsers.values().iterator();
			while (itNbEvents.hasNext()) {
				nbEventWaiting += itNbEvents.next();
			}
		}

		if (nbEventWaiting >= this.getMaxAllowedEvent()) {
			requiredLevel = LEVEL_SUB_PRO;
		}

		final int userCurrentCreateEventLevel = ACCESS.getLevel(new CreateEventPermission());
		final int maxEventAllowedForFree = this.getMaxAllowedEvent();
		final SubscriptionHelperData userData = new SubscriptionHelperData(requiredLevel, userCurrentCreateEventLevel,
				nbEventWaiting, maxEventAllowedForFree);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Subsciption Data for user id : " + acs.getZeroClickUserIdOfCurrentSubject() + " : "
					+ userData.getLogMessage());
		}

		return userData;
	}

	public String getCpsText(final Long subscriptionId) {
		String cpsText = null;
		final IRoleService roleService = BEANS.get(IRoleService.class);
		final DocumentFormData documentFormData = roleService.getActiveDocument(subscriptionId);
		cpsText = documentFormData.getContent().getValue();

		// if (subscriptionId == 3l) {// free
		// cpsText = "<h1>CPS 0Click</h1>" + "<h2>Faites ce que vous voulez !
		// </h2>";
		// } else {
		// cpsText = "<h1>CPS 0Click</h1>" + "<h2>Préambule</h2>"
		// + "<p>Les présentes conditions de prestation de service (les
		// “Conditions“) sont conclues entre ELYCOOP SCOP SARL (“Elycoop”)
		// <strong>représentée par son gérant Jimmy MERCANTE domicilié à
		// l’adresse : Pôle Pixel – Bât B – 26 rue Emile Decorps – 69100
		// VILLEURBANNE, immatriculé au RCS de {ville} sous le numéro de Siret :
		// 429 851 637 000 34 et le code APE 7022 Z</strong>. Et l’entité
		// acceptant les présentes conditions (“Vous” ou “Vos” ou “Votre”). Ces
		// conditions régissent votre utilisation du service 0Click (le
		// “Service“). Elycoop et Vous sont ci-après désignés individuellement
		// comme une “Partie“ ou collectivement comme les “Parties“.</p>"
		// + "<p>Les présentes conditions générales et les factures forment
		// ensemble le contrat (le “contrat“).</p>"
		// + "<p>EN CLIQUANT SUR LE BOUTON \"J'ACCEPTE\", EN TERMINANT LE
		// PROCESSUS D'INSCRIPTION OU EN UTILISANT LE SERVICE, VOUS DÉCLAREZ
		// AVOIR LU ET ACCEPTÉ LES PRÉSENTES CONDITIONS, ET ÊTRE AUTORISÉ À AGIR
		// POUR LE COMPTE DU TITULAIRE DU COMPTE ET DE LE LIER AUX PRÉSENTES
		// CONDITIONS.</p>"
		// + "<p>Compte tenu de ce qui précède, les parties acceptent ce qui
		// suit :</p>"
		// + "<h2>Article 1 – Définitions</h2>" + "<h3>1.01 - Abonnement</h3>"
		// + "<p>Désigne le contrat conclu entre Elycoop et Vous pendant une
		// période déterminée (précisée à l’Article 3) en vue de l'accès au
		// Service moyennant le versement d'un prix forfaitaire et global.</p>"
		// + "<h3>1.02 - Documentation</h3>"
		// + "<p>Désigne les manuels d’utilisations, informations utilisateurs,
		// documentations techniques et tous autres documents relatifs à
		// l’utilisation du Service. La Documentation est fournie en format
		// électronique et est disponible sur le site internet du service.</p>"
		// + "<h3>1.03 - Données</h3>"
		// + "<p>Désigne l’ensemble des données ou informations collectées et
		// traitées par Vous, au moyen du Service.</p>"
		// + "<h3>Fonctionnalités du Service</h3>"
		// + "Elycoop Vous fournira le Service suivants au titre du contrat,
		// sous réserve du respect de l’ensemble de ses stipulations par Vous.
		// La liste des fonctionnalités du Service est précisé sur l’interface
		// de gestion des Logiciels."
		// + "<h3>1.05 - Logiciels</h3>"
		// + "<p>Désignent les programmes développés par Elycoop et mis à
		// disposition de Vous par l’intermédiaire du Serveur, aux fins de vous
		// permettre à ce dernier d’accéder aux Services, conformément aux
		// stipulations du contrat.</p>"
		// + "<h3>1.06 - Prix</h3>"
		// + "<p>Désigne les redevances dues par Vous à Elycoop, telles que
		// spécifiées dans l’article 4, au titre de la fourniture dues
		// Services.</p>"
		// + "<h3>1.07 - Serveur</h3>"
		// + "<p>Désigne lesa machines et les équipements réseaux exploité par
		// Elycoop, sur lequel sont installés les Logiciels, et permettant la
		// fourniture du Service.</p>"
		// + "<h3>1.08 - Site</h3>"
		// + "</p>Désigne le site internet développé, hébergé sur le Serveur par
		// Elycoop aux fins de piloter les Logiciels permettant la fourniture
		// des Services.</p>"
		// + "<h3>1.09 - Stripe</h3>"
		// + "<p>Désigne la société Stripe, Inc. (185 Berry Street, Suite 550,
		// San Francisco, CA 94107) s’occupant d’enregistrer les données
		// relatives à la carte bancaire, communiquées par Vous, procédant aux
		// paiements.</p>"
		// + "<h3>1.10 - Support</h3>"
		// + "Désigne les services d’aide à l'utilisation, de configurations et
		// de conseil que Elycoop Vous fournit pour lui permettre une
		// utilisation adéquate des Logiciels et des Services. Le support est
		// joignable en envoyant un email à l’adresse <a
		// href='mailto:meeting@0click.org'>meeting@0click.org</a>.</p>"
		// + "Vous pouvez informer Elycoop des incidents que Vous considérez
		// comme des incidents ou dysfonctionnements par courrier électronique à
		// l’adresse et fournir à Elycoop toutes informations pertinentes aux
		// fins de permettre à Elycoop d’essayer de reproduire lesdits incidents
		// ou dysfonctionnement, étant précisé que la nécessité et la teneur de
		// toute éventuelle réponse à apporter à tout incident ou
		// dysfonctionnement ainsi signalé reste à l’entière discrétion
		// d’Elycoop."
		// + "<h2>Article 2 - Objet</h2>";
		// }
		return cpsText;
	}

	/**
	 * get the payment URL to redirect USer for the subscription. null means no
	 * payment required.
	 *
	 * @param subscriptionId
	 * @return
	 */
	public String getSubscriptionPaymentURL(final Long subscriptionId) {
		final IAppParamsService appParamService = BEANS.get(IAppParamsService.class);
		final String paramUrlKey = appParamService.getValue(PARAM_KEY_URL_BASE + subscriptionId);
		final String paramUrlNameKey = appParamService.getValue(PARAM_KEY_URL_NAME_BASE + subscriptionId);

		final String urlFromParams = TextsHelper.get(paramUrlKey);
		final String urlNameFromParams = TextsHelper.get(paramUrlNameKey);

		String url = null;
		final StringBuilder sbLink = new StringBuilder(64);
		if (null != urlFromParams && null != urlNameFromParams) {
			sbLink.append("<a href='").append(urlFromParams).append("' target='_blank'>").append(urlNameFromParams)
					.append("</a>");
			url = sbLink.toString();
		}

		// if (subscriptionId == 3l) {// free
		// url = null;
		// } else if (subscriptionId == 4l) {// pro
		// url = "<a
		// href='https://subscriptions.zoho.com/subscribe/e0c71c8b88c7cb1944d3227cb7edba566a2bba0f6b053217afe8ded60e8a6aa6/TEST_PRO'
		// target='blank'>"
		// + TEXTS.get("zc.user.role.pro") + "<a>";
		// } else if (subscriptionId == 5l) {// business
		// url = "<a
		// href='https://subscriptions.zoho.com/subscribe/e0c71c8b88c7cb1944d3227cb7edba566a2bba0f6b053217afe8ded60e8a6aa6/TEST_BUSINESS'
		// target='_blank'>"
		// + TEXTS.get("zc.user.role.business") + "<a>";
		// } else {
		// url = "Unknow subscription id";
		// }
		return url;
	}

	public Boolean isNewSubscriptionForCurrentuser(final Long subscriptionId) {
		final IUserService userService = BEANS.get(IUserService.class);
		final UserFormData userDetails = userService.getCurrentUserDetails();
		final Long currentUserSubscription = userDetails.getSubscriptionBox().getValue();
		return !currentUserSubscription.equals(subscriptionId);
	}

	public class SubscriptionHelperData {
		private int userRequiredLevel;
		private int userCurrentLevel;
		private Integer userNbAskedEvent;
		private Integer subscriptionAllowedEvent;
		private final Boolean accessAllowed;
		private final String messageKey;

		public SubscriptionHelperData(final int userRequiredLevel, final int userCurrentLevel,
				final Integer userNbAskedEvent, final Integer subscriptionAllowedEvent) {
			super();
			this.userRequiredLevel = userRequiredLevel;
			this.userCurrentLevel = userCurrentLevel;
			this.userNbAskedEvent = userNbAskedEvent;
			this.subscriptionAllowedEvent = subscriptionAllowedEvent;
			this.accessAllowed = userCurrentLevel >= userRequiredLevel;
			this.messageKey = "zc.subscription.notAllowed";
		}

		public String getLogMessage() {
			final StringBuilder builder = new StringBuilder(64);
			builder.append(this.messageKey).append(" User level : ").append(this.userCurrentLevel)
					.append(" required : ").append(this.userRequiredLevel).append(" nbEvent : ")
					.append(this.userNbAskedEvent).append('/').append(this.subscriptionAllowedEvent);

			return builder.toString();
		}

		public String getUserMessage() {
			return TEXTS.get(this.messageKey, String.valueOf(this.userCurrentLevel),
					String.valueOf(this.userRequiredLevel), String.valueOf(this.userNbAskedEvent),
					String.valueOf(this.subscriptionAllowedEvent));
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder(246);
			builder.append("SubscriptionHelperData [userRequiredLevel=").append(this.userRequiredLevel)
					.append(", userCurrentLevel=").append(this.userCurrentLevel).append(", userNbAskedEvent=")
					.append(this.userNbAskedEvent).append(", subscriptionAllowedEvent=")
					.append(this.subscriptionAllowedEvent).append(", accessAllowed=").append(this.accessAllowed)
					.append(", messageKey=").append(this.messageKey).append(']');
			return builder.toString();
		}

		public int getUserRequiredLevel() {
			return this.userRequiredLevel;
		}

		public void setUserRequiredLevel(final int userLevel) {
			this.userRequiredLevel = userLevel;
		}

		public int getUserCurrentLevel() {
			return this.userCurrentLevel;
		}

		public void setUserCurrentLevel(final int userCurrentLevel) {
			this.userCurrentLevel = userCurrentLevel;
		}

		public Integer getUserNbAskedEvent() {
			return this.userNbAskedEvent;
		}

		public void setUserNbAskedEvent(final Integer userNbAskedEvent) {
			this.userNbAskedEvent = userNbAskedEvent;
		}

		public Integer getSubscriptionAllowedEvent() {
			return this.subscriptionAllowedEvent;
		}

		public void setSubscriptionAllowedEvent(final Integer subscriptionAllowedEvent) {
			this.subscriptionAllowedEvent = subscriptionAllowedEvent;
		}

		public Boolean isAccessAllowed() {
			return this.accessAllowed;
		}
	}
}
