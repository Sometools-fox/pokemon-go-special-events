import json
from pathlib import Path
from google.cloud import firestore

out = Path("inbox/incoming")
out.mkdir(parents=True, exist_ok=True)

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

    path = out / f"{safe_id}.json"

    # clientItemId = 唯一鍵。
    # 已經進 GitHub 的資料不重複匯入。
    if path.exists():
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
