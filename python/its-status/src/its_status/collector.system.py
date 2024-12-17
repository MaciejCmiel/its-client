# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import json
import psutil
from its_status import helpers


class Status:
    def __init__(self, *, cfg):
        self.cfg = cfg

        hw = None

        # This is the ugly-dirty code style unamically dictated by black
        # which does not allow us to keep corresponding options side-by-side
        # with their value... 😢
        lshw_cmd = [
            "lshw",
            "-quiet",
            "-json",
            "-disable",
            "usb",
            "-disable",
            "pci",
            "-disable",
            "pcilegacy",
            "-disable",
            "isapnp",
            "-disable",
            "network",
        ]
        ret = helpers.run(lshw_cmd)
        if ret.returncode == 0:
            lshw = json.loads(ret.stdout)
            if type(lshw) is list:
                # Very old version of lshw, let's hope for the best...
                lshw = lshw[0]
            if "product" in lshw:
                hw = lshw["product"]
            else:
                try:
                    for child in lshw["children"]:
                        if child["id"] == "core" and "product" in child:
                            hw = child["product"]
                            break
                except TypeError:
                    # lshw output does not match our expectations, ignore
                    pass
        if hw is None:
            try:
                with open("/proc/self/cgroup", "rb") as f:
                    lines = f.readlines()
                if lines[-1].decode().split(":")[2].startswith("/docker/"):
                    hw = "oci"
            except Exception:
                pass

        self.static_data = {
            "type": "obu",
            "hardware": hw or "Unknown",
        }
        with open("/etc/os-release", "r") as f:
            self.static_data["os_release"] = dict()
            for l in f.readlines():
                key = l.split("=", maxsplit=1)[0]
                val = l.split("=", maxsplit=1)[1].rstrip().strip('"')
                self.static_data["os_release"][key] = val

    def capture(self):
        data = self.static_data
        data["cpu_load"] = psutil.getloadavg()
        mem = psutil.virtual_memory()
        data["memory"] = (mem.total, mem.available)
        disk = psutil.disk_usage(self.cfg["system"]["data-dir"])
        data["storage"] = (disk.total, disk.free)

        return data
