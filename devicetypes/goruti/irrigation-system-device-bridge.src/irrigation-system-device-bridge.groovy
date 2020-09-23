/**
 * Irrigation System Device Bridge
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
	input("esp32_mac", "string", title:"ESP32 MAC Address", description: "MAC Address in form of 02A1B2C3D4E5", required: False, displayDuringSetup: true)
}

metadata {
	definition (name: "Irrigation System Device Bridge", namespace: "Goruti", author: "Diego Antonino", cstHandler: true) {
        	capability "bridge"
            capability "Refresh"
            capability "Health Check"
            
            attribute "ip", "string"
            attribute "ssid", "string"
            attribute "total_pumps", "string"
	}

	simulator {
    // TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		valueTile("ip", "device.ip", width: 2, height: 1) {
			state "default", label:'${currentValue}'
		}
        valueTile("port", "device.ip", width: 2, height: 1) {
			state "default", label:'${currentValue}'
		}
		valueTile("ssid", "device.ssid", width: 2, height: 1) {
			state "default", label:'${currentValue}'
		}
		valueTile("total_pumps", "device.total_pumps", width: 2, height: 1) {
			state "default", label:'${currentValue}'
		}
        
		main('SSID')
		details(["SSID", "ip", "port", "total_pumps"])
	}
}

def installed() {
	updateSettings()
}

def updated() {
	updateSettings()
    refresh()
}

def refresh() {
	log.debug "Executing 'refresh()'"
    def params = [
    	method: "GET",
        path: "/",
    ]
    sendEthernet(params)
}

def uninstalled() {
  deleteAllChildDevices()
}


def ping() {
    log.info("unexpected ping call from health check")
	refresh()
}

// ------------------------------------------------------------------
def updateSettings(){
	updateDeviceNetworkID(esp32_mac)
    //Device send events every 1 hs at most, this interval allows us to miss 2 events before marking offline.
    //  checkInterval is in seconds 
    log.debug "Configured health checkInterval: ${2*60*60} seconds (2hrs) "
    sendEvent(name: "checkInterval", value: 7200, displayed: false)
    //sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
    //sendEvent(name: "DeviceWatch-Enroll", value: groovy.json.JsonOutput.toJson([protocol: "LAN", scheme:"untracked", hubHardwareId: "${device.hub.hardwareID}"]), displayed: false)

    }

// ------------------------------------------------------------------
def parse(String description){    
    def msg = parseLanMessage(description)
    def content = msg.json

    log.debug "received: ${content}"

    switch (content.type) {
        case "water_level_status":
            update_water_status(content.body)
            break
            
        case "moisture_status":
            update_status(content.body, "humidity")
            break
       
        case "pump_status":
            update_status(content.body, "switch")
            break

        case "system_configuration":
            log.debug "trigger system_configuration event: ${content.body}"
            if (content.body.status == "enabled") {
                sendEvent(name: "ssid", value: "${content.body.ssid}")
                sendEvent(name: "ip", value: "${content.body.ip}")
                sendEvent(name: "port", value: "80")
                sendEvent(name: "total_pumps", value: "${content.body.system.total_pumps}")
                createChildDevices(content.body.system)
                // Refresh system status
       			refresh()
            } else {
                log.debug "Disable ST integration. Deleting Child Devices"
                uninstalled()
            }
            break
        case "refresh":
        	refresh_devices(content.body)
            break
        case "system_test":
            strat_irrigation_update(content.body)
            break
        default:
            log.error "event type '${content.type}' is not defined"
    }

}

// ------------------------------------------------------------------
def strat_irrigation_update(status) {
	log.debug "trigger strat_irrigation_update event: ${status}"
    def systemDevice = childDevices.find{ d -> d.deviceNetworkId == "${device.deviceNetworkId}-systemdevice"}
    
    if (systemDevice) {
        systemDevice.off()
    }
    else {
    	log.error "no child device found: ${device.deviceNetworkId}-systemdevice"
		log.error "childDevices: ${childDevices}"
    }
}
// ------------------------------------------------------------------
def refresh_devices(status) {
	log.debug "trigger refresh_devices event: ${status}"
    
    // Update Virtual Pant Sensors/Pumps
    status.pump_info.each { key, value ->
    	def plantDevice = childDevices.find{ d -> d.deviceNetworkId == "${device.deviceNetworkId}-${value.connected_to_port}"}
		
        if (plantDevice) {
        	plantDevice.parse("humidity:${value.humidity}, switch:${value.pump_status}")
		}
        else {
        	log.error "no child device found: ${device.deviceNetworkId}-${value.connected_to_port}"
            log.error "childDevices: ${childDevices}"
        }
    }
    
    // Update Virtual Water Tank    
    def waterTankDevice = childDevices.find{ d -> d.deviceNetworkId == "${device.deviceNetworkId}-watertank"}
    if (waterTankDevice) {
        def level_status = status.water_level.replaceAll("good", "wet").replaceAll("empty", "dry")
        waterTankDevice.parse("water:${level_status}")
    }
    else {
    	log.error "no child device found: ${device.deviceNetworkId}-watertank"
		log.error "childDevices: ${childDevices}"
    }
}


// ------------------------------------------------------------------
def update_status(status, type) {
	log.debug "trigger update_status status: ${status}, type: ${type}"
	status.each { key, value ->
    	def plantDevice = childDevices.find{ d -> d.deviceNetworkId == "${device.deviceNetworkId}-${key}"}
		if (plantDevice) {
        	plantDevice.parse("${type}:${value}")
		}
        else {
        	log.error "no child device found: ${device.deviceNetworkId}-${key}"
            log.error "childDevices: ${childDevices}"
        }
    }
}

// ------------------------------------------------------------------
def update_water_status(level) {
	log.debug "trigger water_level_status event: ${level}"
    def waterTankDevice = childDevices.find{ d -> d.deviceNetworkId == "${device.deviceNetworkId}-watertank"}
    if (waterTankDevice) {
    	def level_status = level.status.replaceAll("good", "wet").replaceAll("empty", "dry")
    	waterTankDevice.parse("water:${level_status}")
    }
    else {
    	log.error "no child device found: ${device.deviceNetworkId}-watertank"
		log.error "childDevices: ${childDevices}"
    }
}

// ------------------------------------------------------------------
def createChildDevices(body) {
	log.debug "creating Childs ${body}"
    
    if ( device.deviceNetworkId =~ /^[A-Z0-9]{12}$/) {
       
       def childrens = []
       
       // Create Irrigation Water Tank Device
       def deviceHandlerName = "Irrigation Water Tank Device"
       log.debug "creating Childs ${deviceHandlerName}"
       try {
       		childrens.add(addChildDevice(deviceHandlerName, "${device.deviceNetworkId}-watertank", null,
                        [completedSetup: true, label: "Irrigation Water Tank", 
                        isComponent: false]))
       } catch (e) {
           log.error "'Water Tank' child device creation of type '${deviceHandlerName}' failed with error = ${e}"
       }
       
       // Create Irrigation System Device
       deviceHandlerName = "Irrigation System Device"
       log.debug "creating Childs ${deviceHandlerName}"
       try {
       		childrens.add(addChildDevice(deviceHandlerName, "${device.deviceNetworkId}-systemdevice", null,
                        [completedSetup: true, label: "Irrigation System Device", 
                        isComponent: false]))
       } catch (e) {
           log.error "'System Irrigation Device' child device creation of type '${deviceHandlerName}' failed with error = ${e}"
       }
       
       // Create Irrigation Plant Devices
       deviceHandlerName = "Irrigation Plant Device"
       log.debug "creating Childs ${deviceHandlerName}"
       body.pump_info.each { key, value ->
			try {
            	childrens.add(addChildDevice(deviceHandlerName, "${device.deviceNetworkId}-${value.connected_to_port}", null,
                        [completedSetup: true, label: "Irrigation Plant ${value.connected_to_port}", 
                        isComponent: false]))

            } catch (e) {
            	log.error "'Plant Device ${key}' child device creation of type '${deviceHandlerName}' failed with error = ${e}"
            } 
       }
       
       return childrens
   
	}
    else {
        log.error "Please configure Network ID on the Irrigation Bridge Device. Click the 'Gear' icon, enter data for all fields, and click 'Done'"
    }
}

// ------------------------------------------------------------------
def deleteAllChildDevices() {
	log.debug "Deleting Child Devices"    
    childDevices.each { dev ->
    	try {
            deleteChildDevice(dev.deviceNetworkId)
        } catch (e) {
            log.error "SmartThings may have issues trying to delete the child device when it is in use. Need to manually delete them."
        }
    }
}

// ------------------------------------------------------------------
// this funtion is called from the childs (Plant Device)
def sendPumpAction(message) {
	log.debug "seding a pump action ${message}"
    
	def parts = message.split(":")
    def port = parts[0].trim()
    def action = parts[1].trim()
    def params = [
    	method: "GET",
        path: "/pump_action?pump=${port}&action=${action}",
    ]
    sendEthernet(params) 
}

// ------------------------------------------------------------------
// this funtion is called from the childs (System Irrigation Device)
def startIrrigation() {
	log.debug "seding a system action"
    def params = [
    	method: "GET",
        path: "/test_system",
    ]
    sendEthernet(params) 
}
// ------------------------------------------------------------------
private getHeader(){    
    def headers = [
      "HOST": "${device.currentValue("ip")}:${device.latestValue("port")?:"80"}",
      "Content-Type": "application/json"
    ]
    return headers
}

// ------------------------------------------------------------------
def sendEthernet(params) {
	log.debug "Executing 'sendEthernet' ${params}"
    def ip = device.currentValue("ip")
    def port = device.latestValue("port")?:"80"
    
	if (ip != null && port != null) {
    	params["headers"] = getHeader()
        sendHubCommand(new physicalgraph.device.HubAction(params))
    }
    else {
        log.error "Bridge Device has not yet been fully configured"
    }
}

// ------------------------------------------------------------------
def updateDeviceNetworkID(mac) {
	log.debug "Executing 'updateDeviceNetworkID'"
    if(mac) {
        def formattedMac = mac.toUpperCase()
        formattedMac = formattedMac.replaceAll(":", "")
        if(device.deviceNetworkId != formattedMac) {
            log.debug "setting deviceNetworkID = ${formattedMac}"
            device.setDeviceNetworkId("${formattedMac}")
        }
    }
    else {
        log.error "Bridge Device has not yet been fully configured"
    }
    
}