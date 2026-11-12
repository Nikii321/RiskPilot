#!/usr/bin/env bash
# Smoke test against a running local Compose deployment. Uses curl, not Python.
set -euo pipefail
base="${RISK_PILOT_URL:-http://localhost:8080}"
temp_dir="$(mktemp -d)"
risk_id=""
cleanup() {
  if [ -n "$risk_id" ]; then curl -fsS -X DELETE "$base/api/risks/$risk_id" >/dev/null || true; fi
  rm -rf "$temp_dir"
}
trap cleanup EXIT
curl -fsS "$base/actuator/health" > "$temp_dir/health.json"
curl -fsS "$base/api/analysis" > "$temp_dir/analysis.json"
grep -q '"netBenefit"' "$temp_dir/analysis.json"
cat > "$temp_dir/input.json" <<'JSON'
{"title":"Smoke test persistence","category":"TECHNICAL","owner":"Smoke test","loss":500,"delayDays":10,"urgency":3,"features":{"complexity":0.5,"experience":0.5,"workload":0.5,"dependencies":3,"volatility":0.5,"reliability":0.5}}
JSON
curl -fsS -X POST "$base/api/risks" -H 'Content-Type: application/json' --data-binary @"$temp_dir/input.json" > "$temp_dir/created.json"
risk_id="$(sed -nE 's/.*"id":"([a-f0-9-]+)".*/\1/p' "$temp_dir/created.json")"
test -n "$risk_id"
curl -fsS -X PUT "$base/api/risks/$risk_id" -H 'Content-Type: application/json' --data-binary @"$temp_dir/input.json" > "$temp_dir/updated.json"
status="$(curl -sS -o "$temp_dir/conflict.json" -w '%{http_code}' -X PUT "$base/api/risks/$risk_id" -H 'Content-Type: application/json' --data-binary @"$temp_dir/input.json")"
test "$status" = 409
if [ "${CHECK_RESTART:-0}" = 1 ]; then
  docker compose restart app
  healthy=0
  for i in $(seq 1 40); do
    if curl -fsS "$base/actuator/health" >/dev/null 2>&1; then healthy=1; break; fi
    sleep 1
  done
  test "$healthy" = 1
fi
curl -fsS "$base/api/risks" > "$temp_dir/risks.json"
grep -q "$risk_id" "$temp_dir/risks.json"
printf 'PASS: health, analysis, create, update, version conflict, persistence (restart=%s)\n' "${CHECK_RESTART:-0}"
