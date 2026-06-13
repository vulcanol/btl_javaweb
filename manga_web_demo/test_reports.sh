#!/bin/bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin", "password":"admin123"}' | jq -r .token)
echo "Token: $TOKEN"
curl -s -v -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/reports
