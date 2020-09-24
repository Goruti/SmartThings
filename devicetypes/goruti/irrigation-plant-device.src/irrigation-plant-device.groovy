/**
 *  Irrigation Plant Device
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
	definition (name: "Irrigation Plant Device", namespace: "Goruti", author: "Diego Antonino", cstHandler: true) {
		capability "Relative Humidity Measurement"
		capability "Switch"
        capability "Sensor"
        capability "Refresh"
        capability "Health Check"
    
    	attribute "lastActivity", "string"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
            multiAttributeTile(name: "switch", width: 6, height: 4, canChangeIcon: true) {
                tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                    attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00a0dc"
                    attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff"
                }
            }
            
			valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
                state "humidity", label: '${currentValue}% humidity', unit: ""
            }
            
            standardTile("lastActivity", "device.lastActivity", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
                state "default", label: 'Last Activity: ${currentValue}',icon: "st.Health & Wellness.health9"
            }

            main "humidity"
            details(["humidity", "switch", "lastActivity"])
	}
}

def installed() {
	updateSettings()
    sendEvent(name: "switch", value: "off", isStateChange: true)
}

def updated() {
	updateSettings()
}

def refresh(){
	log.info("calling refresh from child device: ${device.deviceNetworkId}")
	parent?.refresh()
}

def ping() {
    log.error("unexpected ping call from health check")
	parent?.refresh()
}

def updateSettings(){
    log.debug "Configured health checkInterval: ${2*60*60} seconds (2hrs) "
    sendEvent(name: "checkInterval", value: 7200, displayed: false, unit: "s")
    //sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
    //sendEvent(name: "DeviceWatch-Enroll", value: groovy.json.JsonOutput.toJson([protocol: "LAN", scheme:"untracked", hubHardwareId: "${device.hub.hardwareID}"]), displayed: false)
}

def on() {
	log.debug "send on"
    sendData("on")
}

def off() {
	log.debug "send off"
    sendData("off")
}

def sendData(String value) {
    def name = device.deviceNetworkId.split("-")[-1]
    parent?.sendPumpAction("${name}:${value}")  
}

def parse(String description) {
	log.debug "parse called: ${description}"
    def events_in = description.split(",")
    
    events_in?.each { event ->
    	def parts = event.split(":")
    	def name  = parts.length>0?parts[0].trim():null
        def value = parts.length>1?parts[1].trim():null
        
        if (name && value) {
        	if (name == "humidity") {
            	value.round(2)
            }
            if (device.currentValue(name) != value) {
            	// Update device
                log.debug "Updated Attribute: name: ${name}, value: ${value}"
                sendEvent(name: name, value: value, isStateChange: true)
            }
    	}
    }
    
    def ts = getReceivedTs()
    log.debug "Updated Attribute: name: lastActivity, value: ${ts}"
    sendEvent(name: "lastActivity", value: ts, displayed:false)
}

def getReceivedTs() {
	if(location.timeZone) {
    	return new Date().format("yyyy-MM-dd h:mm:ss a z", location.timeZone)
    }
    else {
    	return new Date().format("yyyy-MM-dd h:mm:ss a z")
    }
}