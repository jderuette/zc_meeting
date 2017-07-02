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

#### Samples : 
- zc.meeting.error.deletingEvent
- zc.meeting.googleTooManyCall
