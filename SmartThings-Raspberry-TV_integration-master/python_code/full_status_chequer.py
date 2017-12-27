from conf import configuration
import json
import time
import flask_restful_server

from python_common_tools.tools import send_event_to_st
from python_common_tools.tools import get_send_rpi_stats


def main():
    print "Stating 'FULL Status Chequer'"
    TV_STATUS = ''
    try:
        while True:
            #  check TV status
            TV_STATUS = check_tv_status(TV_STATUS)
            #  check RPI status
            send_event_to_st(json.dumps(get_send_rpi_stats()), configuration["ST_IP"])
            time.sleep(configuration["SLEEP_TIME"])
    except Exception as e:
        print "Error in Status Chequer. Error:".format(e)


def check_tv_status(status):
    tv_ip = configuration["TV_IP"]
    resp = json.loads(flask_restful_server.check_status(tv_ip))
    if status != resp.get("status"):
        status = resp.get("status")
        answer = {"type": "tv_status", "body": resp}
        send_event_to_st(json.dumps(answer), configuration["ST_IP"])
        return status


if __name__ == '__main__':
    main()