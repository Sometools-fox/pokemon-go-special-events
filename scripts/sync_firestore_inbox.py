import json
from pathlib import Path
from google.cloud import firestore

out = Path("inbox/incoming")
processed = Path("inbox/processed")
needs_review = Path("inbox/needs-review")

for directory in (out, processed, needs_review):
    directory.mkdir(parents=True, exist_ok=True)

db = firestore.Client()
created = 0

for doc in db.collection("collectorInbox").stream():
    data = doc.to_dict()

    if data.get("status") != "pending":
        continue

    item_id = data.get("clientItemId") or doc.id
    safe_id = "".join(
        c for c in item_id if c.isalnum() or c in "-_"
    )

    if not safe_id:
        continue

    filename = f"{safe_id}.json"
    path = out / filename

    # clientItemId = 唯一鍵。
    # 只要資料已存在於任何 Inbox 狀態目錄，就不再從 Firestore 重複匯入。
    if any((directory / filename).exists() for directory in (out, processed, needs_review)):
        continue

    uploaded = data.get("uploadedAt")
    if hasattr(uploaded, "isoformat"):
        uploaded = uploaded.isoformat()

    payload = {
        "protocolVersion": data.get("protocolVersion", 1),
        "clientItemId": item_id,
        "createdAt": data.get("createdAt"),
        "type": data.get("type"),
        "text": data.get("text", ""),
        "attachmentCount": data.get("attachmentCount", 0),
        "sourceStatus": data.get("status", "pending"),
        "importStatus": "pending",
        "uploadedAt": uploaded,
    }

    path.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )

    created += 1

print(f"Imported {created} new Inbox item(s).")
