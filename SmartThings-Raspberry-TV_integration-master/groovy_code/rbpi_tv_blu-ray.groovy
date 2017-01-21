/**
 *  Flask - Raspberry - Lg_TV - LG_Blu-Ray
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
        input("raspberry_ip", "string", title:"Raspberry IP Address", required: true, displayDuringSetup: true)
        input("raspberry_port", "string", title:"Raspberry Port", required: true, displayDuringSetup: true)
        input("username", "string", title:"Username", required: true, displayDuringSetup: true)
        input("password", "password", title:"Password", required: true, displayDuringSetup: true)
        input("tv_ip", "string", title:"Tv IP Address", required: true, displayDuringSetup: true)
        input("blu_ray_ip", "string", title:"Blu-Ray IP Address", required: true, displayDuringSetup: true)
        
}

metadata {
  definition (name: "RbPi_LgTv_LgBluRay", namespace: "diegoantonino", author: "Diego Antonino") {
		capability "tv"
        capability "switch"
        
        command "on"
        command "off"
        command "tv_status"
        command "arrow_up"
        command "arrow_down"
        command "channel_up"
        command "channel_down"
        command "vol_up"
        command "vol_down"
        command "vol_mute"
        command "arrow_right"
        command "arrow_left"
        command "ok_botton"
        command "tv_input"
        command "tv_exit"
        command "back"
        
        //attribute "tv_status", "enum", ["On", "Off"]
  }

  simulator {
  }

  tiles(scale: 2) {
  	  standardTile("tv_status", "device.switch", inactiveLabel: true,  width: 6, height: 4) {
        state "off", label: '${name}', action:"on", icon: "st.Electronics.electronics18", backgroundColor: "#ff3f34"
        state "on", label: '${name}', action:"off", icon: "st.Electronics.electronics18", backgroundColor: "#79b821"
      }
      standardTile("refresh", "device.switch", inactiveLabel: true, decoration: "flat",  width: 6, height: 2) {
		state "default", action:"tv_status", icon:"st.secondary.refresh-icon"
	  }
      standardTile("arrow_up", "device.switch", inactiveLabel: true, decoration: "flat") {
		state "default", action:"arrow_up", icon: "st.thermostat.thermostat-up", backgroundColor: "#616A6B"
	  }
      standardTile("arrow_down", "device.switch", inactiveLabel: true, decoration: "flat") {
		state "default", action:"arrow_down", icon: "st.thermostat.thermostat-down", backgroundColor: "#616A6B"
	  }
      standardTile("arrow_right", "device.switch", inactiveLabel: true, decoration: "flat") {
		state "default", action:"arrow_right", icon: "st.thermostat.thermostat-right", backgroundColor: "#616A6B"
	  }
      standardTile("arrow_left", "device.switch", inactiveLabel: true, decoration: "flat") {
		state "default", action:"arrow_left", icon: "st.thermostat.thermostat-left", backgroundColor: "#616A6B"
	  }
      standardTile("channel_up", "device.switch", inactiveLabel: true, decoration: "flat") {
		state "default", action:"channel_up", icon: "st.thermostat.thermostat-up", backgroundColor: "#E67E22"
	  }
      standardTile("channel_down", "device.switch", inactiveLabel: true, decoration: "flat") {
		state "default", action:"channel_down", icon: "st.thermostat.thermostat-down", backgroundColor: "#E67E22"
	  }
      standardTile("vol_up", "device.switch", inactiveLabel: true, decoration: "flat") {
		state "default", action:"vol_up", icon: "st.thermostat.thermostat-up", backgroundColor: "#5499C7"
	  }
      standardTile("vol_down", "device.switch", inactiveLabel: true, decoration: "flat") {
		state "default", action:"vol_down", icon: "st.thermostat.thermostat-down", backgroundColor: "#5499C7"
	  }
      valueTile("ok_botton", "device.switch", inactiveLabel: true, decoration: "flat") {
		state "default", action:"ok_botton", label:"Ok", backgroundColor: "#BEBEBE"
	  }
      standardTile("tv_input", "device.switch", inactiveLabel: true, decoration: "flat") {
		state "default", action:"tv_input", label:"Input", icon:"st.Electronics.electronics6", backgroundColor: "#239B56"
	  }
      standardTile("tv_exit", "device.switch", inactiveLabel: true, decoration: "flat") {
		state "default", action:"tv_exit", label:"Exit", icon: "st.locks.lock.unlocked", backgroundColor: "#CB4335"
	  }
      valueTile("back", "device.switch", inactiveLabel: true, decoration: "flat") {
		state "default", action:"back", label:"Back", backgroundColor: "#BEBEBE"
	  }
      standardTile("empty", "device.switch", decoration: "flat") {
		state "default"
	  }
      standardTile("empty1x3", "device.switch", decoration: "flat", width: 1, height: 3) {
		state "default"
	  }
      valueTile("Channel", "device.switch", decoration: "flat") {
		state "default", label:"CH", backgroundColor: "#BEBEBE"
	  }
      standardTile("Volume", "device.switch", decoration: "flat") {
		state "default", action:"vol_mute", label:"mute", icon: "st.custom.sonos.muted", backgroundColor: "#F1C40F"
	  }

	  main('tv_status')
      details(["tv_status",
      "tv_input","arrow_up","empty","empty1x3","channel_up","vol_up",
      "arrow_left", "ok_botton", "arrow_right","Channel","Volume",
      "back","arrow_down","tv_exit","channel_down","vol_down","refresh"])
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
	setDeviceNetworkId(raspberry_ip,raspberry_port)
    tv_status()
}

def parse(String description){
    def msg = parseLanMessage(description)
    def error_code = msg.status
    def body = msg.json
    
    if (body) {
    	if (error_code == 200) {
        	log.debug  "Action has been executed"
            if(body.status && body.status != device.currentValue("switch")) {
                sendEvent(name: "switch", value: body.status)
                log.debug  "<Device Handler> tv_status: ${device.currentValue("switch")}"
            }
                           
			} else { log.debug "<Device Handler> ErrorCode:${error_code}, ErrorMessage:${body.error}" }
     
    	} else { log.debug "<Device Handler> ${msg}" }
}

def arrow_up(){
  //log.debug "<Device Handler> arrow_up"
    def body =  [command: 'control', action: "12", tv_ip: tv_ip]
	postAction(body)
}
def arrow_down(){
  //log.debug "<Device Handler> arrow_down"
    def body =  [command: 'control', action: "13", tv_ip: tv_ip]
	postAction(body)
}
def arrow_left(){
  //log.debug "<Device Handler> arrow_letf"
    def body =  [command: 'control', action: "14", tv_ip: tv_ip]
	postAction(body)
}
def arrow_right(){
  //log.debug "<Device Handler> arrow_right"
    def body =  [command: 'control', action: "15", tv_ip: tv_ip]
	postAction(body)
}
def ok_botton(){
  //log.debug "<Device Handler> ok_botton"
    def body =  [command: 'control', action: "20", tv_ip: tv_ip]
	postAction(body)
}
def back(){
  //log.debug "<Device Handler> back"
    def body =  [command: 'control', action: "23", tv_ip: tv_ip]
	postAction(body)
}
def channel_up(){
  //log.debug "<Device Handler> channel_up"
    def body =  [command: 'control', action: "27", tv_ip: tv_ip]
	postAction(body)
}
def channel_down(){
  //log.debug "<Device Handler> channel_down"
    def body =  [command: 'control', action: "28", tv_ip: tv_ip]
	postAction(body)
}
def tv_input(){
  //log.debug "<Device Handler> tv_input"
    def body =  [command: 'control', action: "47", tv_ip: tv_ip]
	postAction(body)
}
def tv_exit(){
  //log.debug "<Device Handler> tv_exit"
    def body =  [command: 'control', action: "412", tv_ip: tv_ip]
	postAction(body)
}
def vol_up(){
  //log.debug "<Device Handler> vol_up"
    def body =  [command: 'control', action: "vol_up", tv_ip: tv_ip, blu_ray_ip: blu_ray_ip]
	postAction(body)
}
def vol_down(){
  //log.debug "<Device Handler> vol_down"
    def body =  [command: 'control', action: "vol_down", tv_ip: tv_ip, blu_ray_ip: blu_ray_ip]
	postAction(body)
}
def vol_mute(){
  //log.debug "<Device Handler> vol_mute"
    def body =  [command: 'control', action: "vol_mute", tv_ip: tv_ip, blu_ray_ip: blu_ray_ip]
	postAction(body)
}

def on(){
//    sendEvent(name: "tv_status", value: "On")
    def body =  [command: 'on', tv_ip: tv_ip]
    postAction(body)
 }
def off(){
//    sendEvent(name: "tv_status", value: "Off")
    def body =  [command: 'off', tv_ip: tv_ip]
    postAction(body)
}

def tv_status(){
	log.debug "<Device Handler> Checking State"
  	def userpass = encodeCredentials(username, password)
  	def headers = getHeader(userpass)

  	def hubAction = new physicalgraph.device.HubAction(
        method: 'GET',
        path: '/homesweethome/api/v1.0/tv/status/'+tv_ip,
        headers: headers,
      )
    hubAction
}
// ------------------------------------------------------------------

private postAction(args){
  def userpass = encodeCredentials(username, password)
  def headers = getHeader(userpass)
  
  log.debug "<Device Handler> ${args}"
  def hubAction = new physicalgraph.device.HubAction(
    method: 'POST',
    path: '/homesweethome/api/v1.0/tv',
    headers: headers,
    body: args
  )
  hubAction
}

// ------------------------------------------------------------------
// Helper methods
// ------------------------------------------------------------------


private encodeCredentials(username, password){
    def userpass_base64 = "$username:$password".bytes.encodeBase64()
    def userpass = "Basic $userpass_base64"
    return userpass
}

private getHeader(userpass){
    def headers = [
      "HOST": "$raspberry_ip:$raspberry_port",
      "Authorization": userpass,
      "Content-Type": "application/json"
    ]
    return headers
}

private setDeviceNetworkId(ip,port){
    def iphex = convertIPtoHex(ip)
    def porthex = convertPortToHex(port)
    device.deviceNetworkId = "$iphex:$porthex"
    log.debug "<Device Handler> Device Network Id set to ${iphex}:${porthex}"
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
  String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}