#!/bin/sh
# Runs popeye tests on core namespace
popeye -f /tests/spinach.yaml \
        -o junit \
        -n $VULAS_CORE_NAMESPACE > /tests/results/core.xml

# Runs popeye tests on monitoring namespace
popeye -f /tests/spinach.yaml \
        -o junit \
        -n $VULAS_MONITORING_NAMESPACE > /tests/results/monitoring.xml

# Runs popeye tests on admin namespace
popeye -f /tests/spinach.yaml \
      -o junit \
      -n $VULAS_ADMIN_NAMESPACE > /tests/results/admin.xml
