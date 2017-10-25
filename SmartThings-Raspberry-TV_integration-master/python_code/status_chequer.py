import flask_restful_server
import conf
import json
import time


def main():
    print "Stating 'Status Chequer'"
    TV_IP = conf.TV_IP
    TV_STATUS = ''

    try:
        while True:
            answer = json.loads(flask_restful_server.check_status(TV_IP))
            if TV_STATUS != answer.get("status"):
                TV_STATUS = answer.get("status")
                answer["type"] = "switch_status"
                flask_restful_server.send_event(json.dumps(answer))
            time.sleep(60)
    except Exception as e:
        print "Error in Status Chequer. Error:".format(e)


if __name__ == '__main__':
    main()