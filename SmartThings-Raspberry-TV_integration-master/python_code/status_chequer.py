import flask_restful_server
import conf
import json
import time


def main():
    print "Stating 'Status Chequer'"
    TV_STATUS = ''

    try:
        while True:
            #check TV status
            TV_STATUS = check_tv_status(TV_STATUS)

            time.sleep(60)

    except Exception as e:
        print "Error in Status Chequer. Error:".format(e)


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