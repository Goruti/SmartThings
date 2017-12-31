from conf import configuration
import json
import time
import tools


def main():
    print "Stating 'FULL Status Chequer'"
    TV_STATUS = ''
    try:
        while True:
            #  check TV status
            TV_STATUS = check_tv_status(TV_STATUS)
            #  check RPI status
            tools.send_event_to_st(json.dumps(tools.get_send_rpi_stats()), configuration["ST_IP"])
            time.sleep(configuration["SLEEP_TIME"])
    except Exception as e:
        print "Error in Status Chequer. Error:".format(e)


def check_tv_status(status):
    resp = json.loads(tools.check_status(configuration["TV_IP"]))
    if status != resp.get("status"):
        status = resp.get("status")
        answer = {"type": "tv_status", "body": resp}
        tools.send_event_to_st(json.dumps(answer), configuration["ST_IP"])
        return status


if __name__ == '__main__':
    main()