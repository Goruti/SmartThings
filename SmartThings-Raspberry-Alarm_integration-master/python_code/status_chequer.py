import conf
import json
import requests
import time
import psutil
import os


def main():
    print "Stating 'Status Chequer'"

    try:
        while True:
            get_send_rpi_stats()
            time.sleep(conf.SLEEP_TIME)

    except Exception as e:
        print "Error in Status Chequer. Error:".format(e)


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
            "memory": memory.percent(),
            "diskUsage": disk.percent(),
            #"hubInfo": "online"
        }
    }

    send_event(json.dumps(evt))


def send_event(event):
    i = 0
    send_flag = True

    while send_flag and i < 5:
        send_flag = send_evt(event)
        if send_flag:
            i += 1
            print "{} send".format(i)


def send_evt(event):
    error_status = False
    url = "http://{}:{}".format(conf.ST_IP, conf.ST_PORT)
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
    main()
