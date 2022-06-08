#!/usr/bin/env bash

set -eu

declare status_code
status_code="$(curl --silent --output /dev/null --write-out '%{http_code}' "https://www.example.com")"

if [[ $status_code != "200" ]]; then
  cat <<EOF > "$TEST_REPORT_DIR/result.json"
[
  {
    "uri": "test.sh",
    "name": "Acceptance test",
    "elements": [
      {
        "type": "scenario",
        "name": "API Gateway request",
        "line": 6,
        "steps": [
          {
            "keyword": "Given ",
            "name": "this step fails",
            "line": 6,
            "match": {
              "location": "test.sh:4"
            },
            "result": {
              "status": "failed",
              "error_message": " Lambda did not return HTTP status code 200",
              "duration": 1
            }
          }
        ]
      }
    ]
  }
]
EOF
exit 1
else
  cat <<EOF > "$TEST_REPORT_DIR/result.json"
[
  {
    "uri": "test.sh",
    "name": "Acceptance test",
    "elements": [
      {
        "type": "scenario",
        "name": "API Gateway request",
        "line": 6,
        "steps": [
          {
            "keyword": "Given ",
            "name": "this step fails",
            "line": 6,
            "match": {
              "location": "test.sh:4"
            },
            "result": {
              "status": "passed",
              "duration": 1
            }
          }
        ]
      }
    ]
  }
]
EOF
fi
