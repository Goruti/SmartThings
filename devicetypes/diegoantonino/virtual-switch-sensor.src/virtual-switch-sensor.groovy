/**
 *  Copyright 2020 Diego Antonino
 *
 *  Provides a virtual switch with timer.
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
metadata {
    definition (name: "Virtual Timer Switch", namespace: "DiegoAntonino", author: "Diego Antonino", runLocally: true, minHubCoreVersion: '000.021.00001', executeCommandsLocally: true, mnmn: "SmartThings", vid: "generic-timer-switch") {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"
    }

    preferences {}

    tiles(scale: 2) {
        multiAttributeTile(name:"valueTile", type:"generic", width:6, height:4) {
            tileAttribute("device.timer", key: "PRIMARY_CONTROL") {
                attributeState "timer", label:'${currentValue}', defaultState: true
            }

            tileAttribute("device.switch", key: "SECONDARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOff", defaultState: true
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#FFFFFF", nextState:"turningOn"
                attributeState "turningOn", label:'...', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOn"
                attributeState "turningOff", label:'...', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#FFFFFF", nextState:"turningOff"
            }

            tileAttribute ("device.timer", key: "VALUE_CONTROL") {
                attributeState("VALUE_UP", action: "increaseTimer")
                attributeState("VALUE_DOWN", action: "decreaseTimer")
            }
        }

    }
}

def parse(String description) {
    // This is a simulated device. No incoming data to parse.
}

def on() {
    log.debug "turningOn"
    sendEvent(name: "switch", value: "on", isStateChange: true)
}

def off() {
    log.debug "turningOff"
    sendEvent(name: "switch", value: "off", isStateChange: true)
}

def setLevel(time_value, rate = null) {
    log.debug "setLevel: ${time_value}, this"
    sendEvent(name: "timer", value: time_value)
}

def increaseTimer() {
    def time = device.latestValue("timer") as Integer ?: 0
    time = time + 1
    setLevel(time)
}

def decreaseTimer() {
    def time = device.latestValue("level") as Integer ?: 0
    if (time > 0) {
        time = level - 1
    }
    setLevel(time)
}

def installed() {
    on()
}