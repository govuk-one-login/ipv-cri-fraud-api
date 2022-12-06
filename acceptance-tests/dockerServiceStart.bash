#docker rm -f $(docker ps -a -q)
#
#docker run -p 5050:5050 -e CHECK_RESULTS_EVERY_SECONDS=NONE -e KEEP_HISTORY=1 \
#-v ${PWD}/target/allure-results:/app/allure-results \
#-v ${PWD}/target/allure-reports:/app/default-reports \
#frankescobar/allure-docker-service

result="$(curl -s 'http://localhost:5050/allure-docker-service/emailable-report/render?project_id=default')"
echo "result: '$result'"

#curl -s 'http://localhost:5050/allure-docker-service/clean-results?project_id=default'
#curl -s 'http://localhost:5050/allure-docker-service/clean-history?project_id=default'