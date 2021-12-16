export default function(model) {
    model.state.simulator.started = true
    model.state.evse = {
        "heartbeatInterval": 86400,
        "evConnectionTimeOut": 120,
        "evses": [
            {
                "id": 1,
                "connectors": [
                    {
                        "id": 1,
                        "cableStatus": "UNPLUGGED",
                        "connectorStatus": "Available"
                    },
                    {
                        "id": 2,
                        "cableStatus": "UNPLUGGED",
                        "connectorStatus": "Available"
                    }
                ],
                "tokenId": null,
                "charging": false,
                "seqNo": 0,
                "evseStatus": "AVAILABLE",
                "transaction": {
                    "transactionId": null,
                    "status": "NONE"
                },
                "scheduledNewEvseStatus": null,
                "totalConsumedWattHours": 0
            }
        ],
        "currentTime": "2021-09-22 08:19:54"
    }


    model.state.simulator.evses = [
        {
            "id": " EVB-X000001",
            "selected": true
        },
        {
            "id": " EVB-X000002",
            "selected": false
        }
    ]
}
