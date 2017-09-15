# DEPRECATED. CURRENTLY THIS FILE IS NOT USED!!!!

import cec

def send_data(data):
    rsp = False
    msg = None
    cecconfig = cec.libcec_configuration()
    cecconfig.strDeviceName = "pyLibCec"
    cecconfig.bActivateSource = 0
    cecconfig.deviceTypes.Add(cec.CEC_DEVICE_TYPE_RECORDING_DEVICE)
    cecconfig.clientVersion = cec.LIBCEC_VERSION_CURRENT
    lib = cec.ICECAdapter.Create(cecconfig)

    adapters = lib.DetectAdapters()
    for adpt in adapters:
        adapter = adpt.strComName

    if adapter:
        if lib.Open(adapter):
            if lib.Transmit(lib.CommandFromString(data)):
                rsp = True
            else:
                msg = "failed to send command"
        else:
            msg = "failed to open a connection to the CEC adapter"
    else:
        msg = "No adapters found"

    return rsp, msg
