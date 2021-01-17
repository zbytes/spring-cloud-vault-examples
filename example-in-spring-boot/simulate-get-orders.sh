#!/usr/bin/env bash

while true
do
  curl -I http://localhost:9080/api/customers/CUST0001/students
	sleep 1
done
