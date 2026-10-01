#!/usr/bin/env python3
import json
from datetime import datetime, timedelta, date
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "data" / "events.json"
OUT = ROOT / "calendar.ics"
SITE = "https://sometools-fox.github.io/pokemon-go-special-events/"

def esc(v):
    return str(v or "").replace("\\","\\\\").replace("\n","\\n").replace(",","\\,").replace(";","\\;")

def fold(line):
    b=line.encode("utf-8")
    if len(b)<=75:return line
    out=[]; cur=""
    for ch in line:
        test=(cur+ch).encode("utf-8")
        if len(test)>73 and cur:
            out.append(cur); cur=" "+ch
        else: cur+=ch
    out.append(cur)
    return "\r\n".join(out)

events=json.loads(DATA.read_text(encoding="utf-8"))
now=datetime.utcnow().strftime("%Y%m%dT%H%M%SZ")
lines=["BEGIN:VCALENDAR","VERSION:2.0","PRODID:-//Sometools-fox//Pokemon GO Special Events//ZH-Hant","CALSCALE:GREGORIAN","METHOD:PUBLISH","X-WR-CALNAME:Pokémon GO 特殊活動","X-WR-CALDESC:Pokémon GO 特殊活動資料庫"]
for e in events:
    s=e.get("schedule",{}).get("startDate"); en=e.get("schedule",{}).get("endDate")
    if not s or not en: continue
    sd=date.fromisoformat(s); ed=date.fromisoformat(en)+timedelta(days=1)
    estimated=e.get("schedule",{}).get("timeStatus")=="estimated"
    title=("⚠️ " if estimated else "")+e.get("name","")
    locs=e.get("locations") or []
    location=next((x.get("name","") for x in locs if x.get("type")=="primary" and x.get("name")), "")
    if not location: location=next((x.get("name","") for x in locs if x.get("name")), "")
    rewards=[x.get("name","") for x in (e.get("specialRewards") or []) if x.get("name")][:3]
    total=len([x for x in (e.get("specialRewards") or []) if x.get("name")])
    reward_text="、".join(rewards)+(f" +{total-3}" if total>3 else "")
    url=SITE+"event.html?id="+e["id"]
    desc=[]
    if reward_text: desc.append("特殊獲取物："+reward_text)
    desc.append("詳細資料："+url)
    lines += ["BEGIN:VEVENT",f"UID:{esc(e['id'])}@pokemon-go-special-events",f"DTSTAMP:{now}",
              f"DTSTART;VALUE=DATE:{sd.strftime('%Y%m%d')}",f"DTEND;VALUE=DATE:{ed.strftime('%Y%m%d')}",
              f"SUMMARY:{esc(title)}"]
    if location: lines.append(f"LOCATION:{esc(location)}")
    lines += [f"DESCRIPTION:{esc(chr(10).join(desc))}",f"URL:{url}","END:VEVENT"]
lines.append("END:VCALENDAR")
OUT.write_text("\r\n".join(fold(x) for x in lines)+"\r\n",encoding="utf-8")
print(f"Generated {OUT} with {len(events)} source events")
