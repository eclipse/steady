#!/bin/bash
VULAS_CORE_NAMESPACE="steady-core"
VULAS_MONITORING_NAMESPACE="steady-monitoring"
VULAS_ADMIN_NAMESPACE="steady-admin"

generate_docs() {
	helmutils.py docs . -v values.yaml -t README.md || :
	helmutils.py docs . -v values_prod.yaml -t README_ADV.md || :
	helmutils.py docs . -v values_adv.yaml -t README_PROD.md || :
}

walk() {
  for chart in $(find . -mindepth 3 -name values.yaml -printf "%h\n"); do
    cd $chart
    echo -e "\n\n Walking to chart $chart"
    generate_docs
    cd - > /dev/null
  done
}

generate_score() {
  # Runs popeye tests on core namespace
  echo -e "\n\n Generating report for core chart"
  popeye -f ci/spinach.yaml \
        -o yaml \
        -n $VULAS_CORE_NAMESPACE > ci/results/core.yaml
  # Runs popeye tests on monitoring namespace
  echo -e "\n\n Generating report for monitoring chart"
  popeye -f ci/spinach.yaml \
        -o yaml \
        -n $VULAS_MONITORING_NAMESPACE > ci/results/monitoring.yaml
  # Runs popeye tests on admin namespace
  echo -e "\n\n Generating report for admin chart"
  popeye -f ci/spinach.yaml \
        -o yaml \
        -n $VULAS_ADMIN_NAMESPACE > ci/results/admin.yaml
}

inject_score() {
  VULAS_CORE_SCORE=$(cat ci/results/core.yaml | yq '.popeye.score')
  VULAS_MONITORING_SCORE=$(cat ci/results/monitoring.yaml | yq '.popeye.score')
  VULAS_ADMIN_SCORE=$(cat ci/results/admin.yaml | yq '.popeye.score')

  sed -i -e "s|CoreChartScore-\([[:digit:]]*\)-green|CoreChartScore-$VULAS_CORE_SCORE-green|g" README.md
  sed -i -e "s|MonitoringChartScore-\([[:digit:]]*\)-green|MonitoringChartScore-$VULAS_MONITORING_SCORE-green|g" README.md
  sed -i -e "s|AdminChartScore-\([[:digit:]]*\)-green|AdminChartScore-$VULAS_ADMIN_SCORE-green|g" README.md
}

walk
generate_score
inject_score
