/**
 *  Presence PI
 *
 *  Copyright 2016 Diego Antonino
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
preferences {
        input("raspberry_mac", "string", title:"Raspberry MAC Address", required: False, displayDuringSetup: true)
}

metadata {
  definition (name: "Presence PI", namespace: "DiegoAntonino", author: "Diego Antonino") {
      capability "bridge"

      attribute "PresenceTrigger", "string"
      attribute "SwitchTrigger", "string"


      attribute "hubInfo", "enum", ["online", "offline"]
  }

  simulator {
    // TODO: define status and reply messages here
  }

  tiles(scale: 2) {
      standardTile("hubInfo", "device.hubInfo", width: 6, height: 4) {
        state "online", label:'${name}', backgroundColor: "#79b821", icon: "st.Electronics.electronics1"
        state "offline", label:'${name}', backgroundColor: "#79b821", icon: "st.Electronics.electronics1"
      }

      main('hubInfo')
      details(["hubInfo"])
  }
}

def installed() {
	updateSettings()
}

def updated() {
	updateSettings()
}
// ------------------------------------------------------------------
def updateSettings(){
    setDeviceNetworkId(raspberry_mac)
    sendEvent(name: "hubInfo", value: "online")
}

def parse(String description){
    def msg = parseLanMessage(description)
    def error_code = msg.status
    def content = msg.json

    log.debug content

    switch (content.type) {
        case "presence_status":
        	log.debug "trigger event"
            createEvent(name: "PresenceTrigger", value: "${content.body.person}.${content.body.status}")
            break
        case "switch_status":
            log.debug "trigger event"
            createEvent(name: "SwitchTrigger", value: "${content.body.ip}.${content.body.status}")
            break
        default:
            log.debug "event type '${content.type}' is not defined"
    }

}

private setDeviceNetworkId(mac){
    device.deviceNetworkId = mac
    log.debug "<Device Handler> Device Network Id set to ${mac}"
}