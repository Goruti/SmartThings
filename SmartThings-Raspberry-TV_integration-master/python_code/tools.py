import requests
import psutil
import os
import nmap
import json
import time
from urlparse import urlparse
import xml.etree.ElementTree as ET
import ssdp
from conf import configuration


def get_send_rpi_stats():
    cpu = psutil.cpu_percent()
    temp = float(os.popen('vcgencmd measure_temp').readline().replace("temp=", "").replace("'C\n", ""))
    memory = psutil.virtual_memory()
    disk = psutil.disk_usage('/')

    evt = {
        "type": "rpi_status",
        "body": {
            "temperature": temp,
            "cpuPercentage": cpu,
            "memory": memory.percent,
            "diskUsage": disk.percent,
            #"hubInfo": "online"
        }
    }
    return evt


def send_event_to_st(event, st_ip):
    i = 0
    resend_flag = True
    #print "event: ", event

    while resend_flag and i < 5:
        resend_flag = __send_evt__(event, st_ip)
        if resend_flag:
            i += 1
            print "{} send".format(i)


def __send_evt__(event, st_ip):
    tx_error = False
    url = "http://{}:39500".format(st_ip)
    headers = {
        'content-type': "application/json",
    }
    try:
        r = requests.post(url, data=event, headers=headers)
    except requests.exceptions.RequestException as e:
        print e
        tx_error = True
    else:
        if r.status_code != 202:
            print "Post Error Code: {}, Post Error Message: {}".format(r.status_code, r.text)
            tx_error = True
    return tx_error


def check_status(ip):
    return json.dumps({'device': ip, 'status': 'off'}) \
        if os.system("{} {} {}".format("ping -W 1 -w 3 -c 3 -q", ip, "> /dev/null 2>&1")) \
        else json.dumps({'device': ip, 'status': 'on'})


def get_smartthing_ip():
    st_ip = __get_st_ip__()
    if not st_ip:
        print "Smartthings Hub is DOWN"
        exit(1)

    if configuration["ST_IP"] != st_ip:
        configuration["ST_IP"] = st_ip
        with open('conf.py', 'w') as f:
            f.write("configuration = {}".format(configuration))
        time.sleep(1)


def __get_st_ip__():
    ST_IP = None
    nm = nmap.PortScanner()
    nm.scan(hosts='192.168.1.0/24', arguments='-p39500 --open')
    if nm.all_hosts():
        ST_IP = nm.all_hosts()[0]
    else:
        print "Smartthings Hub is not UP"
    return ST_IP


def get_tv_ip():
    tv_search_query = "ssdp:all"
    for device in ssdp.discover(tv_search_query):
        location = device.location
        device = ET.fromstring(requests.get(location).text)[1]

        if len(device) >= 10:
            if device[4].text == 'LG Digital Media Renderer TV' and \
                            device[7].text == 'LG Electronics' and \
                            device[10].text == 'LG TV':
                tv_ip = urlparse(location).netloc.split(":")[0]
                break

    if not tv_ip:
        print "TV is OFF, please turn on your LG TV"
        exit(1)

    if configuration["TV_IP"] != tv_ip:
        configuration["TV_IP"] = tv_ip
        with open('conf.py', 'w') as f:
            f.write("configuration = {}".format(configuration))
        time.sleep(1)