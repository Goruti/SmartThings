/**
 * Bridge PI Device
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
	//RPI Configuration
	input("raspberry_mac", "string", title:"Raspberry MAC Address", required: False, displayDuringSetup: true)
}

metadata {
	definition (name: "Bridge PI Device", namespace: "DiegoAntonino", author: "Diego Antonino") {
        	capability "bridge"
            
            attribute "temperature", "string"
            attribute "cpuPercentage", "string"
            attribute "memory", "string"
            attribute "diskUsage", "string"
            attribute "hubInfo", "enum", ["online", "offline"]

      		attribute "PresenceTrigger", "enum", ["present", "not present"]
            attribute "TvTrigger", "enum", ["on", "off"]
      		
	}

	simulator {
    // TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		standardTile("hubInfo", "device.hubInfo", width: 4, height: 4) {
			state "online", label:'${name}', backgroundColor: "#79b821", icon: "st.Electronics.electronics1"
        	state "offline", label:'${name}', backgroundColor: "#79b821", icon: "st.Electronics.electronics1"
		}
		valueTile("temperature", "device.temperature", width: 2, height: 1) {
			state "temperature", label:'${currentValue}°C\nCPU Temp', unit:"C"
		}
		valueTile("cpuPercentage", "device.cpuPercentage", width: 2, height: 1) {
			state "default", label:'${currentValue}%\nCPU Usage', unit:"Percent"
		}
		valueTile("memory", "device.memory", width: 2, height: 1) {
			state "default", label:'${currentValue} MB\nFree Memory', unit:"MB"
		}
		valueTile("diskUsage", "device.diskUsage", width: 2, height: 1) {
			state "default", label:'${currentValue}\nFree Disk', unit:"Percent"
    	}      
        
		main('hubInfo')
		details(["hubInfo", "temperature", "cpuPercentage", "memory" , "diskUsage"])
	}
}

def installed() {
	updateSettings()
}

def updated() {
	updateSettings()
}

def updateSettings(){
    setDeviceNetworkId(raspberry_mac)
    sendEvent(name: "hubInfo", value: "online")
}

// ------------------------------------------------------------------

def parse(String description){
    def msg = parseLanMessage(description)
    def content = msg.json

    log.debug "received: ${content}"

    switch (content.type) {
        case "rpi_status":
            log.debug "trigger rpi_status event"
            sendEvent(name: "temperature", value: "${content.body.temperature}")
            sendEvent(name: "cpuPercentage", value: "${content.body.cpuPercentage}")
            sendEvent(name: "memory", value: "${content.body.memory}")
            sendEvent(name: "diskUsage", value: "${content.body.diskUsage}")
            sendEvent(name: "hubInfo", value: "${content.body.hubInfo}")
            break
            
        case "alarm_status":
            createEvent(name: "AlarmTrigger", value: "${content.body.sensor_name}.${content.body.sensor_status}")
            break
            
        case "tv_status":
            log.debug "trigger TvTrigger event"
            def ip = content.body.device
            def dev_dni = convertIPtoHex(ip)
            createEvent(name: "TvTrigger", value: "${dev_dni}.${content.body.status}")
            break
        
        case "presence_status":
        	log.debug "trigger PresenceTrigger  event"
            createEvent(name: "PresenceTrigger", value: "${content.body.person}.${content.body.status}")
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