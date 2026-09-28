# Secure Inbox Sync V1

Flow:
Android Share -> Local Inbox -> HTTPS Inbox API -> GitHub inbox/incoming -> ChatGPT V2 import -> data/events.json

## Upload payload
- protocolVersion
- clientItemId
- createdAt
- type
- text
- attachments[]

## Required API behavior
1. HTTPS only
2. request authentication
3. payload size limits
4. generated server-side receipt id
5. no permission to modify data/events.json
6. idempotency by clientItemId
7. attachment MIME allow-list: image/jpeg, image/png, image/webp
8. reject executable/archive payloads

Do not embed a GitHub Personal Access Token in the APK.
