/**
 *  Irrigation System Device
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
 */
metadata {
	//definition (name: "Irrigation System Device", namespace: "DiegoAntonino", author: "Diego Antonino", cstHandler: true, mnmn: "SmartThings", vid: "generic-switch") {
	definition (name: "Irrigation System Device", namespace: "Goruti", author: "Diego Antonino", cstHandler: true) {
		capability "Switch"
        capability "Actuator"
        capability "Health Check"
        capability "Refresh"
        
	}
  
	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
        multiAttributeTile(name: "switch", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff"
			}
		}

        main(["switch"])
        details(["switch"])
    }
}

def installed() {
	updateSettings()
    sendEvent(name: "switch", value: "off", displayed: false)
}

def updated() {
	updateSettings()
}

def updateSettings(){
    log.debug "Configured health checkInterval: ${2*60*60} seconds (2hrs) "
    sendEvent(name: "checkInterval", value: 7200, displayed: false, unit: "s")
    //sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
    //sendEvent(name: "DeviceWatch-Enroll", value: groovy.json.JsonOutput.toJson([protocol: "LAN", scheme:"untracked", hubHardwareId: "${device.hub.hardwareID}"]), displayed: false)
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'switch' attribute
	// TODO: handle 'valve' attribute
}

def refresh(){
	log.info("calling refresh from child device: ${device.deviceNetworkId}")
	parent?.refresh()
}

def ping() {
    log.error("unexpected ping call from health check")
    parent?.refresh()
}

def on() {
	log.debug "Executing 'on'"
    sendEvent(name: "switch", value: "on", isStateChange: true)
	parent?.startIrrigation()
}

def off() {
	log.debug "Executing 'off'"
	sendEvent(name: "switch", value: "off", isStateChange: true)
}