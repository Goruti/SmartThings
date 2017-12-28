import requests
import psutil
import os
import nmap


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


def get_st_ip():
    ST_IP = None
    nm = nmap.PortScanner()
    nm.scan(hosts='192.168.1.0/24', arguments='-p39500 --open')
    if nm.all_hosts():
        ST_IP = nm.all_hosts()[0]
    else:
        print "Smartthings Hub is not UP"
    return ST_IP

