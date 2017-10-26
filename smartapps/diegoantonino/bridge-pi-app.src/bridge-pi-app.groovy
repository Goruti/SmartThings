ls -lr/**
 * Bridge PI APP
 *
 *  Copyright 2016 Diego
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
definition(
    name: "Bridge Pi APP",
    namespace: "DiegoAntonino",
    author: "Diego Antonino",
    description: "Raspberry-PI on Smartthings",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome@2x.png")


preferences {
	section("Connect to ..."){
		input "theRPi", "capability.bridge", title: "Which?", multiple: false, required: true
        input "theHub", "hub", title: "On which hub?", multiple: false, required: true
	}

    section("Presence Setup", hideable: true, hidden: true) {
        input "presenceName1", "text", title: "Presence 1 Name", required: false
        input "presenceName2", "text", title: "Presence 2 Name", required: false
        input "presenceName3", "text", title: "Presence 3 Name", required: false
        input "presenceName4", "text", title: "Presence 4 Name", required: false
        input "presenceName5", "text", title: "Presence 4 Name", required: false
    }

    section("TV & Blu-ray Setup", hideable: true, hidden: true) {
        input("tv_ip", "string", title:"TV IP Address", required: false)
        input("blu_ray_ip", "string", title:"Rest Server Ip", required: false)
    }
    section("Flask Server Setup", hideable: true, hidden: true) {
        input("flask_ip", "string", title:"Rest Server Ip", required: false)
        input("flask_port", "string", title:"Rest Server Port", required: false)
        input("username", "string", title:"Rest Server Username", required: true)
		input("password", "password", title:"Rest Server Password", required: true)
    }
}

def installed()
{
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated()
{
	log.debug "Updated with settings: ${settings}"
	uninstalled()
	initialize()
	
}

def uninstalled() {
  def delete = getChildDevices()
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def initialize(){
    subscribe(theRPi, "PresenceTrigger", presenceTrigger)
    subscribe(theRPi, "TvTrigger", tvTrigger)



    if (presenceName1) {
        log.debug "create a presenceSensor named $presenceName1"
        addChildDevice("DiegoAntonino", "Virtual Presence Sensor", "virtual_beacon_" + presenceName1.toLowerCase(), theHub.id, [label: presenceName1, name: presenceName1])
    }
    if (presenceName2) {
        log.debug "create a presenceSensor named $presenceName2"
        addChildDevice("DiegoAntonino", "Virtual Presence Sensor", "virtual_beacon_" + presenceName2.toLowerCase(), theHub.id, [label: presenceName2, name: presenceName2])
    }
    if (presenceName3) {
        log.debug "create a presenceSensor named $presenceName3"
        addChildDevice("DiegoAntonino", "Virtual Presence Sensor", "virtual_beacon_" + presenceName3.toLowerCase(), theHub.id, [label: presenceName3, name: presenceName3])
    }
    if (presenceName4) {
        log.debug "create a presenceSensor named $presenceName4"
        addChildDevice("DiegoAntonino", "Virtual Presence Sensor", "virtual_beacon_" + presenceName4.toLowerCase(), theHub.id, [label: presenceName4, name: presenceName4])
    }
    if (presenceName5) {
        log.debug "create a presenceSensor named $presenceName5"
        addChildDevice("DiegoAntonino", "Virtual Presence Sensor", "virtual_beacon_" + presenceName5.toLowerCase(), theHub.id, [label: presenceName5, name: presenceName5])
    }

    if (tv_ip && blu_ray_ip && flask_ip && flask_port) {
        log.debug "create a TV2"
        def dni = convertIPtoHex(tv_ip)
        addChildDevice("DiegoAntonino", "Virtual TV Device", dni, theHub.id, [label: "TV2", name: "TV2", preferences: [tv_ip: tv_ip, blu_ray_ip: blu_ray_ip, flask_ip: flask_ip, flask_port: flask_port, username: username, password: password]])
    }

}


def presenceTrigger(evt){
    log.debug "got evt.value: ${evt.value}"
    def parts = evt.value.tokenize('.')
    String dev_presence = "virtual_beacon_" + parts[0].toLowerCase()
      
    def children = getChildDevices()
    log.debug "got children ${children}"

    def sensor = children.find{ d -> d.deviceNetworkId == "$dev_presence" }
    log.debug "got sensor ${sensor}"
    if (sensor) {
        switch(parts[1]) {
            case "present":
                sensor.present()
                break
            case "not present":
                sensor.away()
                break
        }
    }
}

def tvTrigger(evt){
    log.debug "got evt.value: ${evt.value}"
    def parts = evt.value.tokenize('.')
    def dni = parts[0]

    def children = getChildDevices()
    log.debug "got children ${children}"

    def sensor = children.find{ d -> d.deviceNetworkId == "$dni" }
    log.debug "got sensor ${sensor}"
    if (sensor) {
        switch(parts[1]) {
            case "on":
                sensor.on_state()
                break
            case "off":
                sensor.off_state()
                break
        }
    }
}

// ------------------------------------------------------------------
// Helper methods
// ------------------------------------------------------------------

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}