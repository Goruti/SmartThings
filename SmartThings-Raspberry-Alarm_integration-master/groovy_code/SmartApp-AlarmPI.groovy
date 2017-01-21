/**
 *  Alarm PI APP
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
        name: "Alarm Pi APP",
        namespace: "DiegoAntonino",
        author: "Diego Antonino",
        description: "Trigger alarms that has been detected by Raspberry-PI on Smartthings",
        category: "Convenience",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome@2x.png",
        iconX3Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome@2x.png")


preferences {
    section("Connect to the alarm..."){
        input "theAlarm", "capability.alarm", title: "Which?", multiple: false, required: true
        input "theHub", "hub", title: "On which hub?", multiple: false, required: true
    }

    section("Zones Setup") {
        input "zoneName1", "text", title: "Zone 1 Name", required:false
        input "zoneType1", "enum", title: "Zone 1 Kind", required:false, metadata: [ values: ['Motion Sensor','Contact Sensor'] ]

        input "zoneName2", "text", title: "Zone 2 Name", required:false
        input "zoneType2", "enum", title: "Zone 2 Kind", required:false, metadata: [ values: ['Motion Sensor','Contact Sensor'] ]

        input "zoneName3", "text", title: "Zone 3 Name", required:false
        input "zoneType3", "enum", title: "Zone 3 Kind", required:false, metadata: [ values: ['Motion Sensor','Contact Sensor'] ]

        input "zoneName4", "text", title: "Zone 4 Name", required:false
        input "zoneType4", "enum", title: "Zone 4 Kind", required:false, metadata: [ values: ['Motion Sensor','Contact Sensor'] ]
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
    subscribe(theAlarm, "AlarmTrigger", alarmTrigger)

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