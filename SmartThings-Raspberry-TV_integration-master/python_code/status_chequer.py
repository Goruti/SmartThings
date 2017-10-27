import flask_restful_server
import conf
import json
import time
import psutil
import os


def main():
    print "Stating 'Status Chequer'"
    TV_STATUS = ''
    RPI_STATUS = ''

    try:
        while True:
            #check TV status
            TV_STATUS = check_tv_status(TV_STATUS)
            RPI_STATUS = get_send_rpi_stats(RPI_STATUS)
            time.sleep(60)

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

    flask_restful_server.send_event(json.dumps(evt))


def check_tv_status(status):
    TV_IP = conf.TV_IP
    resp = json.loads(flask_restful_server.check_status(TV_IP))
    if status != resp.get("status"):
        status = resp.get("status")
        answer = {"type": "tv_status", "body": resp}
        flask_restful_server.send_event(json.dumps(answer))
        return status


if __name__ == '__main__':
    main()