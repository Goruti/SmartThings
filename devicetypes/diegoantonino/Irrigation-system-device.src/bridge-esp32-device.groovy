/**
 * Irrigation System Device
 *
 *  Copyright 2020 Diego Antonino
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
	//ESP32 Configuration
	input("esp32_mac", "string", title:"ESP32 MAC Address", required: False, displayDuringSetup: true)
}

metadata {
	definition (name: "Irrigation System Device", namespace: "DiegoAntonino", author: "Diego Antonino") {
        	capability "bridge"
            capability "Health Check"
            
            attribute "ip", "string"
            attribute "ssid", "string"
	}

	simulator {
    // TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		valueTile("ip", "device.ip", width: 2, height: 1) {
			state "default", label:'${currentValue}'
		}
		valueTile("ssid", "device.ssid", width: 2, height: 1) {
			state "default", label:'${currentValue}'
		}
		valueTile("total_pumps", "device.total_pumps", width: 2, height: 1) {
			state "default", label:'${currentValue}'
		}

		main('SSID')
		details(["SSID", "ip", "total_pumps"])
}

def installed() {
	updateSettings()
}

def updated() {
	updateSettings()
}

// NOP implementation of ping as health check only calls this for tracked devices
// But as capability defines this method it's implemented to avoid MissingMethodException
def ping() {
    log.info("unexpected ping call from health check")
    //Device send events every 5 minutes at most, this interval allows us to miss 2 events before marking offline.
    //TODO  checkInterval is in seconds 
	log.debug "Configured health checkInterval: ${7200} seconds (2hs)"
	sendEvent(name: "checkInterval", value: 7200, displayed: false)
}

def updateSettings(){
    setDeviceNetworkId(esp32_mac)
}

// ------------------------------------------------------------------

def parse(String description){
    def msg = parseLanMessage(description)
    def content = msg.json

    log.debug "received: ${content}"

    switch (content.type) {
        case "water_level_status":
            log.debug "trigger water_level_status event"
            sendEvent(name: "waterLevelStatus", value: "${content.body.status}")
            break
            
        case "moisture_status":
            log.debug "trigger moisture_status event"
            createEvent(name: "moistureStatus", value: "${content.body.sensors_status}")
            break

        case "system_configuration":
            log.debug "trigger system_configuration event"
            sendEvent(name: "ssid", value: "${content.body.ssid}")
            sendEvent(name: "ip", value: "${content.body.ip}")
            sendEvent(name: "total_pumps", value: "${content.body.system.total_pumps}")

            createEvent(name: "newSystemConfiguration", value: "${content.body.system}")
            break
            
        default:
            log.debug "event type '${content.type}' is not defined"
    }

}

// ------------------------------------------------------------------
// Helper methods
// ------------------------------------------------------------------

private setDeviceNetworkId(mac){
    device.deviceNetworkId = mac
    log.debug "<Device Handler> Device Network Id set to ${mac}"
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}