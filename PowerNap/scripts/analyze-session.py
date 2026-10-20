#!/usr/bin/env python3
import csv, statistics, sys, re
from collections import Counter
from pathlib import Path

if len(sys.argv) != 2:
    print("Usage: analyze-session.py SESSION.csv", file=sys.stderr)
    sys.exit(2)

path = Path(sys.argv[1])
rows = list(csv.DictReader(path.open(newline='', encoding='utf-8')))
print(f"File: {path}")
print(f"Rows: {len(rows)}")
counts = Counter(r['type'] for r in rows)
print("Types:", dict(counts))


def pct(values, q):
    if not values:
        return None
    values = sorted(values)
    i = round((len(values)-1) * q)
    return values[max(0, min(len(values)-1, i))]

for kind in ('ACC','HR','IBI','SNAP'):
    rs = [r for r in rows if r['type'] == kind]
    if not rs:
        print(f"{kind}: NO DATA")
        continue
    ts = sorted(int(r['sensor_timestamp_ms']) for r in rs if r['sensor_timestamp_ms'])
    delays = [int(r['delivery_delay_ms']) for r in rs if r['delivery_delay_ms']]
    gaps = [b-a for a,b in zip(ts, ts[1:]) if b >= a]
    print(f"{kind}: {len(rs)} samples, span={(max(ts)-min(ts))/1000:.1f}s")
    if gaps:
        print(f"  sensor gap median={statistics.median(gaps):.0f}ms p95={pct(gaps,.95):.0f}ms max={max(gaps)}ms >5s={sum(g>5000 for g in gaps)}")
    if delays:
        print(f"  delivery delay median={statistics.median(delays):.0f}ms p95={pct(delays,.95):.0f}ms max={max(delays)}ms >5s={sum(d>5000 for d in delays)}")

hrs=[]
for r in rows:
    if r['type']=='HR' and r['hr']:
        try: hrs.append(float(r['hr']))
        except ValueError: pass
if hrs:
    print(f"HR: min={min(hrs):.0f} median={statistics.median(hrs):.0f} max={max(hrs):.0f}")

ibis=[]
for r in rows:
    if r['type']=='IBI' and r['ibi']:
        try: ibis.append(float(r['ibi']))
        except ValueError: pass
if ibis:
    suspicious=[x for x in ibis if x < 400 or x > 2000]
    print(f"IBI: min={min(ibis):.0f} median={statistics.median(ibis):.0f} max={max(ibis):.0f}; suspicious(<400/>2000)={len(suspicious)}")

snap=[r for r in rows if r['type']=='SNAP']
if snap:
    states=Counter(r['state'].strip('"') for r in snap)
    conf=[float(r['confidence']) for r in snap if r['confidence']]
    print("Detector states:", dict(states))
    if conf: print(f"Confidence max={max(conf)*100:.0f}% median={statistics.median(conf)*100:.0f}%")

# v0.2.2 source-level diagnostics
for label in ('HR','ACC'):
    req=[]; complete=[]; skipped=[]
    for r in rows:
        if r['type'] != 'DIAG':
            continue
        reason=r.get('reason','') or ''
        if reason.startswith(f'FLUSH_REQUEST {label}'):
            req.append(reason)
        elif reason.startswith(f'FLUSH_COMPLETE {label}'):
            m=re.search(r'latency_ms=(-?\d+)', reason)
            if m: complete.append(int(m.group(1)))
        elif reason.startswith(f'FLUSH_SKIP {label}'):
            skipped.append(reason)
    if req or complete or skipped:
        accepted=sum('accepted=true' in x.lower() for x in req)
        print(f"Flush {label}: requests={len(req)} accepted={accepted} completed={len(complete)} skipped={len(skipped)}")
        if complete:
            print(f"  completion latency median={statistics.median(complete):.0f}ms max={max(complete)}ms")

print("Events:")
for r in rows:
    if r['type']=='EVENT':
        print(f"  {r['sensor_timestamp_ms']}: {r['reason']}")

feedback=[r for r in rows if r['type']=='FEEDBACK']
if feedback:
    print('Feedback (user-reported ground truth):')
    for r in feedback:
        print(f"  {r['received_timestamp_ms']}: {r['reason']} (session state={r['state']})")
else:
    print('Feedback: none')
