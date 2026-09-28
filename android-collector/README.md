# Android Collector V1

## 目標
Android 分享選單中的「Pokémon GO 活動收集器」，快速接收網址、文字、單張或多張圖片。

## V1 架構
Share Intent → Collector Android App → 本機 Inbox（Room）→ 待處理清單。

V1 不包含 GitHub Token，也不直接修改 events.json。

## 支援 Intent
- ACTION_SEND text/plain
- ACTION_SEND image/*
- ACTION_SEND_MULTIPLE image/*

## Inbox record
- id: UUID
- createdAt: ISO timestamp
- type: link | text | image | images
- text: nullable
- uriList: list
- status: pending | processed
- batchId: UUID

## UX
分享進 App 後立即保存，顯示短暫「已加入待處理」並結束 ShareActivity。
主畫面只顯示待處理數量、最近項目、刪除與標記已處理。

## Security
APK 不保存 GitHub PAT。正式同步採獨立安全 Inbox API；同步層完成前資料只留本機。
