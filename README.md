Urban Search & Rescue(USAR)
Brief Project Description:
Urban Search and Rescue App is a basic mobile tool for disaster management. The
basic function of this app is that it can be utilized to track and share locations of emergency
responders, victims and other resources on a disaster scene in real-time through network.
The app will update the real time locations and other situational information onto a server
using POST, so that all rescue teams can track the latest information and progress through
GET.
In addition, this app will assist to create tags (victim markers and FEMA markers) for
the victims, buildings and public infrastructures. Each responder team is able to create all
kinds of tags of any location. The tags will contain some significant information such as
triage level, time and date, location, potential hazards, supplementary description and
rescue team identifiers. All responder teams should be able to track all the tags and
information in real-time.
Thirdly, When a FEMA tag is created, a notification will broadcast to every responder
team, whilst every responder team can request help and broadcast this information.
Finally, each responder can search nearby victims with certain triage, and display the
search results on Google map only to distinguish from other mingled tags.

Based on the requirements analysis, the application’s components, modules, interfaces
and interactions have been designed. The following diagram describes the system
architecture of this application.
The application is a multi-user real time android disaster management application that
follows a two-thread architecture with interaction between them. The user input is a mixed
action of touch event captured by the main UI thread and data input by keyboard. The main
UI thread will display a Google map interface and a bunch of markers, which added by all
users. Information of each marker will be displayed as requested. To avoid long term block
or data collision in main UI thread, we propose a server thread, which mainly function is to
perform as a server manager to manage all server data uploading, data downloading and
data updating to main UI thread.

