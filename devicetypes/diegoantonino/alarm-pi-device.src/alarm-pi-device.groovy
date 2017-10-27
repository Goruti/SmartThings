/**
 *  Alarm PI Device
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
	definition (name: "Alarm PI Device", namespace: "DiegoAntonino", author: "Diego Antonino") {
        	capability "bridge"
            capability "alarm"
            
            attribute "temperature", "string"
            attribute "cpuPercentage", "string"
            attribute "memory", "string"
            attribute "diskUsage", "string"
            attribute "hubInfo", "enum", ["online", "offline"]

      		attribute "AlarmTrigger", "string"
      		
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
    def error_code = msg.status
    def body = msg.json
    
    log.debug body    
    //createEvent(name: body.sensor_name, value: body.sensor_status)
    createEvent(name: "AlarmTrigger", value: "${body.sensor_name}.${body.sensor_status}")
}

private setDeviceNetworkId(mac){
    device.deviceNetworkId = mac
    log.debug "<Device Handler> Device Network Id set to ${mac}"
}