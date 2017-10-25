from flask import Flask, request, json
import requests
from flask_httpauth import HTTPBasicAuth
import json
import os
#import cec_send_data
import subprocess
import lg_API
import conf
import time
import status_chequer

auth = HTTPBasicAuth()
app = Flask(__name__)


@auth.get_password
def get_password(username):
    username_password = conf.UserPass()
    if username == username_password[0]:
        return username_password[1]
    return None


@auth.error_handler
def unauthorized():
    return json.dumps({'error': 'Unauthorized access'}), 403


@app.route("/homesweethome/api/v1.0/tv", methods=['POST'])
@auth.login_required
def tv_on_off():
    if request.headers['Content-Type'] == 'application/json':
        command = request.json.get('command')
        if command:
            if command == "on":
                tv_ip = request.json.get('tv_ip')
                if tv_ip:
                    body, code, content_type = get_status(tv_ip)
                    if json.loads(body)['status'] == "off":
                        code = subprocess.call('/bin/echo "on 0" | /usr/bin/cec-client -d 1 -s', shell=True)
                        if not code: #code = 0 --> Command was executed OK
                            #select_tv_input(tv_ip)
                            err_code = 200
                            err_msj = json.dumps({'status': 'on'})
                        else:
                            err_msj = json.dumps({'error': code})
                            err_code = 500
                    else:
                        err_msj = json.dumps({'error': 'TV is already On'})
                        err_code = 409
                else:
                    err_msj = json.dumps({'error': 'Bad request - Missing TV IP'})
                    err_code = 400

            elif command == "off":
                tv_ip = request.json.get('tv_ip')
                if tv_ip:
                    body, code, content_type = get_status(tv_ip)
                    if json.loads(body)['status'] == "on":
                        err_code, message = lg_API.TurnTvOff(tv_ip)
                        if err_code == 200:
                            err_msj = json.dumps({'status': 'off'})
                        else:
                            err_msj = json.dumps({'error': message})
                    else:
                        err_msj = json.dumps({'error': 'TV is already Off'})
                        err_code = 409
                else:
                    err_msj = json.dumps({'error': 'Bad request - Missing TV IP'})
                    err_code = 400

            elif command == "control":
                tv_ip = request.json.get('tv_ip')
                action = request.json.get('action')
                if action and tv_ip:
                    if action == "vol_up":
                        err_code, message = lg_API.vol_up(request.json.get('blu_ray_ip'))
                        err_msj = json.dumps({'result': "ok"})
                    elif action == "vol_down":
                        err_code, message = lg_API.vol_down(request.json.get('blu_ray_ip'))
                        err_msj = json.dumps({'result': "ok"})
                    elif action == "vol_mute":
                        err_code, message = lg_API.vol_mute(request.json.get('blu_ray_ip'))
                        err_msj = json.dumps({'result': "ok"})
                    else:
                        err_code, message = lg_API.ControlAction(tv_ip, action)
                        if err_code != 200:
                            err_msj = json.dumps({'error': message})
                        else:
                            err_msj = json.dumps({'result': "ok"})
                else:
                    err_msj = json.dumps({'error': "Bad Request"})
                    err_code = 400

            else:
                msj = "Command '"+command+"' is not defined"
                err_msj = json.dumps({'error': msj})
                print err_msj
                err_code = 400
        else:
            err_msj = json.dumps({'error': 'Bad Request'})
            err_code = 400
    else:
        err_msj = json.dumps({'error': 'Unsupported Media Type'})
        err_code = 415

    return err_msj, err_code, {'Content-type': 'application/json'}


@app.route("/homesweethome/api/v1.0/device/status/<device_ip>", methods=['GET'])
@auth.login_required
def get_status(device_ip):
    status = check_status(device_ip)
    return status, 200, {'Content-type': 'application/json'}


def check_status(ip):
    return json.dumps({'device': ip, 'status': 'off'}) \
        if os.system("{} {} {}".format("ping -W 1 -w 3 -c 3 -q", ip, "> /dev/null 2>&1")) \
        else json.dumps({'device': ip, 'status': 'on'})



def select_tv_input(tv_ip):
    time.sleep(30)
    err_code, message = lg_API.ControlAction(tv_ip, "27")
    time.sleep(1)
    err_code, message = lg_API.ControlAction(tv_ip, "14")
    time.sleep(1)
    err_code, message = lg_API.ControlAction(tv_ip, "20")


def send_event(event):
    i = 0
    send_flag = True
    #print "event: ", event

    while send_flag and i < 5:
        send_flag = send_evt(event)
        if send_flag:
            i += 1
            print "{} send".format(i)


def send_evt(event):
    error_status = False
    url = "http://{}:39500".format(conf.ST_IP)
    headers = {
        'content-type': "application/json",
    }
    try:
        r = requests.post(url, data=event, headers=headers)
    except requests.exceptions.RequestException as e:
        print e
        error_status = True
    else:
        if r.status_code != 202:
            print "Post Error Code: {}, Post Error Message: {}".format(r.status_code, r.text)
            error_status = True
    return error_status


if __name__ == '__main__':
    os.system("python ~/git/SmartThings/SmartThings-Raspberry-TV_integration-master/python_code/status_chequer.py /dev/null 2>&1 &")
    app.run(host='0.0.0.0', port=5000, debug=True)




