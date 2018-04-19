# 0Click meeting
0Click meeting **Work In progress**


## IDE used
Eclipse Neon.3 Release (4.6.3) Build id: 20170314-1500.
With : 
- Scout SDK (6.0.300.RC2)

## Informations about translation
Translation are managed by the standard [Scout NLS editor](https://eclipsescout.github.io/6.0/technical-guide.html#texts).
File is located at the default location meeting.shared/texts.nls (*to find it use ctrl+shift+R (opens resources) and search for "texts.nls"*)

### Key conventions
Keys for text have this geenral format : {org_shortcut}.{module}.{identifier}.
- org : always "zc" for ZeroClick
- module : 
  - meeting (business logic form meeting/event module)
  - user (user management : users, roles, permissions)
  - api (connection with other API)
  - common (other topic)
- identifier : free text to identify the key, may contains . (dot)

#### Samples 
- zc.meeting.error.deletingEvent
- zc.meeting.googleTooManyCall

## Instalation informations
Mostly a standard [Scout application](https://eclipsescout.github.io/6.0/technical-guide.html#overview). Fro standard information about [Eclipse scout installations](https://eclipsescout.github.io/6.0/beginners-guide.html#apx-install_scout).

### ZeroClick specific requirements
Most specific configuration ca be visualiser throught Config.properties files. ${UPPER_CASE_INDETIFIER} means an System/environnement varaibles is required.

| ID | alias |Example | Location | Description | Default |
| -- | ----- | ------- | -------- | ----------- | ------- |
| ZEROCLICK_BDD_LOGIN | org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#username | mylogin | Server | The login to connect to DataBase | (empty) |
| ZEROCLICK_BDD_PASS | org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService#password | The_password | Server | The password to connect to DataBase | (empty) |
| ZEROCLICK_BDD_MAPPING | zeroclick.database.jdbc.mapping.name | jdbc:postgresql://127.0.0.1:5432/zcdev | Server | Mapping to conenct to the DataBase | jdbc:derby:memory:zeroclick-database |
| ZEROCLICK_SERVER_URL | scout.server.url | http://localhost:8080 | Client | BackEnd server URL use by UI servers | (empty) |
| ZEROCLICK_SERVER_URL_PROTOCOL | zeroclick.server.url.protocol | http | Client | BackEnd server protocol use by UI servers | (empty) |
| ZEROCLICK_URL | zeroclick.url | http://localhost:8080 | Client | Use to build full URL (for links in emails , ...) | http://localhost:8082 |
| ZEROCLICK_URL_PROTOCOL | zeroclick.url.protocol | http | Client | Use to build full URL (for links in emails , ...) | (empty) |
| ZEROCLICK_ENV | zeroclick.env | test, prod, local_dev1 | Client | used to inform the curent environnement kind | Local |
| MAILJET_USER |  zeroclick.mail.auth.user | 4f08eb4e3a2c9c2gt72f1e45f8c818dd | Client | Api user ID for Mailjet | (empty) |
| MAILJET_PASS |  zeroclick.mail.auth.password | c836bcbd553284320bf410fbc370a6d3 | Client | APi password for Mailjet | (empty) |
| ZEROCLICK_USER_AREA_UI | user.area | /var/zeroclick or ${user.home}/org.zeroclick.meeting.html.ui.dev | Client | Local storage for Scout Files, and root folder for other application specific files | (empty) |
| ZEROCLICK_USER_AREA_SRV | user.area | /var/zeroclick or ${user.home}/org.zeroclick.meeting.server.dev | Server | Local storage for Scout Files, and root folder for other application specific files | (empty) |
| ZEROCLICK_API_GOOGLE_STORAGE_DIR | contacts.api.google.user.storage.dir | ${user.area}/GoogleUserStorage | Client | Local Storage for Google User OAuth data | (empty) |
| ZEROCLICK_API_GOOGLE_AUTH_FILE | contacts.api.google.client.auth.file | ${user.area}/GoogleClientStorage/client_secret_zeroclick_dev.json | Client | Your (Google) app credential file | (empty) |
| ZEROCLICK_MAIL_FROM | zeroclick.mail.from | bob358@someProvider.com | Client | Mail who send email | admin@0click.org |
| ZEROCLICK_MAIL_FROM_NAME | zeroclick.mail.from.name | Bob from 0Click | Client | Name display in mail client for the sender | admin |
| ZEROCLICK_MAIL_BCC | zeroclick.mail.bcc | suport@someProvider.com | Client | in not empty will be bcc for all mail **Deprecated** use app parameter "bcc.support.email" instead | (empty) |
| ZEROCLICK_AUTH_PRIVATE_KEY | scout.auth.privatekey | MD4CAQAwEAYHKoZIzj0CAQYFK4EEAAoEJzAlAgEBBCCd+rjJjsljL61yQLposwb9wh+3cWmpttZgPUUFrRtR+w== | Client | private key (see SecurityUtility) | (empty) |
| ZEROCLICK_AUTH_PUBLIC_KEY | scout.auth.publickey | MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEs2qqfky/9JpMzt1T5dg79QXFborYQ2Y7B45e1k/qGQboXY4oSAON2P6ijpnyPtaDhfp962ua9OomsbM9mSElIA== | Server | public key (see SecurityUtility) | (empty) |
| ZEROCLICK_API_MICROSOFT_CLIENT_ID | contacts.api.microsoft.client.id | feaa1131-18df-41cc-a693-c88c430167d3 | Client | Microsoft client ID | (empty) |
| ZEROCLICK_API_MICROSOFT_CLIENT_SECRET | contacts.api.microsoft.client.secret | sdf455$çsdfsz | Client | Microsoft client password | none |
| ZEROCLICK_API_MICROSOFT_CLIENT_URL | contacts.api.microsoft.callback.url | /api/microsoft/oauth2callback | Client | Internal ul to get Tokens | http://localhost:8082/api/microsoft/oauth2callback |
