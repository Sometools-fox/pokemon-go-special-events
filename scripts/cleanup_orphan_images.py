#!/usr/bin/env python3
"""Delete unreferenced collector objects older than the retention window.

Objects referenced by incoming or needs-review are always protected, regardless of age.
Processed references may already have been deleted by the normal cleanup workflow.
"""
import argparse, json
from datetime import datetime, timezone, timedelta
from pathlib import Path
from google.cloud import storage

ROOT=Path("inbox")
BUCKET="pokemon-go-special-events.firebasestorage.app"

def refs(folder):
    out=set()
    for p in (ROOT/folder).glob("*.json"):
        if p.name=="index.json": continue
        try: d=json.loads(p.read_text(encoding="utf-8"))
        except Exception: continue
        for a in d.get("attachments") or []:
            s=a.get("storagePath")
            if isinstance(s,str) and s.startswith("collector/"): out.add(s)
    return out

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("--days",type=int,default=30)
    ap.add_argument("--apply",action="store_true")
    args=ap.parse_args()
    protected=refs("incoming")|refs("needs-review")
    cutoff=datetime.now(timezone.utc)-timedelta(days=args.days)
    client=storage.Client(project="pokemon-go-special-events")
    bucket=client.bucket(BUCKET)
    candidates=[]
    for blob in client.list_blobs(bucket,prefix="collector/"):
        if blob.name in protected: continue
        created=blob.time_created
        if created and created < cutoff: candidates.append(blob)
    print(f"protected refs: {len(protected)}")
    print(f"older than {args.days} days and unprotected: {len(candidates)}")
    for b in candidates:
        print(("DELETE " if args.apply else "DRY-RUN DELETE ")+b.name)
        if args.apply: b.delete()
if __name__=="__main__": main()
