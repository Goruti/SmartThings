/**
 *  Illuminance Sensor TSL2561 connected to a PyCom (it used the RPI to check if it's online or not)
 *
 *  Copyright 2017 Diego Antonino
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
		input("pycom_mac", "string", title:"PyCom MAC", required: true, displayDuringSetup: true)
		input("pycom_ip", "string", title: "PyCom IP", required: false, displayDuringSetup: true)
		input("raspberry_ip", "string", title: "Raspberry-ip", required: false, displayDuringSetup: true)
		input("raspberry_port", "string", title: "Raspberry-port", required: false, displayDuringSetup: true)
		input("username", "string", title: "Username", required: false, displayDuringSetup: true)
		input("password", "password", title: "Password", required: false, displayDuringSetup: true)
}

metadata {
  definition (name: "Illuminance Sensor", namespace: "DiegoAntonino", author: "Diego Antonino") {
    capability "illuminanceMeasurement"
    
	attribute "status", "enum", ["online", "offline"]
	attribute "lux", "number"
  }

  simulator {
    // TODO: define status and reply messages here
  }

  tiles(scale: 2) {
	standardTile("status", "device.status", decoration: "flat", width: 6, height: 2) {
		state  "online", label:'${name}', action:"refresh", icon: "st.illuminance.illuminance.bright", backgroundColor: "#44b621"
		state  "offline", label:'${name}', action:"refresh", icon: "st.illuminance.illuminance.bright", backgroundColor: "#bc2323"
	}
	valueTile("lux", "device.lux", decoration: "flat", width: 6, height: 1) {
		state  "value", label:'${currentValue} lux'
	}

      main('status')
      details(["status", "lux"])
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
    setDeviceNetworkId(pycom_mac)
    sendEvent(name: "status", value: "online")
}

def parse(String description){
    def msg = parseLanMessage(description)
    def body = msg.json
    
    //log.debug "body", body
    
    if (body) {
        sendEvent(name: "lux", value: "${body.lux}")
    }
}

def refresh() {
    log.debug "<Device Handler> Checking State"
    def userpass = encodeCredentials(username, password)
    def headers = getHeader(userpass)

    def hubAction = new physicalgraph.device.HubAction(
            method: "GET",
            path: "/homesweethome/api/v1.0/device/status/${pycom_ip}",
            headers: headers,
    )

    try {
        return sendHubCommand(hubAction)
    } catch (Exception e) {
        log.debug "Hit Exception ${e} on ${hubAction}"
    }
}

// ------------------------------------------------------------------
// Helper methods
// ------------------------------------------------------------------

private encodeCredentials(username, password){
    def userpass_base64 = "${username}:${password}".bytes.encodeBase64()
    def userpass = "Basic ${userpass_base64}"
    return userpass
}

private getHeader(userpass){
    def headers = [
            "HOST": "${raspberry_ip}:${raspberry_port}",
            "Authorization": userpass,
            "Content-Type": "application/json"
    ]
    return headers
}

private setDeviceNetworkId(mac){
    device.deviceNetworkId = mac
    log.debug "<Device Handler> Device Network Id set to ${mac}"
}