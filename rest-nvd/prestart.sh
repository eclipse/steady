#!/bin/bash

echo "Triggering the update of feeds. A manual full update might be a good idea."
python /app/app/update.py --verbose
echo "Done"

