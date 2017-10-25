/**
 *  Presence PI APP
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
    name: "Presence Pi APP",
    namespace: "DiegoAntonino",
    author: "Diego Antonino",
    description: "Trigger Presence/Away that has been detected by Raspberry-PI on Smartthings",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome@2x.png")


preferences {
	section("Connect to the Pi..."){
		input "thePi", "capability.bridge", title: "Which?", multiple: false, required: true
        input "theHub", "hub", title: "On which hub?", multiple: false, required: true
	}

    section("Presence Setup") {
        input "presenceName1", "text", title: "Presence 1 Name", required: false
        input "presenceName2", "text", title: "Presence 2 Name", required: false
        input "presenceName3", "text", title: "Presence 3 Name", required: false
        input "presenceName4", "text", title: "Presence 4 Name", required: false
        input "presenceName5", "text", title: "Presence 4 Name", required: false
    }

    section("TV - Switch Setup") {
        input "TvSwitchIp1", "text", title: "TvSwitch 1 IP", required: false
        input "TvSwitchIp2", "text", title: "TvSwitch 2 IP", required: false

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
  unsubscribe()
  def delete = getChildDevices()
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def initialize(){
    subscribe(thePi, "PresenceTrigger", presenceTrigger)
    subscribe(thePi, "SwitchTrigger", switchTrigger)



    if (presenceName1) {
        log.debug "create a presenceSensor named $presenceName1"
        def d = addChildDevice("DiegoAntonino", "Virtual Presence Sensor", "presencePi_" + presenceName1, theHub.id, [label: presenceName1, name: presenceName1])
    }
    if (presenceName2) {
        log.debug "create a presenceSensor named $presenceName2"
        def d = addChildDevice("DiegoAntonino", "Virtual Presence Sensor", "presencePi_" + presenceName2, theHub.id, [label: presenceName2, name: presenceName2])
    }
    if (presenceName3) {
        log.debug "create a presenceSensor named $presenceName3"
        def d = addChildDevice("DiegoAntonino", "Virtual Presence Sensor", "presencePi_" + presenceName3, theHub.id, [label: presenceName3, name: presenceName3])
    }
    if (presenceName4) {
        log.debug "create a presenceSensor named $presenceName4"
        def d = addChildDevice("DiegoAntonino", "Virtual Presence Sensor", "presencePi_" + presenceName4, theHub.id, [label: presenceName4, name: presenceName4])
    }
    if (presenceName5) {
        log.debug "create a presenceSensor named $presenceName5"
        def d = addChildDevice("DiegoAntonino", "Virtual Presence Sensor", "presencePi_" + presenceName5, theHub.id, [label: presenceName5, name: presenceName5])
    }
    if (TvSwitchIp1) {
        log.debug "create a TV - Switch  named $TvSwitchName1"
        def d = addChildDevice("DiegoAntonino", "Flask - Raspberry", "switchPi_" + TvSwitchIp1, theHub.id, [label: "TV-Switch 1", name: "TV-Switch 1"])
    }
    if (TvSwitchIp2) {
        log.debug "create a TV - Switch  named $TvSwitchName2"
        def d = addChildDevice("DiegoAntonino", "Flask - Raspberry", "switchPi_" + TvSwitchIp2, theHub.id, [label: "TV-Switch 2", name: "TV-Switch 2"])
    }




}


def presenceTrigger(evt){
    log.debug "got evt.value: ${evt.value}"
    def parts = evt.value.tokenize('.')
    def dev_presence = parts[0]
      
    def children = getChildDevices()
    log.debug "got children $children"

    def sensor = children.find{ d -> d.deviceNetworkId == "$dev_presence" }
    log.debug "got sensor $sensor"
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

def switchTrigger(evt){
    log.debug "got evt.value: ${evt.value}"
    def parts = evt.value.tokenize('.')
    def dev_presence = parts[0]

    def children = getChildDevices()
    log.debug "got children $children"

    def sensor = children.find{ d -> d.deviceNetworkId == "$dev_presence" }
    log.debug "got sensor $sensor"
    if (sensor) {
        switch(parts[1]) {
            case "on":
                sensor.on_state()
                break
            case "not off":
                sensor.off_state()
                break
        }
    }
}