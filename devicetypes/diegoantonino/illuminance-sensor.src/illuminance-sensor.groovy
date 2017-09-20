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
    input("pycom_mac", "string", title: "PyCom-mac", required: true, displayDuringSetup: true)
    input("pycom_ip", "string", title: "PyCom-ip", required: false, displayDuringSetup: true)
    input("raspberry_ip", "string", title: "Raspberry-ip", required: false, displayDuringSetup: true)
    input("raspberry_port", "string", title: "Raspberry-port", required: false, displayDuringSetup: true)
    input("username", "string", title: "Username", required: false, displayDuringSetup: true)
    input("password", "password", title: "Password", required: false, displayDuringSetup: true)
}

metadata {
    definition (name: "Illuminance Sensor", namespace: "DiegoAntonino", author: "Diego Antonino") {
        capability "illuminanceMeasurement"
        capability "Refresh"

        command "refresh"

        attribute "PyComInfo", "enum", ["online", "offline"]
        attribute "lux", "number"
        attribute "ir_light", "number"
        attribute "total_light", "number"
        attribute "visible_light", "number"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles(scale: 2) {
        standardTile("PyComInfo", "device.PyComInfo", width: 6, height: 2) {
            state "online", label:'${name}', action:"refresh", backgroundColor: "#44b621", icon: "st.Electronics.electronics1"
            state "offline", label:'${name}', action:"refresh",  backgroundColor: "#bc2323", icon: "st.Electronics.electronics1"
        }
        valueTile("lux", "device.lux", decoration: "flat", width: 6, height: 1) {
            state "lux", label:'${currentValue} Lux'
        }
        valueTile("ir_light", "device.ir_light", decoration: "flat", width: 2, height: 1) {
            state "ir_light", label:'${currentValue}\nIR'
        }
        valueTile("total_light", "device.total_light", decoration: "flat", width: 2, height: 1) {
            state "total_light", label:'${currentValue}\nTotal'
        }
        valueTile("visible_light", "device.visible_light", decoration: "flat", width: 2, height: 1) {
            state "visible_light", label:'${currentValue}\nVisible'
        }

        main("PycomInfo")
        details(["PyComInfo",
        "lux", 
        "ir_light", "total_light", "visible_light"])
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
    //runEvery5Minutes(refresh())
}

def parse(String description) {
    def msg = parseLanMessage(description)
    def body = msg.json

    log.debug body

    if (body) {
        sendEvent(name: "lux", value: "${body.lux}")
        sendEvent(name: "ir_light", value: "${body.ir_light}")
        sendEvent(name: "total_light", value: "${body.total_light}")
        sendEvent(name: "visible_light", value: "${body.visible_light}")
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