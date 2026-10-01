#!/usr/bin/env python3
"""Safely clean Firebase Storage attachments after Inbox processing.

Default is dry-run. Only files referenced by inbox/processed/*.json are candidates.
Anything still referenced by inbox/incoming or inbox/needs-review is protected.
"""
import argparse, json
from pathlib import Path
from google.cloud import storage

ROOT=Path("inbox")
BUCKET="pokemon-go-special-events.firebasestorage.app"

def paths(folder):
    out=set()
    for p in (ROOT/folder).glob("*.json"):
        if p.name=="index.json": continue
        try: data=json.loads(p.read_text(encoding="utf-8"))
        except Exception: continue
        for a in data.get("attachments") or []:
            s=a.get("storagePath")
            if isinstance(s,str) and s.startswith("collector/"):
                out.add(s)
    return out

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("--apply",action="store_true")
    args=ap.parse_args()
    processed=paths("processed")
    protected=paths("incoming")|paths("needs-review")
    candidates=sorted(processed-protected)
    print(f"processed refs: {len(processed)}")
    print(f"protected refs: {len(protected)}")
    print(f"delete candidates: {len(candidates)}")
    for p in candidates: print(("DELETE " if args.apply else "DRY-RUN DELETE ")+p)
    if not args.apply: return
    bucket=storage.Client(project="pokemon-go-special-events").bucket(BUCKET)
    for p in candidates:
        blob=bucket.blob(p)
        if blob.exists():
            blob.delete()
            print("deleted "+p)
        else:
            print("already absent "+p)

if __name__=="__main__": main()
