/**
 * Irrigation System APP
 *
 *  Copyright 2020 Diego
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
    name: "Irrigation System APP",
    namespace: "DiegoAntonino",
    author: "Diego Antonino",
    description: "Irrigation System on Smartthings",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome@2x.png")


preferences {
	section("Connect to ..."){
		input "irrigationSystemBridge", "capability.bridge", title: "Which?", multiple: false, required: true
        input "theHub", "hub", title: "On which hub?", multiple: true, required: true
	}

    section("Web Server Setup", hideable: true, hidden: true) {
        input("web_server_ip", "string", title:"Rest Server Ip", required: false)
        input("web_server_port", "string", title:"Rest Server Port", required: false)
        input("username", "string", title:"Rest Server Username", required: false)
		input("password", "password", title:"Rest Server Password", required: false)
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
	initNewConfig()
    initSystem()
}

//############## NEW CONFIGURATION SETUP #############
def initNewConfig() {
	//subscribe
    subscribe(irrigationSystemBridge, "newSystemConfiguration", newConfigurationTrigger)
}

//define new configuration trigger
def newConfigurationTrigger(evt){
    log.debug "got evt.value: ${evt.value}"
    uninstalled()
    //add childs
    //addChildDevice("DiegoAntonino", "Virtual Presence Sensor", "virtual_beacon_" + presenceName1.toLowerCase(), theHub.id, [label: presenceName1, name: presenceName1])

}

//############## TV SETUP #############
def initTv() {
    if (tv_ip && blu_ray_ip && flask_ip && flask_port) {
        //subscribe
        subscribe(theRPi, "TvTrigger", tvTrigger)
        
        //add childs
        log.debug "create a TV-Device"
        def dni = convertIPtoHex(tv_ip)
        addChildDevice("DiegoAntonino", "Virtual TV Device", dni, theHub.id, [label: "TV", name: "TV", preferences: [tv_ip: tv_ip, blu_ray_ip: blu_ray_ip, flask_ip: flask_ip, flask_port: flask_port, username: username, password: password]])
    }
}

// define tv trigger
def tvTrigger(evt) {
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

//############## ALARM SETUP ############3
def initAlarm() {
	if ( (zoneName1 && zoneType1) || (zoneName2 && zoneType2) || (zoneName3 && zoneType3) || (zoneName4 && zoneType4) ) {
    	//subscribe
        subscribe(theRPi, "AlarmTrigger", alarmTrigger)
        
        //add childs
        if (zoneName1 && zoneType1) {
            log.debug "create a $zoneType1 named $zoneName1"
            def d = addChildDevice("DiegoAntonino", "Virtual " + zoneType1, "zone01", theHub.id, [label:zoneName1, name:zoneType1])
        }
        if (zoneName2 && zoneType2) {
            log.debug "create a $zoneType2 named $zoneName2"
            def d = addChildDevice("DiegoAntonino", "Virtual " + zoneType2, "zone02", theHub.id, [label:zoneName2, name:zoneType2])
        }
        if (zoneName3 && zoneType3) {
            log.debug "create a $zoneType3 named $zoneName3"
            def d = addChildDevice("DiegoAntonino", "Virtual " + zoneType3, "zone03", theHub.id, [label:zoneName3, name:zoneType3])
        }
        if (zoneName4 && zoneType4) {
            log.debug "create a $zoneType4 named $zoneName4"
            def d = addChildDevice("DiegoAntonino", "Virtual " + zoneType4, "zone04", theHub.id, [label:zoneName4, name:zoneType4])
        }
    }
}

//define alarm trigger
def alarmTrigger(evt){
      log.debug "got evt.value: ${evt.value}"
      def parts = evt.value.tokenize('.')
      def zone = parts[0]
      
      def children = getChildDevices()
      def sensor = children.find{ d -> d.deviceNetworkId == "$zone" }
        log.debug "got sensor $sensor"
        if (sensor) {
              switch(parts[1]) {
                    case "open":
                        sensor.open()
                        break
                    case "close":
                        sensor.close()
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